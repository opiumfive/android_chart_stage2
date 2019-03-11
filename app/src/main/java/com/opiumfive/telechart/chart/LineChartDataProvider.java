package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.model.LineChartData;

public interface LineChartDataProvider {
    public LineChartData getLineChartData();
    public void setLineChartData(LineChartData data);
}
