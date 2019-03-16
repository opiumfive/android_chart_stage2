package com.opiumfive.telechart.chart.formatter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.opiumfive.telechart.chart.util.FloatUtils;

import static com.opiumfive.telechart.GlobalConst.BOTTOM_LABEL_DATE_FORMAT;

public class ValueFormatterHelper {

    private char decimalSeparator = '.';
    private SimpleDateFormat dateFormat = new SimpleDateFormat(BOTTOM_LABEL_DATE_FORMAT, Locale.ENGLISH);

    public int formatFloatValueWithPrependedAndAppendedText(char[] formattedValue, float value) {
        return formatFloatValue(formattedValue, value);
    }

    public int formatIntValueToDate(char[] formattedValue, float value) {
        String date = dateFormat.format((long)value);
        System.arraycopy(date.toCharArray(), 0, formattedValue, formattedValue.length - date.length(), date.length());
        return date.length();
    }

    public int formatFloatValue(char[] formattedValue, float value) {
        return FloatUtils.formatFloat(formattedValue, value, formattedValue.length);
    }
}
