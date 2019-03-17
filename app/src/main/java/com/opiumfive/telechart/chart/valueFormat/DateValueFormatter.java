package com.opiumfive.telechart.chart.valueFormat;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.opiumfive.telechart.GlobalConst.BOTTOM_LABEL_DATE_FORMAT;

public class DateValueFormatter implements AxisValueFormatter {

    private SimpleDateFormat dateFormat = new SimpleDateFormat(BOTTOM_LABEL_DATE_FORMAT, Locale.ENGLISH);

    @Override
    public int formatValue(char[] formattedValue, float value) {
        String date = dateFormat.format((long)value);
        System.arraycopy(date.toCharArray(), 0, formattedValue, formattedValue.length - date.length(), date.length());
        return date.length();
    }
}