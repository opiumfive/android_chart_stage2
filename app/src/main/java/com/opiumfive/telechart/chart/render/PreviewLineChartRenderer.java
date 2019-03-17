package com.opiumfive.telechart.chart.render;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartDataProvider;


public class PreviewLineChartRenderer extends LineChartRenderer {

    private static final int DEFAULT_PREVIEW_STROKE_WIDTH_DP = 1;
    private static final int DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP = 4;
    private static final float DEFAULT_MAX_ANGLE_VARIATION = 10f;

    private int backrgroundColor;
    private int previewColor;

    private Paint previewPaint = new Paint();

    public PreviewLineChartRenderer(Context context, ILineChart chart, ChartDataProvider dataProvider) {
        super(context, chart, dataProvider);
        previewPaint.setAntiAlias(false);

        setMaxAngleVariation(DEFAULT_MAX_ANGLE_VARIATION);
    }

    @Override
    public void drawUnclipped(Canvas canvas) {
        super.drawUnclipped(canvas);
        final Viewrect currentViewrect = chartViewportHandler.getCurrentViewrect();
        final Viewrect maxViewrect = chartViewportHandler.getMaximumViewport();
        final float left = chartViewportHandler.computeRawX(currentViewrect.left);
        final float top = chartViewportHandler.computeRawY(currentViewrect.top);
        final float right = chartViewportHandler.computeRawX(currentViewrect.right);
        final float bottom = chartViewportHandler.computeRawY(currentViewrect.bottom);
        final float start = chartViewportHandler.computeRawX(maxViewrect.left);
        final float end = chartViewportHandler.computeRawX(maxViewrect.right);
        previewPaint.setColor(backrgroundColor);
        previewPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(start, top, left - DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP, bottom, previewPaint);
        canvas.drawRect(right + DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP, top, end, bottom, previewPaint);

        previewPaint.setStrokeWidth(Util.dp2px(density, DEFAULT_PREVIEW_STROKE_WIDTH_DP));
        previewPaint.setColor(previewColor);
        previewPaint.setStyle(Paint.Style.STROKE);
        int halfStrokeWidth = Util.dp2px(density, DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP) / 2;
        int halfStrokeHeight = Util.dp2px(density, DEFAULT_PREVIEW_STROKE_WIDTH_DP) / 2;
        canvas.drawLine(left + halfStrokeWidth, top, right - halfStrokeWidth, top, previewPaint);
        canvas.drawLine(left + halfStrokeWidth, bottom, right - halfStrokeWidth, bottom, previewPaint);
        previewPaint.setStrokeWidth(Util.dp2px(density, DEFAULT_PREVIEW_STROKE_SIDES_WIDTH_DP));
        canvas.drawLine(left, top - halfStrokeHeight, left, bottom + halfStrokeHeight, previewPaint);
        canvas.drawLine(right, top - halfStrokeHeight, right, bottom + halfStrokeHeight, previewPaint);
    }

    public int getPreviewColor() {
        return previewColor;
    }

    public void setPreviewColor(int color) {
        previewColor = color;
    }

    public int getBackgroundColor() {
        return backrgroundColor;
    }

    public void setBackgroundColor(int color) {
        backrgroundColor = color;
    }
}