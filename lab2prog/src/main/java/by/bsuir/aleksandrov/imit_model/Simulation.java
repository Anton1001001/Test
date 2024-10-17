package by.bsuir.aleksandrov.imit_model;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.*;

public class Simulation {
    private final PriorityBlockingQueue<Event> eventQueue = new PriorityBlockingQueue<>();
    private final RandomGenerator generator = new RandomGenerator();
    private final ControlStation[] stations;
    private final AdjustmentStation adjustmentStation;
    private final double meanArrivalTime;
    private final double varianceArrivalTime;
    private final double simulationEndTime;

    private Map<Double, Integer> numOfAdjQue;
    private Map<Double, Double> averQueSizes;

    private int totalProcessed = 0;
    private int totalDefective = 0;
    private int totalFirstRepaire = 0;
    private double totalTimeInSystem = 0;
    private double totalQueueLengthTime = 0;
    private double totalAdjustmentBusyTime = 0;
    private double previousTimeOfChangingAdj = 0;
    private double previousTimeOfChangingQue = 0;
    private int currentQueueLength = 0;

    private List<Double> averageTimesInSystem = new ArrayList<>();
    private List<Integer> totalProcesseds = new ArrayList<>();
    private List<Double> adjustmentUtilizations = new ArrayList<>();
    private List<Double> averageQueueLengths = new ArrayList<>();
    private List<Double> defectivePercentages = new ArrayList<>();
    private List<Double> repairedPercentages = new ArrayList<>();

    private boolean trace;

    public Simulation(ControlStation[] stations, AdjustmentStation adjustmentStation, double meanArrivalTime, double varianceArrivalTime, double simulationEndTime, boolean trace) {
        this.stations = stations;
        this.adjustmentStation = adjustmentStation;
        this.meanArrivalTime = meanArrivalTime;
        this.varianceArrivalTime = varianceArrivalTime;
        this.simulationEndTime = simulationEndTime;
        this.trace = trace;
    }

    public void startSimulation() {
        averQueSizes = new HashMap<>();
        numOfAdjQue = new HashMap<>();
        double currentTime = 0;
        totalProcessed = 0;
        totalDefective = 0;
        totalFirstRepaire = 0;
        totalTimeInSystem = 0;
        totalQueueLengthTime = 0;
        totalAdjustmentBusyTime = 0;
        previousTimeOfChangingAdj = 0;
        previousTimeOfChangingQue = 0;
        currentQueueLength = 0;
        adjustmentStation.setNextFreeTime(0);
        stations[0].setNextFreeTime(0);
        stations[1].setNextFreeTime(0);
        stations[2].setNextFreeTime(0);

        if (trace) {
            System.out.println("Начало моделирования\n");
        }
        eventQueue.add(new Event(currentTime, Event.GENERATE_PRODUCT, null));

        while (!eventQueue.isEmpty() || currentTime < simulationEndTime) {
            Event event = eventQueue.poll();
            if (event == null) {
                break;
            }
            currentTime = event.getEventTime();
            handleEvent(event, currentTime);
        }
        if (trace) {
            System.out.println("\nОкончание моделирования\n");
        }

        calculateMetrics(currentTime);
    }

    private void handleEvent(Event event, double currentTime) {
        switch (event.getEventType()) {
            case Event.GENERATE_PRODUCT:
                generateProduct(currentTime);
                break;
            case Event.START_PROCESSING:
                startProcessing(event.getProduct(), currentTime);
                break;
            case Event.END_PROCESSING:
                endProcessing(event.getProduct(), currentTime);
                break;
            case Event.START_ADJUSTMENT:
                startAdjustment(event.getProduct(), currentTime);
                break;
            case Event.END_ADJUSTMENT:
                endAdjustment(event.getProduct(), currentTime);
                break;
        }
    }

    private void generateProduct(double currentTime) {
        Product product = new Product();
        totalProcessed++;
        double nextArrivalTime = currentTime + generator.generateNormalTime(meanArrivalTime, varianceArrivalTime);
        if (!(nextArrivalTime > simulationEndTime)) {
            eventQueue.add(new Event(nextArrivalTime, Event.GENERATE_PRODUCT, null));
        }
        eventQueue.add(new Event(currentTime, Event.START_PROCESSING, product));
        if (trace) {
            System.out.println("Прибыл новый продукт с id " + product.getId() + " текущее время " + currentTime + " расчетное время прихода следующего продукта " + nextArrivalTime);
        }
    }

    private void startProcessing(Product product, double currentTime) {
        if (product.getStartTimeInSystem() == null) {
            product.setStartTimeInSystem(currentTime);
        }
        ControlStation currentStation = stations[product.getCurrentStation()];
        if (currentStation.getNextFreeTime() > currentTime) {
            eventQueue.add(new Event(currentStation.getNextFreeTime(), Event.START_PROCESSING, product));
        } else {
            double processTime = currentStation.getProcessTime(generator);
            if (trace) {
                System.out.println("Начало котроля продукта " + product.getId() + " на станции " + product.getCurrentStation() + " текущее время " + currentTime + " рассчетное время контроля " + processTime);
            }
            eventQueue.add(new Event(currentTime + processTime, Event.END_PROCESSING, product));
            currentStation.setNextFreeTime(currentTime + processTime);
        }
    }

