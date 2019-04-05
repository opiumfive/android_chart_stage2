package com.opiumfive.telechart.data;

import android.graphics.Color;

import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.draw.LinePathOptimizer;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;

import java.util.ArrayList;
import java.util.List;

public class DataMapper {

    public static LineChartData mapFromPlainData(ChartData chartData) {
        List<List<PointValue>> plotsValuesList = new ArrayList<>(chartData.getColumns().size() - 1);

        ColumnData xValuesData = chartData.getColumns().get(0);
        for (int columnIndex = 1; columnIndex < chartData.getColumns().size(); columnIndex++) {
            ColumnData yValuesData = chartData.getColumns().get(columnIndex);
            List<PointValue> values = new ArrayList<>(yValuesData.getList().size());

            for (int i = 0; i < chartData.getColumns().get(0).getList().size(); i++) {
                values.add(new PointValue(xValuesData.getList().get(i), yValuesData.getList().get(i)));
            }

            plotsValuesList.add(values);
        }

        final List<Line> lines = new ArrayList<>(chartData.getColumns().size() - 1);
        for (int columnIndex = 1; columnIndex < chartData.getColumns().size(); columnIndex++) {
            Line line = new Line(plotsValuesList.get(columnIndex - 1));
            String lineId = chartData.getColumns().get(columnIndex).getTitle();
            line.setColor(Color.parseColor(chartData.getColors().get(lineId)));
            line.setTitle(chartData.getNames().get(lineId));
            lines.add(line);
        }

        return optimizeData(new LineChartData(lines));
    }

    private static LineChartData optimizeData(LineChartData data) {
        LinePathOptimizer linePathOptimizer = new LinePathOptimizer();
        for (Line line : data.getLines()) {
            line.setValues(linePathOptimizer.optimizeLine(line.getValues(), 1f));
        }
        return data;
    }
}
