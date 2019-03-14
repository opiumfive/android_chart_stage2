package com.opiumfive.telechart.chart;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import com.opiumfive.telechart.chart.renderer.PreviewChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.PreviewChartTouchHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.renderer.PreviewLineChartRenderer;
import com.opiumfive.telechart.fastdraw.FastSurfaceView;
import com.opiumfive.telechart.fastdraw.FastTextureView;


public class PreviewLineChartView extends LineChartView {

    protected PreviewLineChartRenderer previewChartRenderer;

    public PreviewLineChartView(Context context) {
        this(context, null, 0);
    }

    public PreviewLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewLineChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chartViewportHandler = new PreviewChartViewportHandler();
        previewChartRenderer = new PreviewLineChartRenderer(context, this, this);
        touchHandler = new PreviewChartTouchHandler(context, this);
        setChartRenderer(previewChartRenderer);
        setLineChartData(LineChartData.generateDummyData());
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
