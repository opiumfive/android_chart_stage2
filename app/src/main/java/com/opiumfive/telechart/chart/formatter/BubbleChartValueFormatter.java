package com.opiumfive.telechart.chart.formatter;

import com.opiumfive.telechart.chart.model.BubbleValue;

public interface BubbleChartValueFormatter {

    public int formatChartValue(char[] formattedValue, BubbleValue value);
}
