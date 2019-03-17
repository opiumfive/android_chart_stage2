package com.opiumfive.telechart.chart.render;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValues;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartDataProvider;

import java.util.List;

import static com.opiumfive.telechart.chart.Util.getColorFromAttr;


public class LineChartRenderer {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 2;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 3;
    private static final float DEFAULT_MAX_ANGLE_VARIATION = 2f;

    public int DEFAULT_LABEL_MARGIN_DP = 0;
    protected ILineChart chart;
    protected ChartViewportHandler chartViewportHandler;

    protected Paint labelPaint = new Paint();
    protected Paint labelBackgroundPaint = new Paint();
    protected RectF labelBackgroundRect = new RectF();
    protected Paint touchLinePaint = new Paint();
    protected Paint.FontMetricsInt fontMetrics = new Paint.FontMetricsInt();
    protected boolean isViewportCalculationEnabled = true;
    protected float density;
    protected float scaledDensity;
    protected SelectedValues selectedValues = new SelectedValues();
    protected char[] labelBuffer = new char[64];
    protected int labelOffset;
    protected int labelMargin;
    protected boolean isValueLabelBackgroundEnabled;
    protected boolean isValueLabelBackgroundAuto;
    protected float maxAngleVariation = DEFAULT_MAX_ANGLE_VARIATION;

    private ChartDataProvider dataProvider;

    private int checkPrecision;

    private float baseValue;

    private int touchToleranceMargin;
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint innerPointPaint = new Paint();

    private Viewrect tempMaximumViewrect = new Viewrect();

    public LineChartRenderer(Context context, ILineChart chart, ChartDataProvider dataProvider) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        this.chart = chart;
        this.chartViewportHandler = chart.getChartViewportHandler();

        labelMargin = Util.dp2px(density, DEFAULT_LABEL_MARGIN_DP);
        labelOffset = labelMargin;

        labelPaint.setAntiAlias(true);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        labelPaint.setColor(Color.WHITE);

        labelBackgroundPaint.setAntiAlias(true);
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        labelBackgroundPaint.setColor(getColorFromAttr(context, R.attr.itemBackground));
        this.dataProvider = dataProvider;

