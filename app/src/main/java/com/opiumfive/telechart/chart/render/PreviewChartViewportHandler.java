package com.opiumfive.telechart.chart.render;

import com.opiumfive.telechart.chart.model.Viewrect;

public class PreviewChartViewportHandler extends ChartViewportHandler {

    public float computeRawX(float valueX) {
        final float pixelOffset = (valueX - maxViewrect.left) * (contentRectMinusAllMargins.width() / maxViewrect.width());
        return contentRectMinusAllMargins.left + pixelOffset;
    }

    public float computeRawY(float valueY) {
        final float pixelOffset = (valueY - maxViewrect.bottom) * (contentRectMinusAllMargins.height() / maxViewrect.height());
        return contentRectMinusAllMargins.bottom - pixelOffset;
    }

    public Viewrect getVisibleViewport() {
        return maxViewrect;
    }

    public void constrainViewport(float left, float top, float right, float bottom) {
        super.constrainViewport(left, top, right, bottom);
        if (viewrectChangeListener != null) {
            viewrectChangeListener.onViewportChanged(currentViewrect);
        }
    }
}