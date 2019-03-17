package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.model.LineChartData;

public interface ChartDataProvider {
    LineChartData getChartData();
    void setChartData(LineChartData data);
}
