package com.opiumfive.telechart.chart.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.opiumfive.telechart.chart.computator.ChartComputator;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.model.SelectedValue.SelectedValueType;
import com.opiumfive.telechart.chart.model.ValueShape;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.provider.LineChartDataProvider;
import com.opiumfive.telechart.chart.util.ChartUtils;
import com.opiumfive.telechart.chart.view.LineChartView;


public class LineChartRenderer {

    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 3;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 4;

    private static final int MODE_DRAW = 0;
    private static final int MODE_HIGHLIGHT = 1;

    public int DEFAULT_LABEL_MARGIN_DP = 4;
    protected LineChartView chart;
    protected ChartComputator computator;

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

    private LineChartDataProvider dataProvider;

    private int checkPrecision;

    private float baseValue;

    private int touchToleranceMargin;
    private Path path = new Path();
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();

    private Bitmap softwareBitmap;
    private Canvas softwareCanvas = new Canvas();
    private Viewport tempMaximumViewport = new Viewport();

    public LineChartRenderer(Context context, LineChartView chart, LineChartDataProvider dataProvider) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        this.chart = chart;
        this.computator = chart.getChartComputator();

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
        linePaint.setStrokeCap(Cap.ROUND);
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP));

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        checkPrecision = ChartUtils.dp2px(density, 2);

    }

    public void onChartSizeChanged() {
        final int internalMargin = calculateContentRectInternalMargin();
        computator.insetContentRectByInternalMargins(internalMargin, internalMargin,
                internalMargin, internalMargin);
        if (computator.getChartWidth() > 0 && computator.getChartHeight() > 0) {
            softwareBitmap = Bitmap.createBitmap(computator.getChartWidth(), computator.getChartHeight(),
                    Bitmap.Config.ARGB_8888);
            softwareCanvas.setBitmap(softwareBitmap);
        }
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

        final Canvas drawCanvas;

        // softwareBitmap can be null if chart is rendered in layout editor. In that case use default canvas and not
        // softwareCanvas.
        if (null != softwareBitmap) {
            drawCanvas = softwareCanvas;
            drawCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        } else {
            drawCanvas = canvas;
        }

        for (Line line : data.getLines()) {
            if (line.hasLines()) {
                drawPath(drawCanvas, line);
            }
        }

        if (null != softwareBitmap) {
            canvas.drawBitmap(softwareBitmap, 0, 0, null);
        }
    }

    public void drawUnclipped(Canvas canvas) {
        final LineChartData data = dataProvider.getLineChartData();
        int lineIndex = 0;
        for (Line line : data.getLines()) {
            if (checkIfShouldDrawPoints(line)) {
                drawPoints(canvas, line, lineIndex, MODE_DRAW);
            }
            ++lineIndex;
        }
        if (isTouched()) {
            // Redraw touched point to bring it to the front
            highlightPoints(canvas);
        }
    }

    private boolean checkIfShouldDrawPoints(Line line) {
        return line.hasPoints() || line.getValues().size() == 1;
    }

    public boolean checkTouch(float touchX, float touchY) {
        selectedValue.clear();
        final LineChartData data = dataProvider.getLineChartData();
        int lineIndex = 0;
        for (Line line : data.getLines()) {
            if (checkIfShouldDrawPoints(line)) {
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
            if (checkIfShouldDrawPoints(line)) {
                int margin = line.getPointRadius() + DEFAULT_TOUCH_TOLERANCE_MARGIN_DP;
                if (margin > contentAreaMargin) {
                    contentAreaMargin = margin;
                }
            }
        }
        return ChartUtils.dp2px(density, contentAreaMargin);
    }


    private void drawPath(Canvas canvas, final Line line) {
        prepareLinePaint(line);

        int valueIndex = 0;
        for (PointValue pointValue : line.getValues()) {

            final float rawX = computator.computeRawX(pointValue.getX());
            final float rawY = computator.computeRawY(pointValue.getY());

            if (valueIndex == 0) {
                path.moveTo(rawX, rawY);
            } else {
                path.lineTo(rawX, rawY);
            }

            ++valueIndex;
        }

        canvas.drawPath(path, linePaint);

        path.reset();
    }

    private void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(ChartUtils.dp2px(density, line.getStrokeWidth()));
        linePaint.setColor(line.getColor());
        linePaint.setPathEffect(line.getPathEffect());
        linePaint.setShader(null);
    }

    private void drawPoints(Canvas canvas, Line line, int lineIndex, int mode) {
        pointPaint.setColor(line.getPointColor());
        int valueIndex = 0;
        for (PointValue pointValue : line.getValues()) {
            int pointRadius = ChartUtils.dp2px(density, line.getPointRadius());
            final float rawX = computator.computeRawX(pointValue.getX());
            final float rawY = computator.computeRawY(pointValue.getY());
            if (computator.isWithinContentRect(rawX, rawY, checkPrecision)) {
                if (MODE_DRAW == mode) {
                    drawPoint(canvas, line, pointValue, rawX, rawY, pointRadius);
                    if (line.hasLabels()) {
                        drawLabel(canvas, line, pointValue, rawX, rawY, pointRadius + labelOffset);
                    }
                } else if (MODE_HIGHLIGHT == mode) {
                    highlightPoint(canvas, line, pointValue, rawX, rawY, lineIndex, valueIndex);
                } else {
                    throw new IllegalStateException("Cannot process points in mode: " + mode);
                }
            }
            ++valueIndex;
        }
    }

    private void drawPoint(Canvas canvas, Line line, PointValue pointValue, float rawX, float rawY, float pointRadius) {
        if (ValueShape.SQUARE.equals(line.getShape())) {
            canvas.drawRect(rawX - pointRadius, rawY - pointRadius, rawX + pointRadius, rawY + pointRadius,
                    pointPaint);
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

    private void highlightPoints(Canvas canvas) {
        int lineIndex = selectedValue.getFirstIndex();
        Line line = dataProvider.getLineChartData().getLines().get(lineIndex);
        drawPoints(canvas, line, lineIndex, MODE_HIGHLIGHT);
    }

    private void highlightPoint(Canvas canvas, Line line, PointValue pointValue, float rawX, float rawY, int lineIndex, int valueIndex) {
        if (selectedValue.getFirstIndex() == lineIndex && selectedValue.getSecondIndex() == valueIndex) {
            int pointRadius = ChartUtils.dp2px(density, line.getPointRadius());
            pointPaint.setColor(line.getDarkenColor());
            drawPoint(canvas, line, pointValue, rawX, rawY, pointRadius + touchToleranceMargin);
            if (line.hasLabels() || line.hasLabelsOnlyForSelected()) {
                drawLabel(canvas, line, pointValue, rawX, rawY, pointRadius + labelOffset);
            }
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
        drawLabelTextAndBackground(canvas, labelBuffer, labelBuffer.length - numChars, numChars,
                line.getDarkenColor());
    }

    private boolean isInArea(float x, float y, float touchX, float touchY, float radius) {
        float diffX = touchX - x;
        float diffY = touchY - y;
        return Math.pow(diffX, 2) + Math.pow(diffY, 2) <= 2 * Math.pow(radius, 2);
    }

    public void resetRenderer() {
        this.computator = chart.getChartComputator();
    }

    protected void drawLabelTextAndBackground(Canvas canvas, char[] labelBuffer, int startIndex, int numChars,
                                              int autoBackgroundColor) {
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
