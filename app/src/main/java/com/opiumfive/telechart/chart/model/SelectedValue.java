package com.opiumfive.telechart.chart.model;


import java.util.ArrayList;
import java.util.List;

public class SelectedValue {

    private List<PointValue> points = new ArrayList<>();
    private float touchX;

    public SelectedValue() {
    }

    public void add(PointValue pointValue) {
        points.add(pointValue);
    }

    public void set(SelectedValue selectedValue) {
        points.clear();
        points.addAll(selectedValue.points);
        touchX = selectedValue.touchX;
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
