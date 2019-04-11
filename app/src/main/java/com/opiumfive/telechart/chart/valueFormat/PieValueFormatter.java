package com.opiumfive.telechart.chart.valueFormat;


public class PieValueFormatter implements AxisValueFormatter {

    @Override
    public int formatValue(char[] formattedValue, float value) {
        String perc = String.valueOf((long)value) + "%";
        System.arraycopy(perc.toCharArray(), 0, formattedValue, formattedValue.length - perc.length(), perc.length());
        return perc.length();
    }
}
