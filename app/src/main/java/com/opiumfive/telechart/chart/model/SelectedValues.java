package com.opiumfive.telechart.chart.model;


import java.util.ArrayList;
import java.util.List;

public class SelectedValues {

    private List<PointValue> points = new ArrayList<>();
    private float touchX;

    public SelectedValues() {
    }

    public void add(PointValue pointValue) {
        points.add(pointValue);
    }

    public void set(SelectedValues selectedValues) {
        points.clear();
        points.addAll(selectedValues.points);
        touchX = selectedValues.touchX;
    }

    public void clear() {
        points.clear();
        touchX = -1f;
    }

    public boolean isSet() {
        if (points.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public List<PointValue> getPoints() {
        return points;
    }

    public float getTouchX() {
        return touchX;
    }

    public void setTouchX(float touchX) {
        this.touchX = touchX;
    }
}
