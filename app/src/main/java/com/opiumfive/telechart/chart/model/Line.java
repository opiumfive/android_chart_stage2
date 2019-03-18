package com.opiumfive.telechart.chart.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.valueFormat.LineChartValueFormatter;
import com.opiumfive.telechart.chart.valueFormat.SimpleLineChartValueFormatter;


public class Line {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 1;
    private static final int DEFAULT_POINT_RADIUS_DP = 1;
    public static final int UNINITIALIZED = 0;

    private int color = Util.DEFAULT_COLOR;
    private int pointColor = UNINITIALIZED;
    private boolean isActive = true;
    private String title;
    private float alpha = 1f;

    private int strokeWidth = DEFAULT_LINE_STROKE_WIDTH_DP;
    private int pointRadius = DEFAULT_POINT_RADIUS_DP;

    private LineChartValueFormatter formatter = new SimpleLineChartValueFormatter();
    private List<PointValue> values = new ArrayList<>();
    private String id = UUID.randomUUID().toString();

    public Line() {
    }

    public Line(List<PointValue> values) {
        setValues(values);
    }

    public Line(Line line) {
        this.color = line.color;
        this.pointColor = line.pointColor;
        this.strokeWidth = line.strokeWidth;
        this.pointRadius = line.pointRadius;
        this.formatter = line.formatter;

        for (PointValue pointValue : line.values) {
            this.values.add(new PointValue(pointValue));
        }
    }

    public void update(float scale) {
        for (PointValue value : values) {
            value.update(scale);
        }
    }

    public void finish() {
        for (PointValue value : values) {
            value.finish();
        }
    }

    public List<PointValue> getValues() {
        return this.values;
    }

    public void setValues(List<PointValue> values) {
        if (null == values) {
            this.values = new ArrayList<PointValue>();
        } else {
            this.values = values;
        }
    }

    public int getColor() {
        return color;
    }

    public Line setColor(int color) {
        this.color = color;
        return this;
    }

    public int getPointColor() {
        if (pointColor == UNINITIALIZED) {
            return color;
        } else {
            return pointColor;
        }
    }

    public Line setPointColor(int pointColor) {
        this.pointColor = pointColor;
        return this;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Line setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public Line setPointRadius(int pointRadius) {
        this.pointRadius = pointRadius;
        return this;
    }

    public LineChartValueFormatter getFormatter() {
        return formatter;
    }

    public Line setFormatter(LineChartValueFormatter formatter) {
        if (null != formatter) {
            this.formatter = formatter;
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
