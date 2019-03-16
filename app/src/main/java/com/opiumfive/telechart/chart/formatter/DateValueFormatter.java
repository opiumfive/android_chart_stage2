package com.opiumfive.telechart.chart.formatter;

public class DateValueFormatter implements AxisValueFormatter {

    private ValueFormatterHelper valueFormatterHelper = new ValueFormatterHelper();

    @Override
    public int formatValue(char[] formattedValue, float value) {
        return valueFormatterHelper.formatIntValueToDate(formattedValue, value);
    }
}