package com.opiumfive.telechart.chart.formatter;


import com.opiumfive.telechart.chart.model.PointValue;

public interface LineChartValueFormatter {
    public int formatChartValue(char[] formattedValue, PointValue value);
}
