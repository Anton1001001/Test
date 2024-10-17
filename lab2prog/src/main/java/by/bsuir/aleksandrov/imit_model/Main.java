package by.bsuir.aleksandrov.imit_model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream("src/main/java/by/bsuir/aleksandrov/imit_model/config.properties");
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error while reading file!");
            return;
        }

        ControlStation[] stations = {
                new ControlStation(
                        Double.parseDouble(properties.getProperty("station1.minProcessTime")),
                        Double.parseDouble(properties.getProperty("station1.maxProcessTime")),
                        Double.parseDouble(properties.getProperty("station1.adjustmentProbability"))
                ),
                new ControlStation(
                        Double.parseDouble(properties.getProperty("station2.minProcessTime")),
                        Double.parseDouble(properties.getProperty("station2.maxProcessTime")),
                        Double.parseDouble(properties.getProperty("station2.adjustmentProbability"))
                ),
                new ControlStation(
                        Double.parseDouble(properties.getProperty("station3.minProcessTime")),
                        Double.parseDouble(properties.getProperty("station3.maxProcessTime")),
                        Double.parseDouble(properties.getProperty("station3.adjustmentProbability"))
                )
        };

        AdjustmentStation adjustmentStation = new AdjustmentStation(
                Double.parseDouble(properties.getProperty("adjustmentStation.meanAdjustmentTime")),
                Double.parseDouble(properties.getProperty("adjustmentStation.varianceAdjustmentTime"))
        );

        double meanArrivalTime = Double.parseDouble(properties.getProperty("meanArrivalTime"));
        double varianceArrivalTime = Double.parseDouble(properties.getProperty("varianceArrivalTime"));
        double simulationEndTime = Double.parseDouble(properties.getProperty("simulationEndTime"));

        boolean trace = Boolean.parseBoolean(properties.getProperty("trace"));
        int simulationNumber = Integer.parseInt(properties.getProperty("simulationNumber"));
        Simulation simulator = new Simulation(stations, adjustmentStation, meanArrivalTime, varianceArrivalTime, simulationEndTime, trace);
        for (int i=0; i<simulationNumber; i++) {
                System.out.println("\nПрогон №" + i + "\n");
            simulator.startSimulation();
        }
        printSummaryTable(simulationNumber, simulator);
    }

    private static void printSummaryTable(int simulationNumber, Simulation simulation) {
        System.out.println("\nСводная таблица по результатам всех прогонов:\n");

        System.out.printf("%-10s %-20s %-15s %-20s %-20s %-20s %-20s\n",
                "Прогон",
                "Сред. время в системе",
                "Кол-во обработано",
                "Загруж. наладки",
                "Сред. длина очереди",
                "Отбраковано (%)",
                "Налажено (%)");

        System.out.println("------------------------------------------------------------------------------------------" +
                "-----------------------------------------------");

        for (int i = 0; i < simulationNumber; i++) {
            System.out.printf("%-10d %-20.2f %-15d %-20.2f %-20.2f %-20.2f %-20.2f\n",
                    (i + 1),
                    simulation.getAverageTimesInSystem().get(i),
                    simulation.getTotalProcesseds().get(i),
                    simulation.getAdjustmentUtilizations().get(i),
                    simulation.getAverageQueueLengths().get(i),
                    simulation.getDefectivePercentages().get(i),
                    simulation.getRepairedPercentages().get(i));
        }

        double avgTimeInSystem = simulation.getAverageTimesInSystem().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgProcessed = simulation.getTotalProcesseds().stream().mapToInt(Integer::intValue).average().orElse(0);
        double avgAdjustmentUtilization = simulation.getAdjustmentUtilizations().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgQueueLength = simulation.getAverageQueueLengths().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgDefectivePercentage = simulation.getDefectivePercentages().stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgRepairedPercentage = simulation.getRepairedPercentages().stream().mapToDouble(Double::doubleValue).average().orElse(0);

        System.out.println("------------------------------------------------------------------------------------------" +
                "-----------------------------------------------");
        System.out.printf("%-10s %-20.2f %-15.2f %-20.2f %-20.2f %-20.2f %-20.2f\n",
                "Среднее",
                avgTimeInSystem,
                avgProcessed,
                avgAdjustmentUtilization,
                avgQueueLength,
                avgDefectivePercentage,
                avgRepairedPercentage);
    }


}
