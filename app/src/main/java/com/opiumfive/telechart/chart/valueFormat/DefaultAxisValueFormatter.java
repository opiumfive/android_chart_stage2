package com.opiumfive.telechart.chart.valueFormat;


import com.opiumfive.telechart.chart.Util;

public class DefaultAxisValueFormatter implements AxisValueFormatter {

    @Override
    public int formatValue(char[] formattedValue, float value) {
        return Util.formatFloat(formattedValue, value, formattedValue.length);
    }
}
