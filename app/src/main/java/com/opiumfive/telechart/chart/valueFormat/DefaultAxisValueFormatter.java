package com.opiumfive.telechart.chart.valueFormat;

public class DefaultAxisValueFormatter implements AxisValueFormatter {

    private ValueFormatterHelper valueFormatterHelper = new ValueFormatterHelper();

    @Override
    public int formatValue(char[] formattedValue, float value) {
        return valueFormatterHelper.formatFloatValueWithPrependedAndAppendedText(formattedValue, value);
    }
}
