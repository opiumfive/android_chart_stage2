package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.Scroller;

import com.opiumfive.telechart.chart.renderer.ChartViewportHandler;
import com.opiumfive.telechart.chart.model.Viewport;

public class ChartScroller {

    private Viewport scrollerStartViewport = new Viewport();
    private Point surfaceSizeBuffer = new Point();
    private Scroller scroller;
    private PreviewChartTouchHandler.ScrollMode currentScrollMode;

    public ChartScroller(Context context) {
        scroller = new Scroller(context);
    }

    public boolean startScroll(ChartViewportHandler chartViewportHandler, PreviewChartTouchHandler.ScrollMode scrollMode) {
        scroller.abortAnimation();
        currentScrollMode = scrollMode;
        scrollerStartViewport.set(chartViewportHandler.getCurrentViewport());
        return true;
    }

    public boolean scroll(ChartViewportHandler chartViewportHandler, float distanceX, float distanceY, ScrollResult scrollResult) {

        final Viewport maxViewport = chartViewportHandler.getMaximumViewport();
        final Viewport visibleViewport = chartViewportHandler.getVisibleViewport();
        final Viewport currentViewport = chartViewportHandler.getCurrentViewport();
        final Rect contentRect = chartViewportHandler.getContentRectMinusAllMargins();

        final boolean canScrollLeft = currentViewport.left > maxViewport.left;
        final boolean canScrollRight = currentViewport.right < maxViewport.right;


        boolean canScrollX = canScrollLeft && distanceX <= 0 || canScrollRight && distanceX >= 0;

        if (canScrollX) {

            float viewportOffsetX = distanceX * visibleViewport.width() / contentRect.width();

            switch (currentScrollMode) {
                case FULL:
                    chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
                    chartViewportHandler.setViewportTopLeft(currentViewport.left + viewportOffsetX, currentViewport.top);
                    break;
                case LEFT_SIDE:
                    chartViewportHandler.setCurrentViewport(currentViewport.left + viewportOffsetX, currentViewport.top, currentViewport.right, currentViewport.bottom);
                    break;
                case RIGHT_SIDE:
                    chartViewportHandler.setCurrentViewport(currentViewport.left, currentViewport.top, currentViewport.right + viewportOffsetX, currentViewport.bottom);
                    break;
            }

        }

        scrollResult.canScrollX = canScrollX;

        return canScrollX;
    }

    public boolean computeScrollOffset(ChartViewportHandler chartViewportHandler) {
        if (scroller.computeScrollOffset()) {
            final Viewport maxViewport = chartViewportHandler.getMaximumViewport();

            chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);

            final float currXRange = maxViewport.left + maxViewport.width() * scroller.getCurrX() / surfaceSizeBuffer.x;
            final float currYRange = maxViewport.top - maxViewport.height() * scroller.getCurrY() / surfaceSizeBuffer.y;

            chartViewportHandler.setViewportTopLeft(currXRange, currYRange);

            return true;
        }

        return false;
    }

    public boolean fling(int velocityX, int velocityY, ChartViewportHandler chartViewportHandler) {
        chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
        scrollerStartViewport.set(chartViewportHandler.getCurrentViewport());

        int startX = (int) (surfaceSizeBuffer.x * (scrollerStartViewport.left - chartViewportHandler.getMaximumViewport().left) / chartViewportHandler.getMaximumViewport().width());
        int startY = (int) (surfaceSizeBuffer.y * (chartViewportHandler.getMaximumViewport().top - scrollerStartViewport.top) / chartViewportHandler.getMaximumViewport().height());

        scroller.forceFinished(true);

        final int width = chartViewportHandler.getContentRectMinusAllMargins().width();
        final int height = chartViewportHandler.getContentRectMinusAllMargins().height();
        scroller.fling(startX, startY, velocityX, velocityY, 0, surfaceSizeBuffer.x - width + 1, 0, surfaceSizeBuffer.y - height + 1);
        return true;
    }

    public static class ScrollResult {
        public boolean canScrollX;
    }

}
