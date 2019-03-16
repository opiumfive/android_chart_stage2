package com.opiumfive.telechart.chart.formatter;

import com.opiumfive.telechart.chart.model.PointValue;

public class SimpleLineChartValueFormatter implements LineChartValueFormatter {

    private ValueFormatterHelper valueFormatterHelper = new ValueFormatterHelper();

    @Override
    public int formatChartValue(char[] formattedValue, PointValue value) {
        return valueFormatterHelper.formatFloatValueWithPrependedAndAppendedText(formattedValue, value.getY());
    }
}