    private void endProcessing(Product product, double currentTime) {
        ControlStation currentStation = stations[product.getCurrentStation()];
        if (trace) {
            System.out.println("Окончание котроля продукта " + product.getId() + " на станции " + product.getCurrentStation() + " текущее время " + currentTime);
        }
        totalTimeInSystem += currentTime - product.getStartTimeInSystem();
        if (currentStation.shouldSendForAdjustment(generator)) {
            product.incrementDefect();
            totalFirstRepaire++;
            if (product.getDefectCount() > 1) {
                totalDefective++;
                if (trace) {
                    System.out.println("Продукт " + product.getId() + " бракован после второго дефекта.");
                }
                return;
            }
            eventQueue.add(new Event(currentTime, Event.START_ADJUSTMENT, product));
        } else {
            product.moveToNextStation();
            if (product.isFullyProcessed()) {
                if (trace) {
                    System.out.println("Продукт " + product.getId() + " успешно обработан.");
                }
            } else {
                eventQueue.add(new Event(currentTime, Event.START_PROCESSING, product));
            }
        }
    }

    private void startAdjustment(Product product, double currentTime) {
        if (adjustmentStation.getNextFreeTime() - currentTime > 0.00001) {
            if (!product.isInAdjQueue()) {
                currentQueueLength++;
                numOfAdjQue.put(currentTime, currentQueueLength);
                updateQueMetric(currentTime);
                previousTimeOfChangingQue = currentTime;
                product.setInAdjQueue(true);
            }
            eventQueue.add(new Event(adjustmentStation.getNextFreeTime() + 0.00001, Event.START_ADJUSTMENT, product));

        } else {
            if (currentQueueLength != 0) {
                currentQueueLength--;
                numOfAdjQue.put(currentTime, currentQueueLength);
                updateQueMetric(currentTime);
                previousTimeOfChangingQue = currentTime;
            }
            product.setInAdjQueue(false);
            double adjustmentTime = adjustmentStation.getAdjustmentTime(generator);
            if (trace) {
                System.out.println("Начало наладки продукта " + product.getId() + " текущее время " + currentTime + " рассчетное время наладки " + adjustmentTime);
            }
            previousTimeOfChangingAdj = currentTime;
            eventQueue.add(new Event(currentTime + adjustmentTime, Event.END_ADJUSTMENT, product));
            adjustmentStation.setNextFreeTime(currentTime + adjustmentTime);
        }
    }

    private void endAdjustment(Product product, double currentTime) {
        if (trace) {
            System.out.println("Окончание наладки продукта " + product.getId() + " текущее время " + currentTime);
        }
        product.moveToFirstStation();
        updateAdjMetric(currentTime);
        eventQueue.add(new Event(currentTime, Event.START_PROCESSING, product));
    }

    private void updateAdjMetric(double currentTime) {
        totalAdjustmentBusyTime += (currentTime - previousTimeOfChangingAdj);
    }

    private void updateQueMetric(double currentTime) {
        totalQueueLengthTime += (currentQueueLength * (currentTime - previousTimeOfChangingQue));
        averQueSizes.put(currentTime,totalQueueLengthTime/currentTime);
    }

    private void calculateMetrics(double currentTime) {

        System.out.println("Количество изделий системы: " + totalProcessed);
        totalProcesseds.add(totalProcessed);

        double averageTimeInSystem = totalTimeInSystem / totalProcessed;
        System.out.println("Среднее время пребывания изделия в системе: " + averageTimeInSystem);
        averageTimesInSystem.add(averageTimeInSystem);

        double adjustmentUtilization = totalAdjustmentBusyTime / currentTime;
        System.out.println("Загруженность станций наладки: " + adjustmentUtilization);
        adjustmentUtilizations.add(adjustmentUtilization);

        double averageQueueLength = totalQueueLengthTime / currentTime;
        System.out.println("Среднее количество изделий в очереди на наладку: " + averageQueueLength);
        averageQueueLengths.add(averageQueueLength);

        double defectivePercentage = ((double) totalDefective / totalProcessed) * 100;
        System.out.println("Процент отбракованных изделий: " + defectivePercentage + "%");
        defectivePercentages.add(defectivePercentage);

        double repairedPercentage = ((double) (totalFirstRepaire - totalDefective*2) / totalProcessed) * 100;
        System.out.println("Процент налаженных изделий после брака: " + repairedPercentage + "%");
        repairedPercentages.add(repairedPercentage);

        Frame example = new Frame(numOfAdjQue, averQueSizes);
        example.displayChart();
    }

    public List<Double> getRepairedPercentages() {
        return repairedPercentages;
    }

    public List<Double> getDefectivePercentages() {
        return defectivePercentages;
    }

    public List<Double> getAverageQueueLengths() {
        return averageQueueLengths;
    }

    public List<Double> getAdjustmentUtilizations() {
        return adjustmentUtilizations;
    }

    public List<Integer> getTotalProcesseds() {
        return totalProcesseds;
    }

    public List<Double> getAverageTimesInSystem() {
        return averageTimesInSystem;
    }
}