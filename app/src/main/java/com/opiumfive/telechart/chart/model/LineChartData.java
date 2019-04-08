package com.opiumfive.telechart.chart.model;

import android.graphics.Color;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;


public class LineChartData {

    public static final int DEFAULT_TEXT_SIZE_SP = 14;

    protected Axis axisXBottom;
    protected Axis axisYLeft;


    protected int valueLabelTextColor = Color.WHITE;
    protected int valueLabelTextSize = DEFAULT_TEXT_SIZE_SP;
    protected Typeface valueLabelTypeface;

    private List<Line> lines = new ArrayList<>();

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

    public List<Line> getLines() {
        return lines;
    }

    public Bounds getBoundsForViewrect(Viewrect viewrect) {
        int right = binarySearchNearest(viewrect.right);
        if (right < lines.get(0).getValues().size() - 1) right++;
        return new Bounds(binarySearchNearest(viewrect.left), right);
    }

    private int binarySearchNearest(float value) {
        List<PointValue> a = lines.get(0).getValues();

        if(value < a.get(0).getX()) {
            return 0;
        }

        if (value > a.get(a.size() - 1).getX()) {
            return a.size() - 1;
        }

        int lo = 0;
        int hi = a.size() - 1;

        while (lo <= hi) {
            int mid = (hi + lo) / 2;

            if (value < a.get(mid).getX()) {
                hi = mid - 1;
            } else if (value > a.get(mid).getX()) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        // lo == hi + 1
        return (a.get(lo).getX() - value) < (value - a.get(hi).getX()) ? lo : hi;
    }

    public LineChartData setLines(List<Line> lines) {
        if (null == lines) {
            this.lines = new ArrayList<>();
        } else {
            this.lines = lines;
        }
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

    public static class Bounds {

        public Bounds(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int from;
        public int to;
    }
}
