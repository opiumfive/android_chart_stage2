package com.opiumfive.telechart.chart.valueFormat;

import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.util.FloatUtils;

public class SimpleLineChartValueFormatter implements LineChartValueFormatter {

    @Override
    public int formatChartValue(char[] formattedValue, PointValue value) {
        return FloatUtils.formatFloat(formattedValue, value.getY(), formattedValue.length);
    }
}
