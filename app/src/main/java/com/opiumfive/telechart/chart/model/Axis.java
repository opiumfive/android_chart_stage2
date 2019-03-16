package com.opiumfive.telechart.chart.model;

import android.graphics.Color;

import com.opiumfive.telechart.chart.formatter.AxisValueFormatter;
import com.opiumfive.telechart.chart.formatter.DefaultAxisValueFormatter;
import com.opiumfive.telechart.chart.util.ChartUtils;

public class Axis {

    public static final int DEFAULT_TEXT_SIZE_SP = 12;
    public static final int DEFAULT_MAX_AXIS_LABEL_CHARS = 3;

    private int textSize = DEFAULT_TEXT_SIZE_SP;
    private int maxLabelChars = DEFAULT_MAX_AXIS_LABEL_CHARS;
    private String name;
    private boolean hasLines = false;
    private boolean isInside = true;
    private int textColor = Color.LTGRAY;
    private int lineColor = ChartUtils.DEFAULT_DARKEN_COLOR;
    private AxisValueFormatter formatter = new DefaultAxisValueFormatter();

    public Axis() {
    }

    public Axis(Axis axis) {
        this.name = axis.name;
        this.hasLines = axis.hasLines;
        this.isInside = axis.isInside;
        this.textColor = axis.textColor;
        this.lineColor = axis.lineColor;
        this.textSize = axis.textSize;
        this.maxLabelChars = axis.maxLabelChars;
        this.formatter = axis.formatter;
    }

    public boolean hasLines() {
        return hasLines;
    }

    public Axis setHasLines(boolean hasLines) {
        this.hasLines = hasLines;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public Axis setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public boolean isInside() {
        return isInside;
    }

    public Axis setInside(boolean isInside) {
        this.isInside = isInside;
        return this;
    }

    public int getLineColor() {
        return lineColor;
    }

    public Axis setLineColor(int lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public Axis setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public AxisValueFormatter getFormatter() {
        return formatter;
    }

    public Axis setFormatter(AxisValueFormatter formatter) {
        if (null == formatter) {
            this.formatter = new DefaultAxisValueFormatter();
        } else {
            this.formatter = formatter;
        }
        return this;
    }
}
