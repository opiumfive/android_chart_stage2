package com.opiumfive.telechart.chart.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.model.SelectedValue.SelectedValueType;
import com.opiumfive.telechart.chart.model.ValueShape;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.LineChartDataProvider;
import com.opiumfive.telechart.chart.util.ChartUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LineChartRenderer {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 2;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 4;
    private static final float DEFAULT_MAX_ANGLE_VARIATION = 20f;

    private static final int MODE_DRAW = 0;
    private static final int MODE_HIGHLIGHT = 1;


    public int DEFAULT_LABEL_MARGIN_DP = 4;
    protected ILineChart chart;
    protected ChartViewportHandler computator;

    protected Paint labelPaint = new Paint();
    protected Paint labelBackgroundPaint = new Paint();
    protected RectF labelBackgroundRect = new RectF();
    protected Paint.FontMetricsInt fontMetrics = new Paint.FontMetricsInt();
    protected boolean isViewportCalculationEnabled = true;
    protected float density;
    protected float scaledDensity;
    protected SelectedValue selectedValue = new SelectedValue();
    protected char[] labelBuffer = new char[64];
    protected int labelOffset;
    protected int labelMargin;
    protected boolean isValueLabelBackgroundEnabled;
    protected boolean isValueLabelBackgroundAuto;
    protected float maxAngleVariation = DEFAULT_MAX_ANGLE_VARIATION;

    private LineChartDataProvider dataProvider;

    private int checkPrecision;

    private float baseValue;

    private int touchToleranceMargin;
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();

    private Viewport tempMaximumViewport = new Viewport();

    public LineChartRenderer(Context context, ILineChart chart, LineChartDataProvider dataProvider) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        this.chart = chart;
        this.computator = chart.getChartViewportHandler();

        labelMargin = ChartUtils.dp2px(density, DEFAULT_LABEL_MARGIN_DP);
        labelOffset = labelMargin;

        labelPaint.setAntiAlias(true);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        labelPaint.setColor(Color.WHITE);

        labelBackgroundPaint.setAntiAlias(true);
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        this.dataProvider = dataProvider;

        touchToleranceMargin = ChartUtils.dp2px(density, DEFAULT_TOUCH_TOLERANCE_MARGIN_DP);

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Cap.BUTT);
        linePaint.setDither(true);
        linePaint.setStrokeJoin(Paint.Join.BEVEL);
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP));

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        checkPrecision = ChartUtils.dp2px(density, 2);

    }

    public void setMaxAngleVariation(float maxAngleVariation) {
        this.maxAngleVariation = maxAngleVariation;
    }

    public void onChartSizeChanged() {
        final int internalMargin = calculateContentRectInternalMargin();
        computator.insetContentRectByInternalMargins(internalMargin, internalMargin, internalMargin, internalMargin);
    }

    public void onChartDataChanged() {
        final LineChartData data = chart.getChartData();

        Typeface typeface = chart.getChartData().getValueLabelTypeface();
        if (null != typeface) {
            labelPaint.setTypeface(typeface);
        }

        labelPaint.setColor(data.getValueLabelTextColor());
        labelPaint.setTextSize(ChartUtils.sp2px(scaledDensity, data.getValueLabelTextSize()));
        labelPaint.getFontMetricsInt(fontMetrics);

        this.isValueLabelBackgroundEnabled = data.isValueLabelBackgroundEnabled();
        this.isValueLabelBackgroundAuto = data.isValueLabelBackgroundAuto();
        this.labelBackgroundPaint.setColor(data.getValueLabelBackgroundColor());

        selectedValue.clear();

        final int internalMargin = calculateContentRectInternalMargin();
        computator.insetContentRectByInternalMargins(internalMargin, internalMargin, internalMargin, internalMargin);
        baseValue = dataProvider.getLineChartData().getBaseValue();

        onChartViewportChanged();
    }

    public void onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            calculateMaxViewport();
            computator.setMaxViewport(tempMaximumViewport);
            computator.setCurrentViewport(computator.getMaximumViewport());
        }
    }

    public void draw(Canvas canvas) {
        final LineChartData data = dataProvider.getLineChartData();


        for (Line line : data.getLines()) {
            drawPath(canvas, line);
        }
    }

    public void drawUnclipped(Canvas canvas) {
        if (isTouched()) {
            // TODO draw touched point to bring it to the front
            //highlightPoint(canvas);
        }
    }

    public boolean checkTouch(float touchX, float touchY) {
        selectedValue.clear();
        final LineChartData data = dataProvider.getLineChartData();
        int lineIndex = 0;
        for (Line line : data.getLines()) {
            int pointRadius = ChartUtils.dp2px(density, line.getPointRadius());
            int valueIndex = 0;
            for (PointValue pointValue : line.getValues()) {
                final float rawValueX = computator.computeRawX(pointValue.getX());
                final float rawValueY = computator.computeRawY(pointValue.getY());
                if (isInArea(rawValueX, rawValueY, touchX, touchY, pointRadius + touchToleranceMargin)) {
                    selectedValue.set(lineIndex, valueIndex, SelectedValueType.LINE);
                }
                ++valueIndex;
            }
            ++lineIndex;
        }
        return isTouched();
    }

    private void calculateMaxViewport() {
        tempMaximumViewport.set(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        LineChartData data = dataProvider.getLineChartData();


        for (Line line : data.getLines()) {
            // Calculate max and min for viewport.
            for (PointValue pointValue : line.getValues()) {
                if (pointValue.getX() < tempMaximumViewport.left) {
                    tempMaximumViewport.left = pointValue.getX();
                }
                if (pointValue.getX() > tempMaximumViewport.right) {
                    tempMaximumViewport.right = pointValue.getX();
                }
                if (pointValue.getY() < tempMaximumViewport.bottom) {
                    tempMaximumViewport.bottom = pointValue.getY();
                }
                if (pointValue.getY() > tempMaximumViewport.top) {
                    tempMaximumViewport.top = pointValue.getY();
                }

            }
        }


    }

    private int calculateContentRectInternalMargin() {
        int contentAreaMargin = 0;
        final LineChartData data = dataProvider.getLineChartData();
        for (Line line : data.getLines()) {
            int margin = line.getPointRadius() + DEFAULT_TOUCH_TOLERANCE_MARGIN_DP;
            if (margin > contentAreaMargin) {
                contentAreaMargin = margin;
            }
        }
        return ChartUtils.dp2px(density, contentAreaMargin);
    }

    private void drawPath(Canvas canvas, final Line line) {
        prepareLinePaint(line);
        List<PointValue> optimizedList = computator.optimizeLine(line.getValues(), maxAngleVariation);
        float[] lines = new float[optimizedList.size() * 4];

        int valueIndex = 0;
        for (PointValue pointValue : optimizedList) {

            final float rawX = computator.computeRawX(pointValue.getX());
            final float rawY = computator.computeRawY(pointValue.getY());

            //lines[valueIndex * 2] = rawX;
            //lines[valueIndex * 2 + 1] = rawY;

            if (valueIndex == 0) {
                lines[valueIndex * 4] = rawX;
                lines[valueIndex * 4 + 1] = rawY;
            } else {
                lines[valueIndex * 4] =  lines[valueIndex * 4 - 2];
                lines[valueIndex * 4 + 1] =  lines[valueIndex * 4 - 1];
            }

            lines[valueIndex * 4 + 2] = rawX;
            lines[valueIndex * 4 + 3] = rawY;

            valueIndex++;
        }

        //canvas.drawLines(lines, 0, lines.length, linePaint);
        //canvas.drawLines(lines, 2, lines.length - 4, linePaint);

        /*if (count >= 4) {
            if ((count & 2) != 0) {
                canvas.drawLines(pointlist, 0, count-2, linePaint);
                canvas.drawLines(pointlist, 2, count-2, linePaint);
            } else {
                canvas.drawLines(pointlist, 0, count, linePaint);
                canvas.drawLines(pointlist, 2, count - 4, linePaint);
            }
        }*/

        canvas.drawLines(lines, linePaint);

    }

    private void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, line.getStrokeWidth()));
        linePaint.setColor(line.getColor());
    }

    private void drawPoint(Canvas canvas, Line line, PointValue pointValue, float rawX, float rawY, float pointRadius) {
        if (ValueShape.SQUARE.equals(line.getShape())) {
            canvas.drawRect(rawX - pointRadius, rawY - pointRadius, rawX + pointRadius, rawY + pointRadius, pointPaint);
        } else if (ValueShape.CIRCLE.equals(line.getShape())) {
            canvas.drawCircle(rawX, rawY, pointRadius, pointPaint);
        } else if (ValueShape.DIAMOND.equals(line.getShape())) {
            canvas.save();
            canvas.rotate(45, rawX, rawY);
            canvas.drawRect(rawX - pointRadius, rawY - pointRadius, rawX + pointRadius, rawY + pointRadius, pointPaint);
            canvas.restore();
        } else {
            throw new IllegalArgumentException("Invalid point shape: " + line.getShape());
        }
    }

    private void highlightPoint(Canvas canvas, Line line, PointValue pointValue, float rawX, float rawY, int lineIndex, int valueIndex) {
        if (selectedValue.getFirstIndex() == lineIndex && selectedValue.getSecondIndex() == valueIndex) {
            int pointRadius = ChartUtils.dp2px(density, line.getPointRadius());
            pointPaint.setColor(line.getDarkenColor());
            drawPoint(canvas, line, pointValue, rawX, rawY, pointRadius + touchToleranceMargin);
            drawLabel(canvas, line, pointValue, rawX, rawY, pointRadius + labelOffset);
        }
    }

    private void drawLabel(Canvas canvas, Line line, PointValue pointValue, float rawX, float rawY, float offset) {
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        final int numChars = line.getFormatter().formatChartValue(labelBuffer, pointValue);
        if (numChars == 0) {
            // No need to draw empty label
            return;
        }

        final float labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.length - numChars, numChars);
        final int labelHeight = Math.abs(fontMetrics.ascent);
        float left = rawX - labelWidth / 2 - labelMargin;
        float right = rawX + labelWidth / 2 + labelMargin;

        float top;
        float bottom;

        if (pointValue.getY() >= baseValue) {
            top = rawY - offset - labelHeight - labelMargin * 2;
            bottom = rawY - offset;
        } else {
            top = rawY + offset;
            bottom = rawY + offset + labelHeight + labelMargin * 2;
        }

        if (top < contentRect.top) {
            top = rawY + offset;
            bottom = rawY + offset + labelHeight + labelMargin * 2;
        }
        if (bottom > contentRect.bottom) {
            top = rawY - offset - labelHeight - labelMargin * 2;
            bottom = rawY - offset;
        }
        if (left < contentRect.left) {
            left = rawX;
            right = rawX + labelWidth + labelMargin * 2;
        }
        if (right > contentRect.right) {
            left = rawX - labelWidth - labelMargin * 2;
            right = rawX;
        }

        labelBackgroundRect.set(left, top, right, bottom);
        drawLabelTextAndBackground(canvas, labelBuffer, labelBuffer.length - numChars, numChars, line.getDarkenColor());
    }

    private boolean isInArea(float x, float y, float touchX, float touchY, float radius) {
        float diffX = touchX - x;
        float diffY = touchY - y;
        return Math.pow(diffX, 2) + Math.pow(diffY, 2) <= 2 * Math.pow(radius, 2);
    }

    public void resetRenderer() {
        this.computator = chart.getChartViewportHandler();
    }

    protected void drawLabelTextAndBackground(Canvas canvas, char[] labelBuffer, int startIndex, int numChars, int autoBackgroundColor) {
        final float textX;
        final float textY;

        if (isValueLabelBackgroundEnabled) {

            if (isValueLabelBackgroundAuto) {
                labelBackgroundPaint.setColor(autoBackgroundColor);
            }

            canvas.drawRect(labelBackgroundRect, labelBackgroundPaint);

            textX = labelBackgroundRect.left + labelMargin;
            textY = labelBackgroundRect.bottom - labelMargin;
        } else {
            textX = labelBackgroundRect.left;
            textY = labelBackgroundRect.bottom;
        }

        canvas.drawText(labelBuffer, startIndex, numChars, textX, textY, labelPaint);
    }

    public boolean isTouched() {
        return selectedValue.isSet();
    }

    public void clearTouch() {
        selectedValue.clear();
    }

    public Viewport getMaximumViewport() {
        return computator.getMaximumViewport();
    }

    public void setMaximumViewport(Viewport maxViewport) {
        if (null != maxViewport) {
            computator.setMaxViewport(maxViewport);
        }
    }

    public Viewport getCurrentViewport() {
        return computator.getCurrentViewport();
    }

    public void setCurrentViewport(Viewport viewport) {
        if (null != viewport) {
            computator.setCurrentViewport(viewport);
        }
    }

    public boolean isViewportCalculationEnabled() {
        return isViewportCalculationEnabled;
    }

    public void setViewportCalculationEnabled(boolean isEnabled) {
        this.isViewportCalculationEnabled = isEnabled;
    }

    public void selectValue(SelectedValue selectedValue) {
        this.selectedValue.set(selectedValue);
    }

    public SelectedValue getSelectedValue() {
        return selectedValue;
    }

}
