package com.opiumfive.telechart.chart.renderer;

import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.renderer.ChartViewportHandler;

public class PreviewChartViewportHandler extends ChartViewportHandler {

    public float computeRawX(float valueX) {
        final float pixelOffset = (valueX - maxViewport.left) * (contentRectMinusAllMargins.width() / maxViewport.width());
        return contentRectMinusAllMargins.left + pixelOffset;
    }

    public float computeRawY(float valueY) {
        final float pixelOffset = (valueY - maxViewport.bottom) * (contentRectMinusAllMargins.height() / maxViewport.height());
        return contentRectMinusAllMargins.bottom - pixelOffset;
    }

    public Viewport getVisibleViewport() {
        return maxViewport;
    }

    public void constrainViewport(float left, float top, float right, float bottom) {
        super.constrainViewport(left, top, right, bottom);
        viewportChangeListener.onViewportChanged(currentViewport);
    }
}