package com.opiumfive.telechart.chart.formatter;

import com.opiumfive.telechart.chart.model.SubcolumnValue;

public interface ColumnChartValueFormatter {

    public int formatChartValue(char[] formattedValue, SubcolumnValue value);

}
