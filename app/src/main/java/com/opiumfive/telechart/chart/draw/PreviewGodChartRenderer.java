package com.opiumfive.telechart.chart.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartDataProvider;


public class PreviewGodChartRenderer extends GodChartRenderer {

    private static final int DEFAULT_PREVIEW_STROKE_WIDTH_DP = 1;
    private static final int DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP = 10;
    private static final int DEFAULT_CHEVRON_WIDTH_DP = 2;
    private static final int DEFAULT_CHEVRON_HEIGHT_DP = 12;

    private int backgroundColor;
    private int previewColor;
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Bitmap overlayBitmap;
    private Canvas overlayCanvas;
    private Bitmap boundsBitmap;
    private Canvas boundsCanvas;
    private boolean needRecache = true;
    private RectF rect = new RectF();
    private Paint clearPaint = new Paint();
    private int clearCornersColor;

    private Paint previewPaint = new Paint();

    public PreviewGodChartRenderer(Context context, IChart chart, ChartDataProvider dataProvider) {
        super(context, chart, dataProvider);
        previewPaint.setAntiAlias(false);
        cacheCanvas = new Canvas();
        overlayCanvas = new Canvas();
        boundsCanvas = new Canvas();
        clearPaint.setColor(context.getResources().getColor(android.R.color.transparent));
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearCornersColor = Util.getColorFromAttr(context, R.attr.itemBackground);
    }

    protected void prepareLinePaint(final Line line) {
        linePaint.setStrokeWidth(Util.dp2px(density, line.getStrokeWidth() / 2f));
        linePaint.setColor(line.getColor());
        int alpha = (int)(255 * line.getAlpha());
        linePaint.setAlpha(alpha);
    }

