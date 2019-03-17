package com.opiumfive.telechart.chart;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import com.opiumfive.telechart.chart.render.PreviewChartViewportHandler;
import com.opiumfive.telechart.chart.touchControl.PreviewChartTouchHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.render.PreviewLineChartRenderer;


public class PreviewChartView extends ChartView {

    protected PreviewLineChartRenderer previewChartRenderer;

    public PreviewChartView(Context context) {
        this(context, null, 0);
    }

    public PreviewChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chartViewportHandler = new PreviewChartViewportHandler();
        previewChartRenderer = new PreviewLineChartRenderer(context, this, this);
        touchHandler = new PreviewChartTouchHandler(context, this);
        setChartRenderer(previewChartRenderer);
        setChartData(LineChartData.generateDummyData());
        //updateCanvasDrawer();
    }

    public int getPreviewColor() {
        return previewChartRenderer.getPreviewColor();
    }

    public void setPreviewColor(int color) {
        previewChartRenderer.setPreviewColor(color);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public int getPreviewBackgroundColor() {
        return previewChartRenderer.getBackgroundColor();
    }

    public void setPreviewBackgroundColor(int color) {
        previewChartRenderer.setBackgroundColor(color);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
}
