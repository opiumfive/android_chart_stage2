package com.opiumfive.telechart.chart.valueFormat;

import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.model.PointValue;

public class SimpleLineChartValueFormatter implements LineChartValueFormatter {

    @Override
    public int formatChartValue(char[] formattedValue, PointValue value) {
        return Util.formatFloat(formattedValue, value.getY(), formattedValue.length);
    }
}