    public void draw(Canvas canvas) {
        final LineChartData data = dataProvider.getChartData();

        boolean needRedraw = false;
        for (Line line : data.getLines()) {
            if (line.getAlpha() > 0f && line.getAlpha() < 1f) needRedraw = true;
        }

        Line line = data.getLines().get(0);

        if (needRedraw) {
            needRecache = true;

            switch (chart.getType()) {
                case LINE:
                case LINE_2Y:
                    for (Line l : data.getLines()) {
                        if (l.isActive() || (!l.isActive() && l.getAlpha() > 0f)) drawPath(canvas, l, new LineChartData.Bounds(0, l.getValues().size() - 1));
                    }
                    break;
                case DAILY_BAR:
                    drawDailyBar(canvas, data.getLines().get(0), new LineChartData.Bounds(0, line.getValues().size() - 1));
                    break;
                case STACKED_BAR:
                    drawStackedBar(canvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1));
                    break;
                case AREA:
                    drawArea(canvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1), false);
                    break;
                case PIE:
                    drawArea(canvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1), false);
                    break;
            }
        } else {
            if (needRecache) {
                needRecache = false;

                cacheCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                switch (chart.getType()) {
                    case LINE:
                    case LINE_2Y:
                        for (Line l : data.getLines()) {
                            if (l.isActive() || (!l.isActive() && l.getAlpha() > 0f)) drawPath(cacheCanvas, l, new LineChartData.Bounds(0, l.getValues().size() - 1));
                        }
                        break;
                    case DAILY_BAR:
                        drawDailyBar(cacheCanvas, data.getLines().get(0), new LineChartData.Bounds(0, line.getValues().size() - 1));
                        break;
                    case STACKED_BAR:
                        drawStackedBar(cacheCanvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1));
                        break;
                    case AREA:
                        drawArea(cacheCanvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1), false);
                        break;
                    case PIE:
                        drawArea(cacheCanvas, data.getLines(), new LineChartData.Bounds(0, line.getValues().size() - 1), false);
                        break;
                }
            }

            canvas.drawBitmap(cacheBitmap, 0, 0, null);
        }
    }

    public void onChartSizeChanged() {
        cacheBitmap = Bitmap.createBitmap(chartViewrectHandler.getChartWidth(), chartViewrectHandler.getChartHeight(), Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(cacheBitmap);
        overlayBitmap = Bitmap.createBitmap(chartViewrectHandler.getChartWidth(), chartViewrectHandler.getChartHeight(), Bitmap.Config.ARGB_8888);
        overlayCanvas.setBitmap(overlayBitmap);
        boundsBitmap = Bitmap.createBitmap(chartViewrectHandler.getChartWidth(), chartViewrectHandler.getChartHeight(), Bitmap.Config.ARGB_8888);
        boundsCanvas.setBitmap(boundsBitmap);
    }

    @Override
    public void drawSelectedValues(Canvas canvas) {
        super.drawSelectedValues(canvas);

        final Viewrect currentViewrect = chartViewrectHandler.getCurrentViewrect();
        final Viewrect maxViewrect = chartViewrectHandler.getMaximumViewport();
        final float left = chartViewrectHandler.computeRawX(currentViewrect.left);
        final float top = chartViewrectHandler.computeRawY(currentViewrect.top);
        final float right = chartViewrectHandler.computeRawX(currentViewrect.right);
        final float bottom = chartViewrectHandler.computeRawY(currentViewrect.bottom);
        final float start = chartViewrectHandler.computeRawX(maxViewrect.left);
        final float end = chartViewrectHandler.computeRawX(maxViewrect.right);

        int strokeWidth = Util.dp2px(density, DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP);
        int strokeHeight = Util.dp2px(density, DEFAULT_PREVIEW_STROKE_WIDTH_DP);


        previewPaint.setStyle(Paint.Style.FILL);

        previewPaint.setColor(clearCornersColor);
        boundsCanvas.drawPaint(previewPaint);
        rect.set(start, top, end, bottom);
        boundsCanvas.drawRoundRect(rect, strokeWidth / 2f, strokeWidth / 2f, clearPaint);

        if (boundsBitmap != null) {
            canvas.drawBitmap(boundsBitmap, 0, 0, null);
        }

        previewPaint.setColor(backgroundColor);
        rect.set(start, top, end, bottom);

        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        overlayCanvas.drawRoundRect(rect, strokeWidth / 2f, strokeWidth / 2f, previewPaint);

        previewPaint.setColor(previewColor);

        rect.set(left, top, right, bottom);

        overlayCanvas.drawRoundRect(rect, strokeWidth / 2f, strokeWidth / 2f, previewPaint);
        overlayCanvas.drawRect(left + strokeWidth, top + strokeHeight,
                right - strokeWidth, bottom - strokeHeight, clearPaint);

        if (overlayBitmap != null) {
            canvas.drawBitmap(overlayBitmap, 0, 0, null);
        }

        previewPaint.setColor(Color.WHITE);

        int chevronWidth = Util.dp2px(density, DEFAULT_CHEVRON_WIDTH_DP);
        int chevronHeight = Util.dp2px(density, DEFAULT_CHEVRON_HEIGHT_DP);

        rect.set(left + strokeWidth / 2f - chevronWidth / 2f, top - (top - bottom) / 2f - chevronHeight / 2f,
                left + strokeWidth / 2f + chevronWidth / 2f, bottom + (top - bottom) / 2f + chevronHeight / 2f);
        canvas.drawRoundRect(rect, strokeWidth / 2f, strokeWidth / 2f, previewPaint);
        rect.set(right - strokeWidth / 2f - chevronWidth / 2f, top - (top - bottom) / 2f - chevronHeight / 2f,
                right - strokeWidth / 2f + chevronWidth / 2f, bottom + (top - bottom) / 2f + chevronHeight / 2f);
        canvas.drawRoundRect(rect, strokeWidth / 2f, strokeWidth / 2f, previewPaint);
    }

    public void recalculateMax() {
        calculateMaxViewrect();
        chartViewrectHandler.setMaxViewrect(tempMaximumViewrect);
    }

    public void setPreviewColor(int color) {
        previewColor = color;
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
    }
}