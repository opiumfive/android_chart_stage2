package com.opiumfive.telechart.chart.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.LineChartDataProvider;
import com.opiumfive.telechart.chart.util.ChartUtils;
import com.opiumfive.telechart.chart.LineChartView;


public class PreviewLineChartRenderer extends LineChartRenderer {

    private static final int DEFAULT_PREVIEW_TRANSPARENCY = 64;
    private static final int FULL_ALPHA = 255;
    private static final int DEFAULT_PREVIEW_STROKE_WIDTH_DP = 2;
    private static final float DEFAULT_MAX_ANGLE_VARIATION = 40f;

    private Paint previewPaint = new Paint();

    public PreviewLineChartRenderer(Context context, ILineChart chart, LineChartDataProvider dataProvider) {
        super(context, chart, dataProvider);
        previewPaint.setAntiAlias(false);
        previewPaint.setColor(Color.LTGRAY);
        previewPaint.setStrokeWidth(ChartUtils.dp2px(density, DEFAULT_PREVIEW_STROKE_WIDTH_DP));
        setMaxAngleVariation(DEFAULT_MAX_ANGLE_VARIATION);
    }

    @Override
    public void drawUnclipped(Canvas canvas) {
        super.drawUnclipped(canvas);
        final Viewport currentViewport = computator.getCurrentViewport();
        final float left = computator.computeRawX(currentViewport.left);
        final float top = computator.computeRawY(currentViewport.top);
        final float right = computator.computeRawX(currentViewport.right);
        final float bottom = computator.computeRawY(currentViewport.bottom);
        previewPaint.setAlpha(DEFAULT_PREVIEW_TRANSPARENCY);
        previewPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, previewPaint);
        previewPaint.setStyle(Paint.Style.STROKE);
        previewPaint.setAlpha(FULL_ALPHA);
        canvas.drawRect(left, top, right, bottom, previewPaint);
    }

    public int getPreviewColor() {
        return previewPaint.getColor();
    }

    public void setPreviewColor(int color) {
        previewPaint.setColor(color);
    }
}