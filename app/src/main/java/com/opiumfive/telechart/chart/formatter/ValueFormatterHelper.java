package com.opiumfive.telechart.chart.formatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.opiumfive.telechart.chart.util.FloatUtils;

public class ValueFormatterHelper {

    public static final int DEFAULT_DIGITS_NUMBER = 0;

    private int decimalDigitsNumber = Integer.MIN_VALUE;
    private char[] appendedText = new char[0];
    private char[] prependedText = new char[0];
    private char decimalSeparator = '.';

    public void determineDecimalSeparator() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        if (numberFormat instanceof DecimalFormat) {
            decimalSeparator = ((DecimalFormat) numberFormat).getDecimalFormatSymbols().getDecimalSeparator();
        }
    }

    public int getDecimalDigitsNumber() {
        return decimalDigitsNumber;
    }

    public ValueFormatterHelper setDecimalDigitsNumber(int decimalDigitsNumber) {
        this.decimalDigitsNumber = decimalDigitsNumber;
        return this;
    }

    public char[] getAppendedText() {
        return appendedText;
    }

    public ValueFormatterHelper setAppendedText(char[] appendedText) {
        if (null != appendedText) {
            this.appendedText = appendedText;
        }
        return this;
    }

    public char[] getPrependedText() {
        return prependedText;
    }

    public ValueFormatterHelper setPrependedText(char[] prependedText) {
        if (null != prependedText) {
            this.prependedText = prependedText;
        }
        return this;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public ValueFormatterHelper setDecimalSeparator(char decimalSeparator) {
        char nullChar = '\0';
        if (nullChar != decimalSeparator) {
            this.decimalSeparator = decimalSeparator;
        }
        return this;
    }

    public int formatFloatValueWithPrependedAndAppendedText(char[] formattedValue, float value, int
            defaultDigitsNumber, char[] label) {
        if (null != label) {
            int labelLength = label.length;
            if (labelLength > formattedValue.length) {
                labelLength = formattedValue.length;
            }
            System.arraycopy(label, 0, formattedValue, formattedValue.length - labelLength, labelLength);
            return labelLength;
        }

        final int appliedDigitsNumber = getAppliedDecimalDigitsNumber(defaultDigitsNumber);
        final int charsNumber = formatFloatValue(formattedValue, value, appliedDigitsNumber);
        appendText(formattedValue);
        prependText(formattedValue, charsNumber);
        return charsNumber + getPrependedText().length + getAppendedText().length;
    }

    public int formatFloatValueWithPrependedAndAppendedText(char[] formattedValue, float value, char[] label) {
        return formatFloatValueWithPrependedAndAppendedText(formattedValue, value, DEFAULT_DIGITS_NUMBER, label);
    }

    public int formatFloatValueWithPrependedAndAppendedText(char[] formattedValue, float value, int
            defaultDigitsNumber) {
        return formatFloatValueWithPrependedAndAppendedText(formattedValue, value, defaultDigitsNumber, null);
    }

    public int formatFloatValue(char[] formattedValue, float value, int decimalDigitsNumber) {
        return FloatUtils.formatFloat(formattedValue, value, formattedValue.length - appendedText.length, decimalDigitsNumber, decimalSeparator);
    }

    public void appendText(char[] formattedValue) {
        if (appendedText.length > 0) {
            System.arraycopy(appendedText, 0, formattedValue, formattedValue.length - appendedText.length, appendedText.length);
        }
    }

    public void prependText(char[] formattedValue, int charsNumber) {
        if (prependedText.length > 0) {
            System.arraycopy(prependedText, 0, formattedValue, formattedValue.length - charsNumber - appendedText.length - prependedText.length, prependedText.length);
        }
    }

    public int getAppliedDecimalDigitsNumber(int defaultDigitsNumber) {
        final int appliedDecimalDigitsNumber;
        if (decimalDigitsNumber < 0) {
            appliedDecimalDigitsNumber = defaultDigitsNumber;
        } else {
            appliedDecimalDigitsNumber = decimalDigitsNumber;
        }
        return appliedDecimalDigitsNumber;
    }

}
