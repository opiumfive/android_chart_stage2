package com.opiumfive.telechart.chart.valueFormat;

import com.opiumfive.telechart.chart.util.FloatUtils;

public class DefaultAxisValueFormatter implements AxisValueFormatter {

    @Override
    public int formatValue(char[] formattedValue, float value) {
        return FloatUtils.formatFloat(formattedValue, value, formattedValue.length);
    }
}
