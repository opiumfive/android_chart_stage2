package com.opiumfive.telechart.chart.draw;

import com.opiumfive.telechart.chart.model.Viewrect;

public class PreviewChartViewrectHandler extends ChartViewrectHandler {

    public PreviewChartViewrectHandler(float density) {
        super(density);
    }

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
}