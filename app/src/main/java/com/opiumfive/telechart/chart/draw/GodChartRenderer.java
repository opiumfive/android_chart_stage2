package com.opiumfive.telechart.chart.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import com.opiumfive.telechart.chart.valueFormat.PieValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.opiumfive.telechart.Settings.SELECTED_VALUES_DATE_FORMAT;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;
import static com.opiumfive.telechart.chart.Util.getDrawableFromAttr;

// god class, it was no time for arch, all time was taken by performance issues
public class GodChartRenderer {

    private static final String DETAIL_TITLE_PATTERN = "0000000000000000";
    private static final String PERCENT_PATTERN = "0000";
    private static final String ARROW_TOOLTIP = "❯";
    private static final String AREA_ALL_TITLE = "All";
    private static final int DEFAULT_LINE_STROKE_WIDTH_DP = 2;
    private static final int DEFAULT_TOUCH_TOLERANCE_MARGIN_DP = 3;
    private static final float ADDITIONAL_VIEWRECT_OFFSET = 0.075f;
    private static final int DEFAULT_LABEL_MARGIN_DP = 2;
    private static final float BITMAP_SCALE_FACTOR = 0.66f;
    private static final float SWIRL_BITMAP_SCALE_FACTOR = 0.33f;

    private static final int MESH_WIDTH = 20;
    private static final int MESH_HEIGHT = 20;
    private static final int MESH_COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1);

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
    protected char[] labelBuffer = new char[64];

    protected ChartDataProvider dataProvider;
    private float detailCornerRadius;

    private int touchToleranceMargin;
    protected Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint innerPointPaint = new Paint();
    private Map<String, float[]> linesMap = new HashMap<>();
    private float[] maximums;
    private Map<String, Float> lineSlice = new HashMap<>();
    private Map<String, Path> pathMap = new HashMap<>();
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Bitmap cacheBitmapPie;
    private Canvas cacheCanvasPie;
    private Bitmap morphBitmap;
    private Canvas morphCanvas;
    private Paint bitmapPaint;
    private Paint clearPaint = new Paint();
    private Paint bitmapMorphPaint;
    private RectF destinationRect = new RectF();
    private int rotation = 0;
    private PointF sliceVector = new PointF();
    private RectF drawCircleOval = new RectF();
    private RectF originCircleOval = new RectF();
    private float morphFactor = 0.0f;
    private RectF morphCornersRect = new RectF();
    private int clearCornersColor;
    private Bitmap boundsBitmap;
    private Canvas boundsCanvas;
    private PieValueFormatter pieValueFormatter = new PieValueFormatter();
    private boolean isCachedAreaBitmapForMorph = false;
    private boolean isCachedPieBitmapForMorph = false;

    private final float[] meshVerts = new float[MESH_COUNT * 2];
    private final float[] meshOrig = new float[MESH_COUNT * 2];
    private final Matrix meshMatrix = new Matrix();
    private final Matrix meshInverse = new Matrix();

    private int tooltipArrowColor;

    private static void setXY(float[] array, int index, float x, float y) {
        array[index * 2] = x;
        array[index * 2 + 1] = y;
    }


    protected Viewrect tempMaximumViewrect = new Viewrect();

    public GodChartRenderer(Context context, IChart chart, ChartDataProvider dataProvider) {
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
        touchLinePaint.setAlpha(255 / 10);
        touchLinePaint.setStrokeWidth(Util.dp2px(density, DEFAULT_LINE_STROKE_WIDTH_DP) / 2f);

        detailsTitleColor = getColorFromAttr(context, R.attr.detailTitleColor);

        tooltipArrowColor = context.getResources().getColor(R.color.tooltipArrowColor);

        shadowDrawable = (NinePatchDrawable) getDrawableFromAttr(context, R.attr.detailBackground);
        cacheCanvas = new Canvas();
        cacheCanvasPie = new Canvas();
        morphCanvas = new Canvas();
        boundsCanvas = new Canvas();
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        bitmapMorphPaint = new Paint();
        bitmapMorphPaint.setAntiAlias(true);
        bitmapMorphPaint.setFilterBitmap(true);
        bitmapMorphPaint.setDither(true);
        bitmapMorphPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearCornersColor = Util.getColorFromAttr(context, R.attr.itemBackground);

        clearPaint.setColor(context.getResources().getColor(android.R.color.transparent));
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void onChartDataChanged() {
        final LineChartData data = chart.getChartData();

        linesMap.clear();
        pathMap.clear();
        lineSlice.clear();

        for (Line line: data.getLines()) {
            linesMap.put(line.getId(), new float[line.getValues().size() * 4]);
            pathMap.put(line.getId(), new Path());
            lineSlice.put(line.getId(), 0f);
        }

        maximums = new float[data.getLines().get(0).getValues().size()];

        labelPaint.setTextSize(Util.sp2px(scaledDensity, data.getValueLabelTextSize()));
        labelPaint.getFontMetricsInt(fontMetrics);

        if (chart.getType().equals(CType.AREA) || chart.getType().equals(CType.PIE)) {
            linePaint.setStyle(Paint.Style.FILL);
        }

        selectedValues.clear();

        onChartViewportChanged();

    }

    public void setMorphFactor(float factor) {
        morphFactor = factor;
        if (morphFactor == 0f || morphFactor == 1f) {
            isCachedPieBitmapForMorph = false;
            isCachedAreaBitmapForMorph = false;
        }
        calculateCircleOval();
    }

    public void onChartSizeChanged() {
        cacheBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * BITMAP_SCALE_FACTOR),
                (int) (chartViewrectHandler.getChartHeight() * BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(cacheBitmap);

        // optimize bitmap size depending on screen width
        if (chartViewrectHandler.getChartWidth() < 500) {
            morphBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * BITMAP_SCALE_FACTOR),
                    (int) (chartViewrectHandler.getChartHeight() * BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
            morphCanvas.setBitmap(morphBitmap);

            boundsBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * BITMAP_SCALE_FACTOR),
                    (int) (chartViewrectHandler.getChartHeight() * BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
            boundsCanvas.setBitmap(boundsBitmap);
        } else {
            morphBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * SWIRL_BITMAP_SCALE_FACTOR),
                    (int) (chartViewrectHandler.getChartHeight() * SWIRL_BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
            morphCanvas.setBitmap(morphBitmap);

            boundsBitmap = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * SWIRL_BITMAP_SCALE_FACTOR),
                    (int) (chartViewrectHandler.getChartHeight() * SWIRL_BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
            boundsCanvas.setBitmap(boundsBitmap);
        }

        cacheBitmapPie = Bitmap.createBitmap((int) (chartViewrectHandler.getChartWidth() * BITMAP_SCALE_FACTOR),
                (int) (chartViewrectHandler.getChartHeight() * BITMAP_SCALE_FACTOR), Bitmap.Config.ARGB_8888);
        cacheCanvasPie.setBitmap(cacheBitmapPie);
        destinationRect.set(0, 0, chartViewrectHandler.getChartWidth(), chartViewrectHandler.getChartHeight());
        calculateCircleOval();

        float w = morphBitmap.getWidth();
        float h = morphBitmap.getHeight();

        int index = 0;
        for (int y = 0; y <= MESH_HEIGHT; y++) {
            float fy = h * y / MESH_HEIGHT;
            for (int x = 0; x <= MESH_WIDTH; x++) {
                float fx = w * x / MESH_WIDTH;
                setXY(meshVerts, index, fx, fy);
                setXY(meshOrig, index, fx, fy);
                index += 1;
            }
        }

        meshMatrix.setTranslate(10, 10);
        meshMatrix.invert(meshInverse);
    }

    public void onChartViewportChanged() {
        if (isViewportCalculationEnabled) {
            calculateMaxViewrect();
            chartViewrectHandler.setMaxViewrect(tempMaximumViewrect);
            chartViewrectHandler.setCurrentViewrect(chartViewrectHandler.getMaximumViewport());
            clearTouch();
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
                drawPie(canvas, data.getLines(), bounds, 1f);
                break;
        }

        if (!chart.getType().equals(chart.getTargetType())) {
            switch (chart.getTargetType()) {
                case AREA:
                    drawArea(canvas, data.getLines(), bounds, true);
                    break;
                case PIE:
                    drawPie(canvas, data.getLines(), bounds, morphFactor);
                    break;
            }
        }
    }

    // make bitmap mesh for twisting
    private void swirl(float cx, float cy, float radius, float twists) {
        float[] src = meshOrig;
        float[] dst = meshVerts;

        for (int i = 0; i < MESH_COUNT * 2; i += 2) {
            float x = src[i] - cx;
            float y = src[i + 1] - cy;

            float pixelDistance = (float) Math.sqrt((x * x) + (y * y));
            float pixelAngle = (float) Math.atan2(y, x);

            float swirlAmount = 1.0f - (pixelDistance / radius);

            if (swirlAmount > 0.0f) {
                float twistAngle = (float) (twists * swirlAmount * Math.PI * 2.0f);

                pixelAngle += twistAngle;
                x = (float) Math.cos(pixelAngle) * pixelDistance;
                y = (float) Math.sin(pixelAngle) * pixelDistance;
            }

            dst[i] = x + cx;
            dst[i + 1] = y + cy;
        }
    }

    protected void drawPie(Canvas canvas, List<Line> lines, LineChartData.Bounds bounds, float alpha) {
        if (alpha < 0.55f) {
            bitmapPaint.setAlpha((int) (255 * 0f));
        } else {
            float alph = (alpha - 0.55f) / (1f - 0.55f);
            bitmapPaint.setAlpha((int) (255 * alph));
        }

        // prevent calculations and drawing while swirling
        if (morphFactor == 0f || (morphFactor > 0f && !isCachedPieBitmapForMorph)) {

            int linesSize = lines.size();

            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;
                lineSlice.put(line.getId(), 0f);
            }

            float allMaximum = 0f;
            for (int p = bounds.from; p < bounds.to; p++) {
                float sum = 0;
                for (int l = 0; l < linesSize; l++) {
                    Line line = lines.get(l);
                    if (!line.isActive() && line.getAlpha() == 0f) continue;

                    PointValue pointValue = line.getValues().get(p);

                    allMaximum += pointValue.getY() * line.getAlpha();
                    lineSlice.put(line.getId(), lineSlice.get(line.getId()) + pointValue.getY() * line.getAlpha());
                    sum += pointValue.getY() * line.getAlpha();
                }
                maximums[p] = sum;
            }

            final float sliceScale = 360f / allMaximum;

            float lastAngle = rotation;

            cacheCanvasPie.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            boolean isShouldDrawSelectingLines = false;
            float aroundLineX1 = 0f;
            float aroundLineY1 = 0f;
            float aroundLineX21 = 0f;
            float aroundLineY21 = 0f;

            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;
                float angle = lineSlice.get(line.getId()) * sliceScale;

                sliceVector.set((float) (Math.cos(Math.toRadians(lastAngle + angle / 2))),
                        (float) (Math.sin(Math.toRadians(lastAngle + angle / 2))));
                normalizeVector(sliceVector);
                drawCircleOval.set(originCircleOval);

                linePaint.setColor(line.getColor());
                if (isTouched()) {
                    if (selectedValues.getPoints().get(0).getLineId().equals(line.getId())) {
                        drawCircleOval.inset(-5, -5);
                        cacheCanvasPie.drawArc(drawCircleOval, lastAngle, angle, true, linePaint);

                        sliceVector.set((float) (Math.cos(Math.toRadians(lastAngle))),
                                (float) (Math.sin(Math.toRadians(lastAngle))));

                        final float circleRadius = originCircleOval.width() / 2f + 5;
                        aroundLineX1 = sliceVector.x * circleRadius + originCircleOval.centerX();
                        aroundLineY1 = sliceVector.y * circleRadius + originCircleOval.centerY();

                        sliceVector.set((float) (Math.cos(Math.toRadians(lastAngle + angle))),
                                (float) (Math.sin(Math.toRadians(lastAngle + angle))));

                        aroundLineX21 = sliceVector.x * circleRadius + originCircleOval.centerX();
                        aroundLineY21 = sliceVector.y * circleRadius + originCircleOval.centerY();

                        sliceVector.set((float) (Math.cos(Math.toRadians(lastAngle + angle / 2))),
                                (float) (Math.sin(Math.toRadians(lastAngle + angle / 2))));

                        isShouldDrawSelectingLines = true;
                    } else {
                        cacheCanvasPie.drawArc(drawCircleOval, lastAngle, angle, true, linePaint);
                    }
                } else {
                    cacheCanvasPie.drawArc(drawCircleOval, lastAngle, angle, true, linePaint);
                }

                // draw slice label
                float percent = lineSlice.get(line.getId()) / allMaximum;

                final int numChars = pieValueFormatter.formatValue(labelBuffer, percent * 100);
                final float labelWidth = 0;
                final int labelHeight = Math.abs(fontMetrics.ascent);
                final float centerX = originCircleOval.centerX();
                final float centerY = originCircleOval.centerY();
                final float circleRadius = originCircleOval.width() / 2f;
                final float labelRadius = circleRadius * 0.7f;

                final float rawX = labelRadius * sliceVector.x + centerX;
                final float rawY = labelRadius * sliceVector.y + centerY;

                float left = rawX - labelWidth / 2f;
                float bottom = rawY + labelHeight / 2f;

                // rescale to fit slice
                labelPaint.setColor(Color.WHITE);
                labelPaint.setAlpha((int) (255 * line.getAlpha()));
                float textSize = Util.sp2px(scaledDensity, dataProvider.getChartData().getValueLabelTextSize());
                if (percent < 0.15f) {
                    float factor = (1f - (0.15f - percent) / 0.15f);
                    if (factor < 0.5f) factor = 0.5f;
                    textSize = textSize * factor;
                }
                labelPaint.setTextSize(textSize);
                cacheCanvasPie.drawText(labelBuffer, labelBuffer.length - numChars, numChars, left + labelMargin, bottom - labelMargin, labelPaint);

                lastAngle += angle;
            }

            if (isShouldDrawSelectingLines) {
                innerPointPaint.setStrokeWidth(3);
                cacheCanvasPie.drawLine(originCircleOval.centerX(), originCircleOval.centerY(), aroundLineX1, aroundLineY1, innerPointPaint);
                cacheCanvasPie.drawLine(originCircleOval.centerX(), originCircleOval.centerY(), aroundLineX21, aroundLineY21, innerPointPaint);
            }

            isCachedPieBitmapForMorph = true;
        }

        if (cacheBitmap != null) {
            canvas.drawBitmap(cacheBitmapPie, null, destinationRect, bitmapPaint);
        }
    }

    private void calculateCircleOval() {
        Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();
        final float circleRadius = Math.min(contentRect.width() / 2f * BITMAP_SCALE_FACTOR, contentRect.height() / 2f * BITMAP_SCALE_FACTOR);
        final float centerX = contentRect.centerX() * BITMAP_SCALE_FACTOR;
        final float centerY = contentRect.centerY() * BITMAP_SCALE_FACTOR;
        final float left = centerX - circleRadius;
        final float top = centerY - circleRadius;
        final float right = centerX + circleRadius;
        final float bottom = centerY + circleRadius;
        originCircleOval.set(left, top, right, bottom);
        final float inest = 0.5f * originCircleOval.width() * (1.0f - 0.95f);
        originCircleOval.inset(inest, inest);
    }

    private void normalizeVector(PointF point) {
        final float abs = point.length();
        point.set(point.x / abs, point.y / abs);
    }

    protected void drawArea(Canvas canvas, List<Line> lines, LineChartData.Bounds bounds, boolean thruBitmap) {

        if (thruBitmap) {
            bitmapPaint.setAlpha((int) (255 * 1f));

            if (morphFactor > 0f) {
                float[] pt = {morphBitmap.getWidth() / 2f, morphBitmap.getHeight() / 2f};

                meshInverse.mapPoints(pt);


                float twists = morphFactor  / 2f;

                if (chart.getTargetType().equals(CType.AREA)) {
                    twists = (1 - morphFactor) / 2f;
                }

                swirl(pt[0], pt[1], morphBitmap.getHeight() / 2f, twists);
            }
        }

        int linesSize = lines.size();

        // prevent calculations while swirling
        if (!thruBitmap || ((morphFactor == 0f || (morphFactor > 0f && !isCachedAreaBitmapForMorph)))) {

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
                            path.lineTo(chartViewrectHandler.getContentRectMinusAxesMargins().right, rawY);
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
        }

        boolean drawnFirstWithoutPath = false;

        if (thruBitmap) {
            // prevent drawing path while swirling
            if (morphFactor == 0f || (morphFactor > 0f && !isCachedAreaBitmapForMorph)) {
                cacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                for (int l = linesSize - 1; l >= 0; l--) {
                    Line line = lines.get(l);
                    if (!line.isActive() && line.getAlpha() == 0f) continue;

                    linePaint.setColor(line.getColor());
                    if (!drawnFirstWithoutPath) {
                        drawnFirstWithoutPath = true;
                        Rect contentRect = chartViewrectHandler.getContentRectMinusAxesMargins();
                        cacheCanvas.drawRect(contentRect, linePaint);
                        continue;
                    }

                    cacheCanvas.drawPath(pathMap.get(line.getId()), linePaint);
                    pathMap.get(line.getId()).reset();
                }

                isCachedAreaBitmapForMorph = true;
            }
            if (cacheBitmap != null) {
                if (!chart.getType().equals(chart.getTargetType())) {
                    morphCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    //morphCanvas.concat(meshMatrix);
                    morphCanvas.drawBitmapMesh(cacheBitmap, MESH_WIDTH, MESH_HEIGHT, meshVerts, 0, null, 0, bitmapPaint);

                    if (chart.getTargetType().equals(CType.AREA)) {
                        if (morphBitmap.getHeight() * (1f - morphFactor) >= morphBitmap.getHeight() * 0.5f) {
                            morphCornersRect.set(-morphBitmap.getHeight() * (0.5f - (1f - morphFactor)) * 0.4f, 0,
                                    boundsBitmap.getWidth() + morphBitmap.getHeight() * (0.5f - (1f - morphFactor)) * 0.4f, boundsBitmap.getHeight());
                        } else {
                            morphCornersRect.set(0, 0, boundsBitmap.getWidth(), boundsBitmap.getHeight());
                        }
                    } else {
                        if (morphBitmap.getHeight() * morphFactor >= morphBitmap.getHeight() * 0.5f) {
                            morphCornersRect.set(-morphBitmap.getHeight() * (0.5f - morphFactor) * 0.4f, 0,
                                    boundsBitmap.getWidth() + morphBitmap.getHeight() * (0.5f - morphFactor) * 0.4f, boundsBitmap.getHeight());
                        } else {
                            morphCornersRect.set(0, 0, boundsBitmap.getWidth(), boundsBitmap.getHeight());
                        }
                    }
                    boundsCanvas.drawColor(clearCornersColor);

                    if (chart.getTargetType().equals(CType.AREA)) {
                        boundsCanvas.drawRoundRect(morphCornersRect, morphBitmap.getHeight() * (1f - morphFactor), morphBitmap.getHeight() * (1f - morphFactor), clearPaint);
                    } else {
                        boundsCanvas.drawRoundRect(morphCornersRect, morphBitmap.getHeight() * morphFactor, morphBitmap.getHeight() * morphFactor, clearPaint);
                    }
                    morphCanvas.drawBitmap(boundsBitmap, 0 ,0, null);
                    canvas.drawBitmap(morphBitmap, null, destinationRect, bitmapPaint);
                } else {
                    canvas.drawBitmap(cacheBitmap, null, destinationRect, bitmapPaint);
                }
            }
        } else {
            for (int l = linesSize - 1; l >= 0; l--) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;
                linePaint.setColor(line.getColor());
                if (!drawnFirstWithoutPath) {
                    drawnFirstWithoutPath = true;
                    Rect contentRect = chartViewrectHandler.getContentRectMinusAxesMargins();
                    canvas.drawRect(contentRect, linePaint);
                    continue;
                }
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
            if (isTouched()) {
                linePaint.setColor(Util.lightenColor(l.getColor()));
            } else {
                linePaint.setColor(l.getColor());
            }
            canvas.drawLines(linesMap.get(l.getId()), 0, valueIndex * 4, linePaint);
        }

        if (isTouched() && !selectedValues.getPoints().isEmpty()) {
            float currentY = 0;
            for (PointValue pointValue : selectedValues.getPoints()) {
                linePaint.setColor(Util.darkenColor(pointValue.getColor()));

                final float rawBaseY = chartViewrectHandler.computeRawY(currentY);
                float y = pointValue.getY();
                final float rawY = chartViewrectHandler.computeRawY(currentY + y);
                currentY += y;
                final float rawX = chartViewrectHandler.computeRawX(pointValue.getX());
                canvas.drawLine(rawX, rawY, rawX, rawBaseY, linePaint);
            }
        }
    }

    protected void drawDailyBar(Canvas canvas, Line line, LineChartData.Bounds bounds) {
        float columnWidth = calculateColumnWidth() + 1f;
        linePaint.setStrokeWidth(columnWidth);

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

        if (isTouched()) {
            linePaint.setColor(Util.lightenColor(line.getColor()));
        } else {
            linePaint.setColor(line.getColor());
        }

        canvas.drawLines(lines, 0, valueIndex * 4, linePaint);

        if (isTouched() && !selectedValues.getPoints().isEmpty()) {
            linePaint.setColor(Util.darkenColor(line.getColor()));
            final float rawBaseY = chartViewrectHandler.getContentRectMinusAxesMargins().bottom;
            final float rawY = chartViewrectHandler.computeRawY(selectedValues.getPoints().get(0).getY());
            final float rawX = chartViewrectHandler.computeRawX(selectedValues.getPoints().get(0).getX());
            canvas.drawLine(rawX, rawY, rawX, rawBaseY, linePaint);
        }
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
            Rect content = chartViewrectHandler.getContentRectMinusAxesMargins();
            float lineX = selectedValues.getTouchX();

            if (chart.getType().equals(CType.LINE_2Y) || chart.getType().equals(CType.LINE) || chart.getType().equals(CType.AREA)) {
                canvas.drawLine(lineX, content.top, lineX, content.bottom, touchLinePaint);
            }

            if (chart.getType().equals(CType.LINE_2Y) || chart.getType().equals(CType.LINE)) {
                for (PointValue pointValue : selectedValues.getPoints()) {
                    highlightPoint(canvas, pointValue, lineX, pointValue.getApproxY());
                }
            }

            drawLabel(canvas);
        }
    }

    public boolean checkDetailsTouch(float touchX, float touchY) {
        return labelBackgroundRect.contains(touchX, touchY);
    }

    public boolean checkTouch(float touchX, float touchY) {
        selectedValues.clear();

        final LineChartData data = dataProvider.getChartData();

        if (chart.getType().equals(CType.PIE)) {
            List<Line> lines = data.getLines();

            int linesSize = lines.size();

            for (int l = 0; l < linesSize; l++) {
                Line line = lines.get(l);
                if (!line.isActive() && line.getAlpha() == 0f) continue;
                lineSlice.put(line.getId(), 0f);
            }

            float allMaximum = 0f;

            LineChartData.Bounds bounds = data.getBoundsForViewrect(getCurrentViewrect());

            for (int p = bounds.from; p < bounds.to; p++) {
                float sum = 0;
                for (int l = 0; l < linesSize; l++) {
                    Line line = lines.get(l);
                    if (!line.isActive() && line.getAlpha() == 0f) continue;

                    PointValue pointValue = line.getValues().get(p);

                    allMaximum += pointValue.getY() * line.getAlpha();
                    lineSlice.put(line.getId(), lineSlice.get(line.getId()) + pointValue.getY() * line.getAlpha());
                    sum += pointValue.getY() * line.getAlpha();
                }
                maximums[p] = sum;
            }

            Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();
            final float circleRadius = Math.min(contentRect.width() / 2f, contentRect.height() / 2f);
            final float centerX = contentRect.centerX();
            final float centerY = contentRect.centerY();

            sliceVector.set(touchX - centerX, touchY - centerY);
            float touchRadius = sliceVector.length();
            boolean withinRadius = touchRadius < circleRadius;
            if (withinRadius) {
                final float touchAngle = (pointToAngle(touchX, touchY, centerX, centerY) - rotation + 360f) % 360f;
                final float sliceScale = 360f / allMaximum;

                float lastAngle = rotation;

                PointValue selectedValue = new PointValue();

                for (int l = 0; l < linesSize; l++) {
                    Line line = lines.get(l);
                    if (!line.isActive()) continue;
                    float angle = lineSlice.get(line.getId()) * sliceScale;

                    if (touchAngle >= lastAngle) {
                        selectedValue.setLineId(line.getId());
                        selectedValue.setLine(line.getTitle());
                        selectedValue.setColor(line.getColor());
                        selectedValue.setOriginY(lineSlice.get(line.getId()));
                    }

                    lastAngle += angle;
                }

                selectedValues.add(selectedValue);
                selectedValues.setTouchX(touchX);
            }
        } else {

            for (Line line : data.getLines()) {
                if (!line.isActive()) continue;

                float minDistance = 100f;
                PointValue minPointDistanceValue = null;
                PointValue nearPointValue = null;

                for (int i = 0; i < line.getValues().size(); i++) {
                    PointValue pointValue = line.getValues().get(i);

                    float rawPointX = chartViewrectHandler.computeRawX(pointValue.getX());

                    if (rawPointX < chartViewrectHandler.getContentRectMinusAllMargins().left ||
                            rawPointX > chartViewrectHandler.getContentRectMinusAllMargins().right)
                        continue;

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
                    minPointDistanceValue.setLineId(line.getId());

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
        }

        return isTouched();
    }

    private float pointToAngle(float x, float y, float centerX, float centerY) {
        double diffX = x - centerX;
        double diffY = y - centerY;
        double radian = Math.atan2(-diffX, diffY);

        float angle = ((float) Math.toDegrees(radian) + 360) % 360;
        angle += 90f;
        return angle;
    }

    public void recalculateMax() {
        calculateMaxViewrect();
        chartViewrectHandler.setMaxViewrect(tempMaximumViewrect);
    }

    public void calculateMaxViewrect() {
        tempMaximumViewrect.set(Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MAX_VALUE);
        LineChartData data = dataProvider.getChartData();

        if (chart.getType().equals(CType.AREA) || chart.getType().equals(CType.PIE)) {
            int points = data.getLines().get(0).getValues().size();
            tempMaximumViewrect.left = data.getLines().get(0).getValues().get(0).getX();
            tempMaximumViewrect.right = data.getLines().get(0).getValues().get(points - 1).getX();
            tempMaximumViewrect.top = 100f;
            tempMaximumViewrect.bottom = 0;
        } else if (chart.getType().equals(CType.DAILY_BAR)) {
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
            tempMaximumViewrect.bottom = 0f;
        } else if (chart.getType().equals(CType.STACKED_BAR)) {
            int points = data.getLines().get(0).getValues().size();
            int lines = data.getLines().size();
            
            for (int p = 0; p < points; p++) {
                float localSum = 0f;
                for (int l = 0; l < lines; l++) {
                    if (!data.getLines().get(l).isActive()) continue;
                    localSum += data.getLines().get(l).getValues().get(p).getY();
                }
                if (localSum > tempMaximumViewrect.top) {
                    tempMaximumViewrect.top = localSum;
                }
            }

            tempMaximumViewrect.bottom = 0f;

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
                    if (pointValue.getY() < tempMaximumViewrect.bottom) {
                        tempMaximumViewrect.bottom = pointValue.getY();
                    }
                }
            }
        }
    }

    public Viewrect calculateAdjustedViewrect(Viewrect target) {
        Viewrect adjustedViewrect = new Viewrect(target.left, Float.MIN_VALUE, target.right, Float.MAX_VALUE);
        LineChartData data = dataProvider.getChartData();

        if (chart.getType().equals(CType.AREA) || chart.getType().equals(CType.PIE)) {
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

    protected void prepareLinePaint(final Line line) {
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
        labelPaint.setTextSize(Util.sp2px(scaledDensity, dataProvider.getChartData().getValueLabelTextSize()));
        float rawX = selectedValues.getTouchX();
        Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();

        float labelRawOffset = (contentRect.right - contentRect.left) * 0.2f;

        boolean isZoomable = chart.getType().equals(CType.AREA);

        float titleWidth = labelPaint.measureText(DETAIL_TITLE_PATTERN.toCharArray(), 0, DETAIL_TITLE_PATTERN.length());

        if (isZoomable) {
            titleWidth += labelPaint.measureText(ARROW_TOOLTIP.toCharArray(), 0, ARROW_TOOLTIP.length()) + labelMargin * 5;
        }

        float maxValueWidth = 0f;
        float maxNameWidth = 0f;

        for (PointValue pointValue : selectedValues.getPoints()) {
            String valueText = String.valueOf((long) pointValue.getOriginY());
            String nameText = pointValue.getLine();
            float valueWidth = labelPaint.measureText(valueText.toCharArray(), 0, valueText.length());
            float nameWidth = labelPaint.measureText(nameText.toCharArray(), 0, nameText.length());
            if (valueWidth > maxValueWidth) {
                maxValueWidth = valueWidth;
            }
            if (nameWidth > maxNameWidth) {
                maxNameWidth = nameWidth;
            }
        }

        float calculatedWidth = maxValueWidth + maxNameWidth;

        if (chart.getType().equals(CType.AREA)) {
            calculatedWidth += labelPaint.measureText(PERCENT_PATTERN.toCharArray(), 0, PERCENT_PATTERN.length());
        }

        float contentWidth = Math.max(calculatedWidth, titleWidth);

        int oneLineHeight = Math.abs(fontMetrics.ascent);
        int lines = selectedValues.getPoints().size() + 1;
        float left = rawX - contentWidth / 2 - labelMargin * 3;
        float right = rawX + contentWidth / 2 + labelMargin * 3;

        if (contentRect.right - rawX > contentWidth + labelRawOffset / 2) {
            left += labelRawOffset;
            right += labelRawOffset;
        } else {
            left -= labelRawOffset;
            right -= labelRawOffset;
        }

        if (left < contentRect.left) {
            float diff = contentRect.left - left;
            left = contentRect.left;
            right += diff;
        } else if (right > contentRect.right) {
            float diff = right - contentRect.right;
            right = contentRect.right;
            left -= diff;
        }

        if (chart.getType().equals(CType.STACKED_BAR) && selectedValues.getPoints().size() > 1) {
            lines++;
        } else if(chart.getType().equals(CType.PIE)) {
            lines--;
        }

        float summaryHeight = (oneLineHeight + labelMargin * 4) * lines + labelMargin * 6;

        labelBackgroundRect.set(left, labelMargin * 2, right, summaryHeight + labelMargin * 2);

        Rect shadowBackgroundRect = new Rect((int)(labelBackgroundRect.left - detailCornerRadius / 5), (int)(labelBackgroundRect.top - detailCornerRadius / 5),
                (int)(labelBackgroundRect.right + detailCornerRadius / 5), (int)(labelBackgroundRect.bottom + detailCornerRadius / 5));

        shadowDrawable.setBounds(shadowBackgroundRect);
        shadowDrawable.draw(canvas);

        float textX = labelBackgroundRect.left + labelMargin * 5;
        float valueX = labelBackgroundRect.right - labelMargin * 5;
        float textY = labelBackgroundRect.top + oneLineHeight + labelMargin * 4;

        String text = dateFormat.format(new Date((long) selectedValues.getPoints().get(0).getX()));

        if (!chart.getType().equals(CType.PIE)) {
            labelPaint.setTextAlign(Paint.Align.LEFT);
            labelPaint.setColor(detailsTitleColor);
            labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(text.toCharArray(), 0, text.length(), textX, textY, labelPaint);
        }

        if (isZoomable) {
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            labelPaint.setColor(tooltipArrowColor);
            labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText(ARROW_TOOLTIP.toCharArray(), 0, ARROW_TOOLTIP.length(), textX + titleWidth - labelMargin * 3, textY, labelPaint);
        }

        if (!chart.getType().equals(CType.PIE)) {
            textY += oneLineHeight + labelMargin * 4;
        }

        float sum = 0f;

        for (PointValue pointValue : selectedValues.getPoints()) {
            sum += pointValue.getOriginY();
        }

        for (PointValue pointValue : selectedValues.getPoints()) {
            String valueText = String.valueOf((long) pointValue.getOriginY());
            String nameText = pointValue.getLine();

            if (chart.getType().equals(CType.AREA)) {
                String percent = (long) Math.round(pointValue.getOriginY() / sum * 100) + "%";
                if (percent.length() == 2) percent = "0" + percent;
                nameText = percent + " " + nameText;
            }

            labelPaint.setTextAlign(Paint.Align.LEFT);
            labelPaint.setTypeface(Typeface.DEFAULT);
            labelPaint.setColor(detailsTitleColor);
            canvas.drawText(nameText.toCharArray(), 0, nameText.length(), textX, textY, labelPaint);
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
            labelPaint.setColor(pointValue.getColor());
            canvas.drawText(valueText.toCharArray(), 0, valueText.length(), valueX, textY, labelPaint);

            textY += oneLineHeight + labelMargin * 4;
        }

        if (chart.getType().equals(CType.STACKED_BAR) && selectedValues.getPoints().size() > 1) {
            String valueText = String.valueOf((long) sum);
            labelPaint.setTextAlign(Paint.Align.LEFT);
            labelPaint.setColor(detailsTitleColor);
            labelPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(AREA_ALL_TITLE.toCharArray(), 0, AREA_ALL_TITLE.length(), textX, textY, labelPaint);
            labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(valueText.toCharArray(), 0, valueText.length(), valueX, textY, labelPaint);
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
            isCachedPieBitmapForMorph = false;
            isCachedAreaBitmapForMorph = false;
            chartViewrectHandler.setCurrentViewrect(viewrect);
            clearTouch();
        }
    }

    public void setViewrectCalculationEnabled(boolean isEnabled) {
        this.isViewportCalculationEnabled = isEnabled;
    }

    public SelectedValues getSelectedValues() {
        return selectedValues;
    }
}
