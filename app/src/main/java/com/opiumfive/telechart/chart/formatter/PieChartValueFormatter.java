package com.opiumfive.telechart.chart.formatter;

import com.opiumfive.telechart.chart.model.SliceValue;

public interface PieChartValueFormatter {

    public int formatChartValue(char[] formattedValue, SliceValue value);
}
