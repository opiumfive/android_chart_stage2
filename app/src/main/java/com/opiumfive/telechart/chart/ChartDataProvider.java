package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.model.LineChartData;

public interface ChartDataProvider {
    public LineChartData getChartData();
    public void setChartData(LineChartData data);
}
