package com.opiumfive.telechart.chart.model;

import android.graphics.PathEffect;

import java.util.ArrayList;
import java.util.List;

import com.opiumfive.telechart.chart.formatter.LineChartValueFormatter;
import com.opiumfive.telechart.chart.formatter.SimpleLineChartValueFormatter;
import com.opiumfive.telechart.chart.util.ChartUtils;


public class Line {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 3;
    private static final int DEFAULT_POINT_RADIUS_DP = 6;
    public static final int UNINITIALIZED = 0;

    private int color = ChartUtils.DEFAULT_COLOR;
    private int pointColor = UNINITIALIZED;
    private int darkenColor = ChartUtils.DEFAULT_DARKEN_COLOR;

    private int strokeWidth = DEFAULT_LINE_STROKE_WIDTH_DP;
    private int pointRadius = DEFAULT_POINT_RADIUS_DP;

    private boolean hasPoints = true;
    private boolean hasLines = true;
    private boolean hasLabels = false;
    private boolean hasLabelsOnlyForSelected = false;

    private ValueShape shape = ValueShape.CIRCLE;
    private PathEffect pathEffect;
    private LineChartValueFormatter formatter = new SimpleLineChartValueFormatter();
    private List<PointValue> values = new ArrayList<>();

    public Line() {

    }

    public Line(List<PointValue> values) {
        setValues(values);
    }

    public Line(Line line) {
        this.color = line.color;
        this.pointColor = line.pointColor;
        this.darkenColor = line.darkenColor;
        this.strokeWidth = line.strokeWidth;
        this.pointRadius = line.pointRadius;
        this.hasPoints = line.hasPoints;
        this.hasLines = line.hasLines;
        this.hasLabels = line.hasLabels;
        this.hasLabelsOnlyForSelected = line.hasLabelsOnlyForSelected;
        this.shape = line.shape;
        this.pathEffect = line.pathEffect;
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
        if (pointColor == UNINITIALIZED) {
            this.darkenColor = ChartUtils.darkenColor(color);
        }
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
        if (pointColor == UNINITIALIZED) {
            this.darkenColor = ChartUtils.darkenColor(color);
        } else {
            this.darkenColor = ChartUtils.darkenColor(pointColor);
        }
        return this;
    }

    public int getDarkenColor() {
        return darkenColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Line setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public boolean hasPoints() {
        return hasPoints;
    }

    public Line setHasPoints(boolean hasPoints) {
        this.hasPoints = hasPoints;
        return this;
    }

    public boolean hasLines() {
        return hasLines;
    }

    public Line setHasLines(boolean hasLines) {
        this.hasLines = hasLines;
        return this;
    }

    public boolean hasLabels() {
        return hasLabels;
    }

    public Line setHasLabels(boolean hasLabels) {
        this.hasLabels = hasLabels;
        if (hasLabels) {
            this.hasLabelsOnlyForSelected = false;
        }
        return this;
    }

    public boolean hasLabelsOnlyForSelected() {
        return hasLabelsOnlyForSelected;
    }

    public Line setHasLabelsOnlyForSelected(boolean hasLabelsOnlyForSelected) {
        this.hasLabelsOnlyForSelected = hasLabelsOnlyForSelected;
        if (hasLabelsOnlyForSelected) {
            this.hasLabels = false;
        }
        return this;
    }

    public int getPointRadius() {
        return pointRadius;
    }

    public Line setPointRadius(int pointRadius) {
        this.pointRadius = pointRadius;
        return this;
    }

    public ValueShape getShape() {
        return shape;
    }

    public Line setShape(ValueShape shape) {
        this.shape = shape;
        return this;
    }

    public PathEffect getPathEffect() {
        return pathEffect;
    }

    public void setPathEffect(PathEffect pathEffect) {
        this.pathEffect = pathEffect;
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
}
