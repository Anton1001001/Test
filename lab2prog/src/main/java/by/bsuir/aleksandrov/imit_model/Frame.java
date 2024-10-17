package by.bsuir.aleksandrov.imit_model;

import org.knowm.xchart.*;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class Frame extends JFrame {
    private final Map<Double, Integer> numOfAdjQue;
    private final Map<Double, Double> averQueSizes;

    public Frame(Map<Double, Integer> numOfAdjQue, Map<Double, Double> averQueSizes) {
        this.numOfAdjQue = numOfAdjQue;
        this.averQueSizes = averQueSizes;
    }

    public void displayChart() {
        // Создание графика
        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Зависимости")
                .xAxisTitle("Временная метка")
                .yAxisTitle("Значение")
                .build();

        // Добавление данных для numOfAdjQue
        double[] x1 = numOfAdjQue.keySet().stream().mapToDouble(Double::doubleValue).sorted().toArray();
        double[] y1 = numOfAdjQue.values().stream().mapToDouble(Integer::doubleValue).toArray();
        chart.addSeries("Количество в очереди", x1, y1);

        // Добавление данных для averQueSizes
        double[] x2 = averQueSizes.keySet().stream().mapToDouble(Double::doubleValue).sorted().toArray();
        double[] y2 = averQueSizes.values().stream().mapToDouble(Double::doubleValue).toArray();
        chart.addSeries("Средний размер очереди", x2, y2);

        // Настройка стиля графика
        //chart.getStyleManager().setChartType(StyleManager.ChartType.Line);
        //chart.getStyleManager().setLegendPosition(StyleManager.LegendPosition.InsideNE);

        // Отображение графика
        new SwingWrapper<>(chart).displayChart();
    }
}
