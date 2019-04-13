package com.opiumfive.telechart.chart.model;


public class PointValue {

    private float x;
    private float y;
    private float originX;
    private float originY;
    private float diffX;
    private float diffY;
    private int color;
    private int pointRadius;
    private String line;
    private float approxY;

    public PointValue() {
        set(0, 0);
    }

    public PointValue(float x, float y) {
        set(x, y);
    }

    public PointValue(PointValue pointValue) {
        set(pointValue.x, pointValue.y);
    }

    public PointValue set(float x, float y) {
        this.x = x;
        this.y = y;
        this.originX = x;
        this.originY = y;
        this.diffX = 0;
        this.diffY = 0;
        return this;
    }

    public void setOriginY(float originY) {
        this.originY = originY;
    }

    public float getOriginY() {
        return originY;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return this.x;
    }

    public float getApproxY() {
        return approxY;
    }

    public void setApproxY(float approxY) {
        this.approxY = approxY;
    }

    public float getY() {
        return this.y;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public void setPointRadius(int pointRadius) {
        this.pointRadius = pointRadius;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointValue that = (PointValue) o;

        if (Float.compare(that.diffX, diffX) != 0) return false;
        if (Float.compare(that.diffY, diffY) != 0) return false;
        if (Float.compare(that.originX, originX) != 0) return false;
        if (Float.compare(that.originY, originY) != 0) return false;
        if (Float.compare(that.x, x) != 0) return false;
        if (Float.compare(that.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (originX != +0.0f ? Float.floatToIntBits(originX) : 0);
        result = 31 * result + (originY != +0.0f ? Float.floatToIntBits(originY) : 0);
        result = 31 * result + (diffX != +0.0f ? Float.floatToIntBits(diffX) : 0);
        result = 31 * result + (diffY != +0.0f ? Float.floatToIntBits(diffY) : 0);
        return result;
    }
}
