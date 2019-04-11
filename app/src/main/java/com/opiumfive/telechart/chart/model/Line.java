package com.opiumfive.telechart.chart.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.opiumfive.telechart.chart.Util;


public class Line {

    private static final float DEFAULT_LINE_STROKE_WIDTH_DP = 1.2f;
    private static final int DEFAULT_POINT_RADIUS_DP = 1;
    public static final int UNINITIALIZED = 0;

    private int color = Util.DEFAULT_COLOR;
    private int pointColor = UNINITIALIZED;
    private boolean isActive = true;
    private String title;
    private float alpha = 1f;
    private float offset = 0f;
    private float scale = 1f;
    private float minY = 0f;

    private float strokeWidth = DEFAULT_LINE_STROKE_WIDTH_DP;
    private int pointRadius = DEFAULT_POINT_RADIUS_DP;

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

        for (PointValue pointValue : line.values) {
            this.values.add(new PointValue(pointValue));
        }
    }

    public List<PointValue> getValues() {
        return this.values;
    }

    public void setValues(List<PointValue> values) {
        if (null == values) {
            this.values = new ArrayList<>();
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

    public float getStrokeWidth() {
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

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }
}