        touchToleranceMargin = Util.dp2px(density, DEFAULT_TOUCH_TOLERANCE_MARGIN_DP);

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Cap.BUTT);
        linePaint.setDither(true);
        linePaint.setStrokeJoin(Paint.Join.BEVEL);
        linePaint.setStrokeWidth(Util.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP));

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        innerPointPaint.setAntiAlias(true);
        innerPointPaint.setStyle(Paint.Style.FILL);
        innerPointPaint.setColor(getColorFromAttr(context, R.attr.itemBackground));

        touchLinePaint.setAntiAlias(true);
        touchLinePaint.setStyle(Paint.Style.FILL);
        touchLinePaint.setColor(getColorFromAttr(context, R.attr.dividerColor));
        touchLinePaint.setStrokeWidth(Util.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP) / 2f);

        checkPrecision = Util.dp2px(density, 2);
    }

    public void setMaxAngleVariation(float maxAngleVariation) {
        this.maxAngleVariation = maxAngleVariation;
    }

    public void onChartSizeChanged() {
        final int internalMargin = calculateContentRectInternalMargin();
        chartViewportHandler.insetContentRectByInternalMargins(internalMargin, internalMargin, internalMargin, internalMargin);
    }

    public void onChartDataChanged() {
        final LineChartData data = chart.getChartData();

        Typeface typeface = chart.getChartData().getValueLabelTypeface();
        if (null != typeface) {
            labelPaint.setTypeface(typeface);
        }

        labelPaint.setColor(data.getValueLabelTextColor());
        labelPaint.setTextSize(Util.sp2px(scaledDensity, data.getValueLabelTextSize()));
        labelPaint.getFontMetricsInt(fontMetrics);

        this.isValueLabelBackgroundEnabled = data.isValueLabelBackgroundEnabled();
        this.isValueLabelBackgroundAuto = data.isValueLabelBackgroundAuto();
        this.labelBackgroundPaint.setColor(data.getValueLabelBackgroundColor());

        selectedValues.clear();

        final int internalMargin = calculateContentRectInternalMargin();
        chartViewportHandler.insetContentRectByInternalMargins(internalMargin, internalMargin, internalMargin, internalMargin);
        baseValue = dataProvider.getChartData().getBaseValue();

        onChartViewportChanged();
    }

    public void onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            calculateMaxViewport();
            chartViewportHandler.setMaxViewrect(tempMaximumViewrect);
            chartViewportHandler.setCurrentViewrect(chartViewportHandler.getMaximumViewport());
        }
    }

    public void draw(Canvas canvas) {
        final LineChartData data = dataProvider.getChartData();


        for (Line line : data.getLines()) {
            if (line.isActive()) drawPath(canvas, line);
        }
    }

    public void drawUnclipped(Canvas canvas) {
        if (isTouched()) {
            Rect content = chartViewportHandler.getContentRectMinusAllMargins();
            canvas.drawLine(selectedValues.getTouchX(), content.top, selectedValues.getTouchX(), content.bottom, touchLinePaint);

            for (PointValue pointValue : selectedValues.getPoints()) {

                final float rawX = chartViewportHandler.computeRawX(pointValue.getX());
                final float rawY = chartViewportHandler.computeRawY(pointValue.getY());

                highlightPoint(canvas, pointValue, rawX, rawY);
            }

            drawLabel(canvas, selectedValues.getTouchX());
        }
    }

    public boolean checkTouch(float touchX) {
        selectedValues.clear();

        final LineChartData data = dataProvider.getChartData();
        for (Line line : data.getLines()) {
            if (!line.isActive()) continue;

            float minDistance = 100f;
            PointValue minPointDistanceValue = null;
            float minPointX = 0f;

            for (PointValue pointValue : line.getValues()) {
                float rawPointX = chartViewportHandler.computeRawX(pointValue.getX());

                float dist = Math.abs(touchX - rawPointX);
                if (dist <= minDistance) {
                    minDistance = dist;
                    minPointDistanceValue = pointValue;
                    minPointX = rawPointX;
                }
            }

            if (minPointDistanceValue != null) {
                minPointDistanceValue.setPointRadius(line.getPointRadius());
                minPointDistanceValue.setColor(line.getColor());
                selectedValues.add(minPointDistanceValue);
                selectedValues.setTouchX(minPointX);
            }
        }

        return isTouched();
    }

    private void calculateMaxViewport() {
        tempMaximumViewrect.set(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        LineChartData data = dataProvider.getChartData();

        for (Line line : data.getLines()) {
            if (!line.isActive()) continue;
            for (PointValue pointValue : line.getValues()) {
                if (pointValue.getX() < tempMaximumViewrect.left) {
                    tempMaximumViewrect.left = pointValue.getX();
                }
                if (pointValue.getX() > tempMaximumViewrect.right) {
                    tempMaximumViewrect.right = pointValue.getX();
                }
                if (pointValue.getY() < tempMaximumViewrect.bottom) {
                    tempMaximumViewrect.bottom = pointValue.getY();
                }
                if (pointValue.getY() > tempMaximumViewrect.top) {
                    tempMaximumViewrect.top = pointValue.getY();
                }
            }
        }
    }

    private int calculateContentRectInternalMargin() {
        int contentAreaMargin = 0;
        final LineChartData data = dataProvider.getChartData();
        for (Line line : data.getLines()) {
            if (!line.isActive()) continue;
            int margin = line.getPointRadius() + DEFAULT_TOUCH_TOLERANCE_MARGIN_DP;
            if (margin > contentAreaMargin) {
                contentAreaMargin = margin;
            }
        }
        return Util.dp2px(density, contentAreaMargin);
    }

    private void drawPath(Canvas canvas, final Line line) {
        prepareLinePaint(line);
        List<PointValue> optimizedList = chartViewportHandler.optimizeLine(line.getValues(), maxAngleVariation);
        float[] lines = new float[optimizedList.size() * 4];

        int valueIndex = 0;
        for (PointValue pointValue : optimizedList) {

            final float rawX = chartViewportHandler.computeRawX(pointValue.getX());
            final float rawY = chartViewportHandler.computeRawY(pointValue.getY());

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

        canvas.drawLines(lines, linePaint);
    }

    private void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(Util.dp2px(density, line.getStrokeWidth()));
        linePaint.setColor(line.getColor());
    }

    private void drawPoint(Canvas canvas, float rawX, float rawY, float pointRadius) {
        canvas.drawCircle(rawX, rawY, pointRadius, pointPaint);
    }

    private void drawInnerPoint(Canvas canvas, float rawX, float rawY, float pointRadius) {
        canvas.drawCircle(rawX, rawY, pointRadius, innerPointPaint);
    }

    private void highlightPoint(Canvas canvas, PointValue pointValue, float rawX, float rawY) {
        int pointRadius = Util.dp2px(density, pointValue.getPointRadius());
        pointPaint.setColor(pointValue.getColor());
        drawPoint(canvas, rawX, rawY, pointRadius + touchToleranceMargin);
        drawInnerPoint(canvas, rawX, rawY, touchToleranceMargin);
    }

    private void drawLabel(Canvas canvas, float rawX) {
        final Rect contentRect = chartViewportHandler.getContentRectMinusAllMargins();

        final float labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.length - 20, 20);
        final int labelHeight = Math.abs(fontMetrics.ascent);
        float left = rawX - labelWidth / 2 - labelMargin;
        float right = rawX + labelWidth / 2 + labelMargin;

        labelBackgroundRect.set(left, 0, right, 100);

        canvas.drawRect(labelBackgroundRect, labelBackgroundPaint);

        float textX = labelBackgroundRect.left + labelMargin;
        float textY = labelBackgroundRect.bottom - labelMargin;


        canvas.drawText("some".toCharArray(), 0, 4, textX, textY, labelPaint);


        //final int numChars = line.getFormatter().formatChartValue(labelBuffer, pointValue);
        /*if (numChars == 0) {
            // No need to draw empty label
            return;
        }*/

        //final float labelWidth = labelPaint.measureText(labelBuffer, labelBuffer.length - numChars, numChars);
        //final int labelHeight = Math.abs(fontMetrics.ascent);
        //float left = rawX - labelWidth / 2 - labelMargin;
        //float right = rawX + labelWidth / 2 + labelMargin;

        /*float top;
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
        drawLabelTextAndBackground(canvas, labelBuffer, labelBuffer.length - numChars, numChars, line.getDarkenColor());*/
    }

    public void resetRenderer() {
        this.chartViewportHandler = chart.getChartViewportHandler();
    }

    public boolean isTouched() {
        return selectedValues.isSet();
    }

    public void clearTouch() {
        selectedValues.clear();
    }

    public Viewrect getMaximumViewport() {
        return chartViewportHandler.getMaximumViewport();
    }

    public void setMaximumViewport(Viewrect maxViewrect) {
        if (null != maxViewrect) {
            chartViewportHandler.setMaxViewrect(maxViewrect);
        }
    }

    public Viewrect getCurrentViewport() {
        return chartViewportHandler.getCurrentViewrect();
    }

    public void setCurrentViewport(Viewrect viewrect) {
        if (null != viewrect) {
            chartViewportHandler.setCurrentViewrect(viewrect);
        }
    }

    public boolean isViewportCalculationEnabled() {
        return isViewportCalculationEnabled;
    }

    public void setViewportCalculationEnabled(boolean isEnabled) {
        this.isViewportCalculationEnabled = isEnabled;
    }

    public void selectValue(SelectedValues selectedValues) {
        this.selectedValues.set(selectedValues);
    }

    public SelectedValues getSelectedValues() {
        return selectedValues;
    }

}
