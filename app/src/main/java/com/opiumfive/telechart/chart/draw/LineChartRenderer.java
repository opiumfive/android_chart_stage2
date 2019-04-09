package com.opiumfive.telechart.chart.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.CType;
import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValues;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartDataProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.opiumfive.telechart.Settings.SELECTED_VALUES_DATE_FORMAT;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;
import static com.opiumfive.telechart.chart.Util.getDrawableFromAttr;


public class LineChartRenderer {

    private static final String DETAIL_TITLE_PATTERN = "00000000000";
    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 2;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 3;
    private static final float ADDITIONAL_VIEWRECT_OFFSET = 0.075f;
    private static final int DEFAULT_LABEL_MARGIN_DP = 2;
    private static final float BITMAP_SCALE_FACTOR = 0.66f;

    protected IChart chart;
    protected ChartViewrectHandler chartViewrectHandler;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(SELECTED_VALUES_DATE_FORMAT, Locale.ENGLISH);

    protected Paint labelPaint = new Paint();

    protected RectF labelBackgroundRect = new RectF();
    protected Paint touchLinePaint = new Paint();
    protected Paint.FontMetricsInt fontMetrics = new Paint.FontMetricsInt();
    protected boolean isViewportCalculationEnabled = true;
    protected float density;
    protected float scaledDensity;
    protected SelectedValues selectedValues = new SelectedValues();
    protected int labelOffset;
    protected int labelMargin;
    private int detailsTitleColor;
    private NinePatchDrawable shadowDrawable;

    protected ChartDataProvider dataProvider;
    private float detailCornerRadius;

    private int touchToleranceMargin;
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint innerPointPaint = new Paint();
    private Map<String, float[]> linesMap = new HashMap<>();
    private float[] maximums;
    private Map<String, Path> pathMap = new HashMap<>();
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Paint bitmapPaint;
    private RectF destinationRect = new RectF();


    protected Viewrect tempMaximumViewrect = new Viewrect();

    public LineChartRenderer(Context context, IChart chart, ChartDataProvider dataProvider) {
        this.density = context.getResources().getDisplayMetrics().density;
        this.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        this.chart = chart;
        this.chartViewrectHandler = chart.getChartViewrectHandler();

        labelMargin = Util.dp2px(density, DEFAULT_LABEL_MARGIN_DP);
        labelOffset = labelMargin;

        labelPaint.setAntiAlias(true);
        labelPaint.setStyle(Paint.Style.FILL);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        labelPaint.setColor(Color.WHITE);

        this.dataProvider = dataProvider;

        touchToleranceMargin = Util.dp2px(density, DEFAULT_TOUCH_TOLERANCE_MARGIN_DP);

        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(Util.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP));

