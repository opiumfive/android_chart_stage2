package com.opiumfive.telechart.chart.touchControl;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.Scroller;

import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartScroller {

    private Viewrect scrollerStartViewrect = new Viewrect();
    private Point surfaceSizeBuffer = new Point();
    private Scroller scroller;
    private PreviewChartTouchHandler.ScrollMode currentScrollMode;

    public ChartScroller(Context context) {
        scroller = new Scroller(context);
    }

    public boolean startScroll(ChartViewportHandler chartViewportHandler, PreviewChartTouchHandler.ScrollMode scrollMode) {
        scroller.abortAnimation();
        currentScrollMode = scrollMode;
        scrollerStartViewrect.set(chartViewportHandler.getCurrentViewrect());
        return true;
    }

    public boolean scroll(ChartViewportHandler chartViewportHandler, float distanceX, float distanceY, ScrollResult scrollResult) {

        final Viewrect maxViewrect = chartViewportHandler.getMaximumViewport();
        final Viewrect visibleViewrect = chartViewportHandler.getVisibleViewport();
        final Viewrect currentViewrect = chartViewportHandler.getCurrentViewrect();
        final Rect contentRect = chartViewportHandler.getContentRectMinusAllMargins();

        final boolean canScrollLeft = currentViewrect.left > maxViewrect.left;
        final boolean canScrollRight = currentViewrect.right < maxViewrect.right;


        boolean canScrollX = canScrollLeft && distanceX <= 0 || canScrollRight && distanceX >= 0;

        if (canScrollX) {

            float viewportOffsetX = distanceX * visibleViewrect.width() / contentRect.width();

            switch (currentScrollMode) {
                case FULL:
                    chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
                    chartViewportHandler.setViewportTopLeft(currentViewrect.left + viewportOffsetX, currentViewrect.top);
                    break;
                case LEFT_SIDE:
                    chartViewportHandler.setCurrentViewport(currentViewrect.left + viewportOffsetX, currentViewrect.top, currentViewrect.right, currentViewrect.bottom);
                    break;
                case RIGHT_SIDE:
                    chartViewportHandler.setCurrentViewport(currentViewrect.left, currentViewrect.top, currentViewrect.right + viewportOffsetX, currentViewrect.bottom);
                    break;
            }

        }

        scrollResult.canScrollX = canScrollX;

        return canScrollX;
    }

    public boolean computeScrollOffset(ChartViewportHandler chartViewportHandler) {
        if (scroller.computeScrollOffset()) {
            final Viewrect maxViewrect = chartViewportHandler.getMaximumViewport();

            chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);

            final float currXRange = maxViewrect.left + maxViewrect.width() * scroller.getCurrX() / surfaceSizeBuffer.x;
            final float currYRange = maxViewrect.top - maxViewrect.height() * scroller.getCurrY() / surfaceSizeBuffer.y;

            chartViewportHandler.setViewportTopLeft(currXRange, currYRange);

            return true;
        }

        return false;
    }

    public boolean fling(int velocityX, int velocityY, ChartViewportHandler chartViewportHandler) {
        chartViewportHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
        scrollerStartViewrect.set(chartViewportHandler.getCurrentViewrect());

        int startX = (int) (surfaceSizeBuffer.x * (scrollerStartViewrect.left - chartViewportHandler.getMaximumViewport().left) / chartViewportHandler.getMaximumViewport().width());
        int startY = (int) (surfaceSizeBuffer.y * (chartViewportHandler.getMaximumViewport().top - scrollerStartViewrect.top) / chartViewportHandler.getMaximumViewport().height());

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
