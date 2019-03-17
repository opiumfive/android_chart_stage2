package com.opiumfive.telechart.chart.model;

import android.graphics.Color;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;


public class LineChartData {

    public static final float DEFAULT_BASE_VALUE = 0.0f;
    public static final int DEFAULT_TEXT_SIZE_SP = 12;

    protected Axis axisXBottom;
    protected Axis axisYLeft;


    protected int valueLabelTextColor = Color.WHITE;
    protected int valueLabelTextSize = DEFAULT_TEXT_SIZE_SP;
    protected Typeface valueLabelTypeface;

    protected boolean isValueLabelBackgroundEnabled = true;
    protected boolean isValueLabelBackgrountAuto = true;
    protected int valueLabelBackgroundColor;

    private List<Line> lines = new ArrayList<Line>();
    private float baseValue = DEFAULT_BASE_VALUE;

    public LineChartData() {

    }

    public LineChartData(List<Line> lines) {
        setLines(lines);
    }

    public LineChartData(LineChartData data) {
        if (null != data.axisXBottom) {
            this.axisXBottom = new Axis(data.axisXBottom);
        }
        if (null != data.axisYLeft) {
            this.axisYLeft = new Axis(data.axisYLeft);
        }
        this.valueLabelTextColor = data.valueLabelTextColor;
        this.valueLabelTextSize = data.valueLabelTextSize;
        this.valueLabelTypeface = data.valueLabelTypeface;
        this.baseValue = data.baseValue;
        this.lines = data.lines;
    }

    public static LineChartData generateDummyData() {
        final int numValues = 4;
        LineChartData data = new LineChartData();
        List<PointValue> values = new ArrayList<PointValue>(numValues);
        values.add(new PointValue(0, 2));
        values.add(new PointValue(1, 4));
        values.add(new PointValue(2, 3));
        values.add(new PointValue(3, 4));
        Line line = new Line(values);
        List<Line> lines = new ArrayList<Line>(1);
        lines.add(line);
        data.setLines(lines);
        return data;
    }

    public void update(float scale) {
        for (Line line : lines) {
            line.update(scale);
        }
    }

    public void finish() {
        for (Line line : lines) {
            line.finish();
        }
    }

    public List<Line> getLines() {
        return lines;
    }

    public LineChartData setLines(List<Line> lines) {
        if (null == lines) {
            this.lines = new ArrayList<Line>();
        } else {
            this.lines = lines;
        }
        return this;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public LineChartData setBaseValue(float baseValue) {
        this.baseValue = baseValue;
        return this;
    }

    public Axis getAxisXBottom() {
        return axisXBottom;
    }

    public void setAxisXBottom(Axis axisX) {
        this.axisXBottom = axisX;
    }

    public Axis getAxisYLeft() {
        return axisYLeft;
    }

    public void setAxisYLeft(Axis axisY) {
        this.axisYLeft = axisY;
    }

    public int getValueLabelTextColor() {
        return valueLabelTextColor;
    }

    public void setValueLabelsTextColor(int valueLabelTextColor) {
        this.valueLabelTextColor = valueLabelTextColor;
    }

    public int getValueLabelTextSize() {
        return valueLabelTextSize;
    }

    public void setValueLabelTextSize(int valueLabelTextSize) {
        this.valueLabelTextSize = valueLabelTextSize;
    }

    public Typeface getValueLabelTypeface() {
        return valueLabelTypeface;
    }

    public void setValueLabelTypeface(Typeface typeface) {
        this.valueLabelTypeface = typeface;
    }

    public boolean isValueLabelBackgroundEnabled() {
        return isValueLabelBackgroundEnabled;
    }

    public void setValueLabelBackgroundEnabled(boolean isValueLabelBackgroundEnabled) {
        this.isValueLabelBackgroundEnabled = isValueLabelBackgroundEnabled;
    }

    public boolean isValueLabelBackgroundAuto() {
        return isValueLabelBackgrountAuto;
    }

    public void setValueLabelBackgroundAuto(boolean isValueLabelBackgrountAuto) {
        this.isValueLabelBackgrountAuto = isValueLabelBackgrountAuto;
    }

    public int getValueLabelBackgroundColor() {
        return valueLabelBackgroundColor;
    }

    public void setValueLabelBackgroundColor(int valueLabelBackgroundColor) {
        this.valueLabelBackgroundColor = valueLabelBackgroundColor;
    }
}