        detailCornerRadius = Util.dp2px(density, 5);

        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);

        innerPointPaint.setAntiAlias(true);
        innerPointPaint.setStyle(Paint.Style.FILL);
        innerPointPaint.setColor(getColorFromAttr(context, R.attr.itemBackground));

        touchLinePaint.setAntiAlias(true);
        touchLinePaint.setStyle(Paint.Style.FILL);
        touchLinePaint.setColor(getColorFromAttr(context, R.attr.gridColor));
        touchLinePaint.setStrokeWidth(Util.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP) / 2f);

        detailsTitleColor = getColorFromAttr(context, R.attr.detailTitleColor);

        shadowDrawable = (NinePatchDrawable) getDrawableFromAttr(context, R.attr.detailBackground);
        cacheCanvas = new Canvas();
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
    }

    public void onChartDataChanged() {
        final LineChartData data = chart.getChartData();

        Typeface typeface = chart.getChartData().getValueLabelTypeface();
        if (null != typeface) {
            labelPaint.setTypeface(typeface);
        }

        linesMap.clear();
        pathMap.clear();

        for (Line line: data.getLines()) {
            if (chart.getType().equals(CType.AREA)) {
                linesMap.put(line.getId(), new float[15000 * 4]);
            } else {
                linesMap.put(line.getId(), new float[line.getValues().size() * 4]);
            }
            pathMap.put(line.getId(), new Path());
        }

        maximums = new float[data.getLines().get(0).getValues().size()];

        labelPaint.setColor(data.getValueLabelTextColor());
        labelPaint.setTextSize(Util.sp2px(scaledDensity, data.getValueLabelTextSize()));
        labelPaint.getFontMetricsInt(fontMetrics);

        if (chart.getType().equals(CType.AREA)) {
            linePaint.setStyle(Paint.Style.FILL);
        }

        selectedValues.clear();

        onChartViewportChanged();
    }

    public void onChartSizeChanged() {
        cacheBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * BITMAP_SCALE_FACTOR),
                (int) (chartViewrectHandler.getChartHeight() * BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(cacheBitmap);
        destinationRect.set(0, 0, chartViewrectHandler.getChartWidth(), chartViewrectHandler.getChartHeight());
    }

    public void onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            calculateMaxViewrect();
            chartViewrectHandler.setMaxViewrect(tempMaximumViewrect);
            chartViewrectHandler.setCurrentViewrect(chartViewrectHandler.getMaximumViewport());
        }
    }

    public void draw(Canvas canvas) {
        final LineChartData data = dataProvider.getChartData();

        Viewrect viewrect = getCurrentViewrect();

        LineChartData.Bounds bounds = data.getBoundsForViewrect(viewrect);

        switch (chart.getType()) {
            case LINE:
            case LINE_2Y:
                for (Line line : data.getLines()) {
                    if (line.isActive() || (!line.isActive() && line.getAlpha() > 0f)) drawPath(canvas, line, bounds);
                }
                break;
            case DAILY_BAR:
                drawDailyBar(canvas, data.getLines().get(0), bounds);
                break;
            case STACKED_BAR:
                drawStackedBar(canvas, data.getLines(), bounds);
                break;
            case AREA:
                drawArea(canvas, data.getLines(), bounds, true);
                break;
            case PIE:
                drawPie(canvas, data.getLines(), bounds);
                break;
        }
    }

    protected void drawPie(Canvas canvas, List<Line> lines, LineChartData.Bounds bounds) {
    }

    protected void drawArea(Canvas canvas, List<Line> lines, LineChartData.Bounds bounds, boolean thruBitmap) {
        int linesSize = lines.size();
        for (int p = bounds.from; p < bounds.to; p++) {
            float sum = 0;
            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;

                PointValue pointValue = line.getValues().get(p);

                sum += pointValue.getY() * line.getAlpha();
            }
            maximums[p] = sum;
        }

        int valueIndex = 0;

        for (int p = bounds.from; p < bounds.to; p++) {
            float currentY = 0;
            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;

                PointValue pointValue = line.getValues().get(p);

                float y = pointValue.getY();
                if (line.getAlpha() != 1f) {
                    y = y * line.getAlpha();
                }
                float percent = (currentY + y) / maximums[p] * 100f;
                float rawY = chartViewrectHandler.computeRawY(percent);

                if (thruBitmap) {
                    rawY = rawY * BITMAP_SCALE_FACTOR;
                }

                currentY += y;
                float rawX = chartViewrectHandler.computeRawX(pointValue.getX());

                if (thruBitmap) {
                    rawX = rawX * BITMAP_SCALE_FACTOR;
                }

                Path path = pathMap.get(line.getId());

                if (path != null) {
                    if (valueIndex == 0) {
                        path.moveTo(chartViewrectHandler.getContentRectMinusAxesMargins().left, rawY);
                        path.lineTo(rawX, rawY);
                    } else {
                        path.lineTo(rawX, rawY);
                    }

                    if (p == bounds.to - 1) {
                        path.lineTo(chartViewrectHandler.getContentRectMinusAllMargins().right, rawY);
                    }
                }
            }

            valueIndex++;
        }

        for (int l = 0; l < linesSize; l++) {
            Line line = lines.get(l);
            if (!line.isActive() && line.getAlpha() == 0f) continue;
            Path path = pathMap.get(line.getId());
            Rect contentRect = chartViewrectHandler.getContentRectMinusAxesMargins();
            path.lineTo(contentRect.right, contentRect.bottom);
            path.lineTo(contentRect.left, contentRect.bottom);
            path.close();
        }

        boolean drawnFirstWithoutPath = false;

        if (thruBitmap) {
            cacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            for (int l = linesSize - 1; l >= 0; l--) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;

                if (!drawnFirstWithoutPath) {
                    drawnFirstWithoutPath = true;
                    canvas.drawColor(line.getColor());
                    continue;
                }
                linePaint.setColor(line.getColor());
                cacheCanvas.drawPath(pathMap.get(line.getId()), linePaint);
                pathMap.get(line.getId()).reset();
            }
            if (cacheBitmap != null) {
                canvas.drawBitmap(cacheBitmap, null, destinationRect, bitmapPaint);
            }
        } else {
            for (int l = linesSize - 1; l >= 0; l--) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;

                if (!drawnFirstWithoutPath) {
                    drawnFirstWithoutPath = true;
                    canvas.drawColor(line.getColor());
                    continue;
                }
                linePaint.setColor(line.getColor());
                canvas.drawPath(pathMap.get(line.getId()), linePaint);
                pathMap.get(line.getId()).reset();
            }
        }
    }

    protected void drawStackedBar(Canvas canvas, List<Line> lines, LineChartData.Bounds bounds) {
        float columnWidth = calculateColumnWidth();

        int linesSize = lines.size();

        int valueIndex = 0;

        for (int p = bounds.from; p < bounds.to; p++) {
            float currentY = 0;
            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;

                PointValue pointValue = line.getValues().get(p);

                final float rawBaseY = chartViewrectHandler.computeRawY(currentY);
                float y = pointValue.getY();
                if (line.getAlpha() != 1f) {
                    y = y * line.getAlpha();
                }
                final float rawY = chartViewrectHandler.computeRawY(currentY + y);
                currentY += y;
                final float rawX = chartViewrectHandler.computeRawX(pointValue.getX());

                float[] lineArray = linesMap.get(line.getId());
                lineArray[valueIndex * 4] = rawX;
                lineArray[valueIndex * 4 + 1] = rawY;
                lineArray[valueIndex * 4 + 2] = rawX;
                lineArray[valueIndex * 4 + 3] = rawBaseY;
            }

            valueIndex++;
        }

        linePaint.setStrokeWidth(columnWidth + 1);

        for (Line l : lines) {
            if (!l.isActive() && l.getAlpha() == 0f) continue;
            linePaint.setColor(l.getColor());
            canvas.drawLines(linesMap.get(l.getId()), 0, valueIndex * 4, linePaint);
        }
    }

    protected void drawDailyBar(Canvas canvas, Line line, LineChartData.Bounds bounds) {
        float columnWidth = calculateColumnWidth() + 1f;
        linePaint.setStrokeWidth(columnWidth);
        linePaint.setColor(line.getColor());
        float[] lines = linesMap.get(line.getId());

        int valueIndex = 0;


        for (int i = bounds.from; i <= bounds.to; i++) {
            PointValue pointValue = line.getValues().get(i);

            final float rawBaseY = chartViewrectHandler.getContentRectMinusAxesMargins().bottom;
            final float rawY = chartViewrectHandler.computeRawY(pointValue.getY());
            final float rawX = chartViewrectHandler.computeRawX(pointValue.getX());


            lines[valueIndex * 4] = rawX;
            lines[valueIndex * 4 + 1] = rawY;
            lines[valueIndex * 4 + 2] = rawX;
            lines[valueIndex * 4 + 3] = rawBaseY;

            valueIndex++;
        }

        canvas.drawLines(lines, 0, valueIndex * 4, linePaint);
    }

    private float calculateColumnWidth() {
        float width = chartViewrectHandler.getVisibleViewport().width();
        float day = width / 1000 / 60 / 60 / 24;
        float columnWidth = chartViewrectHandler.getContentRectMinusAllMargins().width() / day;
        if (columnWidth < 2) {
            columnWidth = 2;
        }
        return columnWidth;
    }

    public void drawSelectedValues(Canvas canvas) {
        if (isTouched() && !selectedValues.getPoints().isEmpty()) {
            Rect content = chartViewrectHandler.getContentRectMinusAllMargins();
            float lineX = selectedValues.getTouchX();
            canvas.drawLine(lineX, content.top, lineX, content.bottom, touchLinePaint);

            for (PointValue pointValue : selectedValues.getPoints()) {
                highlightPoint(canvas, pointValue, lineX, pointValue.getApproxY());
            }

            drawLabel(canvas);
        }
    }

    public boolean checkTouch(float touchX) {
        selectedValues.clear();

        final LineChartData data = dataProvider.getChartData();
        for (Line line : data.getLines()) {
            if (!line.isActive()) continue;

            float minDistance = 100f;
            PointValue minPointDistanceValue = null;
            PointValue nearPointValue = null;

            for (int i = 0; i < line.getValues().size(); i++) {
                PointValue pointValue = line.getValues().get(i);

                float rawPointX = chartViewrectHandler.computeRawX(pointValue.getX());

                if (rawPointX < chartViewrectHandler.getContentRectMinusAllMargins().left ||
                        rawPointX > chartViewrectHandler.getContentRectMinusAllMargins().right) continue;

                float dist = Math.abs(touchX - rawPointX);
                if (dist <= minDistance) {
                    minDistance = dist;
                    minPointDistanceValue = pointValue;
                    if (touchX <= rawPointX) {
                        if (i == 0) {
                            nearPointValue = line.getValues().get(i + 1);
                        } else {
                            nearPointValue = line.getValues().get(i - 1);
                        }
                    } else {
                        if (i >= line.getValues().size() - 1) {
                            nearPointValue = line.getValues().get(i - 1);
                        } else {
                            nearPointValue = line.getValues().get(i + 1);
                        }
                    }
                }
            }

            if (minPointDistanceValue != null) {
                minPointDistanceValue.setPointRadius(line.getPointRadius());
                minPointDistanceValue.setColor(line.getColor());
                minPointDistanceValue.setLine(line.getTitle());

                float rawPointX = chartViewrectHandler.computeRawX(minPointDistanceValue.getX());
                float rawPointY = chartViewrectHandler.computeRawY(minPointDistanceValue.getY());
                float rawNearPointX = chartViewrectHandler.computeRawX(nearPointValue.getX());
                float rawNearPointY = chartViewrectHandler.computeRawY(nearPointValue.getY());

                float k = (rawPointY - rawNearPointY) / (rawPointX - rawNearPointX);
                minPointDistanceValue.setApproxY(k * touchX + rawPointY - k * rawPointX);

                selectedValues.add(minPointDistanceValue);
                selectedValues.setTouchX(touchX);
            }
        }

        return isTouched();
    }

    public void recalculateMax() {
        calculateMaxViewrect();
        chartViewrectHandler.setMaxViewrect(tempMaximumViewrect);
    }

    public void calculateMaxViewrect() {
        tempMaximumViewrect.set(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, 0);
        LineChartData data = dataProvider.getChartData();

        if (chart.getType().equals(CType.AREA)) {
            int points = data.getLines().get(0).getValues().size();
            tempMaximumViewrect.left = data.getLines().get(0).getValues().get(0).getX();
            tempMaximumViewrect.right = data.getLines().get(0).getValues().get(points - 1).getX();
            tempMaximumViewrect.top = 100f;
            tempMaximumViewrect.bottom = 0;
        } else if (chart.getType().equals(CType.STACKED_BAR)) {
            int points = data.getLines().get(0).getValues().size();
            int lines = data.getLines().size();
            
            for (int p = 0; p < points; p++) {
                float localSum = 0f;
                for (int l = 0; l < lines; l++) {
                    localSum += data.getLines().get(l).getValues().get(p).getY();
                }
                if (localSum > tempMaximumViewrect.top) {
                    tempMaximumViewrect.top = localSum;
                }
            }

            tempMaximumViewrect.left = data.getLines().get(0).getValues().get(0).getX();
            tempMaximumViewrect.right = data.getLines().get(0).getValues().get(points - 1).getX();
        } else {
            for (Line line : data.getLines()) {
                if (!line.isActive()) continue;
                for (PointValue pointValue : line.getValues()) {
                    if (pointValue.getX() < tempMaximumViewrect.left) {
                        tempMaximumViewrect.left = pointValue.getX();
                    }
                    if (pointValue.getX() > tempMaximumViewrect.right) {
                        tempMaximumViewrect.right = pointValue.getX();
                    }
                    if (pointValue.getY() > tempMaximumViewrect.top) {
                        tempMaximumViewrect.top = pointValue.getY();
                    }
                }
            }
        }
    }

    public Viewrect calculateAdjustedViewrect(Viewrect target) {
        Viewrect adjustedViewrect = new Viewrect(target.left, Float.MIN_VALUE, target.right, Float.MAX_VALUE);
        LineChartData data = dataProvider.getChartData();

        if (chart.getType().equals(CType.AREA)) {
            adjustedViewrect.top = 100f;
            adjustedViewrect.bottom = 0f;
        } else if (chart.getType().equals(CType.STACKED_BAR)) {
            int points = data.getLines().get(0).getValues().size();
            int lines = data.getLines().size();

            for (int p = 0; p < points; p++) {
                float localSum = 0f;
                for (int l = 0; l < lines; l++) {
                    Line line = data.getLines().get(l);
                    if (!line.isActive()) continue;
                    PointValue pointValue = line.getValues().get(p);
                    if (pointValue.getX() >= target.left && pointValue.getX() <= target.right) {
                        localSum += data.getLines().get(l).getValues().get(p).getY();
                    }
                }
                if (localSum > adjustedViewrect.top) {
                    adjustedViewrect.top = localSum;
                }
                adjustedViewrect.bottom = 0;
            }

            float diff = ADDITIONAL_VIEWRECT_OFFSET * (adjustedViewrect.top - adjustedViewrect.bottom);
            adjustedViewrect.top = adjustedViewrect.top + diff;
        } else {
            for (Line line : data.getLines()) {
                if (!line.isActive()) continue;
                for (PointValue pointValue : line.getValues()) {
                    if (pointValue.getX() >= target.left && pointValue.getX() <= target.right) {
                        if (pointValue.getY() > adjustedViewrect.top) {
                            adjustedViewrect.top = pointValue.getY();
                        }
                        if (pointValue.getY() < adjustedViewrect.bottom) {
                            adjustedViewrect.bottom = pointValue.getY();
                        }
                    }
                }
            }

            //additional offset 7.5%
            float diff = ADDITIONAL_VIEWRECT_OFFSET * (adjustedViewrect.top - adjustedViewrect.bottom);
            if (chart.getType().equals(CType.DAILY_BAR)) {
                adjustedViewrect.bottom = 0;
            } else {
                adjustedViewrect.bottom = adjustedViewrect.bottom - diff * 2;
            }
            adjustedViewrect.top = adjustedViewrect.top + diff;
        }

        return adjustedViewrect;
    }

    protected int calculateContentRectInternalMargin() {
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

    protected void drawPath(Canvas canvas, final Line line, LineChartData.Bounds bounds) {
        prepareLinePaint(line);
        float[] lines = linesMap.get(line.getId());

        int valueIndex = 0;

        for (int i = bounds.from; i <= bounds.to; i++) {
            PointValue pointValue = line.getValues().get(i);

            final float rawX = chartViewrectHandler.computeRawX(pointValue.getX());
            final float rawY = chartViewrectHandler.computeRawY(pointValue.getY());

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

        canvas.drawLines(lines, 0, valueIndex * 4, linePaint);
    }

    private void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(Util.dp2px(density, line.getStrokeWidth()));
        linePaint.setColor(line.getColor());
        int alpha = (int)(255 * line.getAlpha());
        linePaint.setAlpha(alpha);
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

    private void drawLabel(Canvas canvas) {
        float rawX = selectedValues.getTouchX();
        Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();

        float titleWidth = labelPaint.measureText(DETAIL_TITLE_PATTERN.toCharArray(), 0, DETAIL_TITLE_PATTERN.length());

        float maxValueWidth = 0f;

        for (PointValue pointValue : selectedValues.getPoints()) {
            labelPaint.setColor(pointValue.getColor());
            String valueText = String.valueOf((long) pointValue.getY());
            String nameText = pointValue.getLine();
            float valueWidth = Math.max(labelPaint.measureText(valueText.toCharArray(), 0, valueText.length()), labelPaint.measureText(nameText.toCharArray(), 0, nameText.length()));
            if (valueWidth > maxValueWidth) {
                maxValueWidth = valueWidth;
            }
        }

        float calculatedWidth = maxValueWidth * selectedValues.getPoints().size() + labelMargin * 4 * (selectedValues.getPoints().size() - 1);
        float contentWidth = Math.max(calculatedWidth, titleWidth);

        int oneLineHeight = Math.abs(fontMetrics.ascent);
        int lines = 5;
        float left = rawX - contentWidth / 2 - labelMargin * 3;
        float right = rawX + contentWidth / 2 + labelMargin * 3;

        if (left < contentRect.left) {
            float diff = contentRect.left - left;
            left = contentRect.left;
            right += diff;
        } else if (right > contentRect.right) {
            float diff = right - contentRect.right;
            right = contentRect.right;
            left -= diff;
        }

        labelBackgroundRect.set(left, 0, right, oneLineHeight * lines);

        Rect shadowBackgroundRect = new Rect((int)(labelBackgroundRect.left - detailCornerRadius / 5), (int)(labelBackgroundRect.top - detailCornerRadius / 5),
                (int)(labelBackgroundRect.right + detailCornerRadius / 5), (int)(labelBackgroundRect.bottom + detailCornerRadius / 5));

        shadowDrawable.setBounds(shadowBackgroundRect);
        shadowDrawable.draw(canvas);

        float textX = labelBackgroundRect.left + labelMargin * 5;
        float textY = labelBackgroundRect.top + oneLineHeight + labelMargin * 3;

        String text = dateFormat.format(new Date((long) selectedValues.getPoints().get(0).getX()));
        labelPaint.setColor(detailsTitleColor);
        canvas.drawText(text.toCharArray(), 0, text.length(), textX, textY, labelPaint);

        textY += oneLineHeight + labelMargin * 4;
        float textYName = textY + oneLineHeight + labelMargin;

        for (PointValue pointValue : selectedValues.getPoints()) {
            labelPaint.setColor(pointValue.getColor());
            String valueText = String.valueOf((long) pointValue.getY());
            String nameText = pointValue.getLine();

            canvas.drawText(valueText.toCharArray(), 0, valueText.length(), textX, textY, labelPaint);
            canvas.drawText(nameText.toCharArray(), 0, nameText.length(), textX, textYName, labelPaint);

            textX += maxValueWidth + labelMargin * 4;
        }
    }

    public void resetRenderer() {
        this.chartViewrectHandler = chart.getChartViewrectHandler();
    }

    public boolean isTouched() {
        return selectedValues.isSet();
    }

    public void clearTouch() {
        selectedValues.clear();
    }

    public Viewrect getMaximumViewrect() {
        return chartViewrectHandler.getMaximumViewport();
    }

    public void setMaximumViewrect(Viewrect maxViewrect) {
        if (null != maxViewrect) {
            chartViewrectHandler.setMaxViewrect(maxViewrect);
        }
    }

    public Viewrect getCurrentViewrect() {
        return chartViewrectHandler.getCurrentViewrect();
    }

    public void setCurrentViewrect(Viewrect viewrect) {
        if (null != viewrect) {
            chartViewrectHandler.setCurrentViewrect(viewrect);
        }
    }

    public void setViewrectCalculationEnabled(boolean isEnabled) {
        this.isViewportCalculationEnabled = isEnabled;
    }

    public SelectedValues getSelectedValues() {
        return selectedValues;
    }
}
