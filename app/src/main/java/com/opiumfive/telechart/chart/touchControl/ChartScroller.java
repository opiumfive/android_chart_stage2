package com.opiumfive.telechart.chart.touchControl;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.Scroller;

import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartScroller {

    private Viewrect scrollerStartViewrect = new Viewrect();
    private Point surfaceSizeBuffer = new Point();
    private Scroller scroller;
    private PreviewChartTouchHandler.ScrollMode currentScrollMode;

    public ChartScroller(Context context) {
        scroller = new Scroller(context);
    }

    public boolean startScroll(ChartViewrectHandler chartViewrectHandler, PreviewChartTouchHandler.ScrollMode scrollMode) {
        scroller.abortAnimation();
        currentScrollMode = scrollMode;
        scrollerStartViewrect.set(chartViewrectHandler.getCurrentViewrect());
        return true;
    }

    public boolean scroll(ChartViewrectHandler chartViewrectHandler, float distanceX, ScrollResult scrollResult) {

        final Viewrect maxViewrect = chartViewrectHandler.getMaximumViewport();
        final Viewrect visibleViewrect = chartViewrectHandler.getVisibleViewport();
        final Viewrect currentViewrect = chartViewrectHandler.getCurrentViewrect();
        final Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();

        final boolean canScrollLeft = currentViewrect.left > maxViewrect.left;
        final boolean canScrollRight = currentViewrect.right < maxViewrect.right;

        boolean canScrollX = canScrollLeft && distanceX <= 0 || canScrollRight && distanceX >= 0;

        float viewportOffsetX = distanceX * visibleViewrect.width() / contentRect.width();

        if (currentScrollMode == null) return false;

        switch (currentScrollMode) {
            case FULL:
                if (canScrollX) {
                    chartViewrectHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
                    chartViewrectHandler.setViewportTopLeft(currentViewrect.left + viewportOffsetX, currentViewrect.top, distanceX);
                }
                break;
            case LEFT_SIDE:
                chartViewrectHandler.setCurrentViewport(currentViewrect.left + viewportOffsetX, currentViewrect.top, currentViewrect.right, currentViewrect.bottom);
                canScrollX = true;
                break;
            case RIGHT_SIDE:
                chartViewrectHandler.setCurrentViewport(currentViewrect.left, currentViewrect.top, currentViewrect.right + viewportOffsetX, currentViewrect.bottom);
                canScrollX = true;
                break;
        }

        scrollResult.canScrollX = canScrollX;

        return canScrollX;
    }

    public boolean computeScrollOffset(ChartViewrectHandler chartViewrectHandler) {
        if (scroller.computeScrollOffset()) {
            final Viewrect maxViewrect = chartViewrectHandler.getMaximumViewport();

            chartViewrectHandler.computeScrollSurfaceSize(surfaceSizeBuffer);

            final float currXRange = maxViewrect.left + maxViewrect.width() * scroller.getCurrX() / surfaceSizeBuffer.x;
            final float currYRange = maxViewrect.top - maxViewrect.height() * scroller.getCurrY() / surfaceSizeBuffer.y;

            chartViewrectHandler.setViewportTopLeft(currXRange, currYRange, 0f);

            return true;
        }

        return false;
    }

    public boolean fling(int velocityX, int velocityY, ChartViewrectHandler chartViewrectHandler) {
        chartViewrectHandler.computeScrollSurfaceSize(surfaceSizeBuffer);
        scrollerStartViewrect.set(chartViewrectHandler.getCurrentViewrect());

        int startX = (int) (surfaceSizeBuffer.x * (scrollerStartViewrect.left - chartViewrectHandler.getMaximumViewport().left) / chartViewrectHandler.getMaximumViewport().width());
        int startY = (int) (surfaceSizeBuffer.y * (chartViewrectHandler.getMaximumViewport().top - scrollerStartViewrect.top) / chartViewrectHandler.getMaximumViewport().height());

        scroller.forceFinished(true);

        final int width = chartViewrectHandler.getContentRectMinusAllMargins().width();
        final int height = chartViewrectHandler.getContentRectMinusAllMargins().height();
        scroller.fling(startX, startY, velocityX, velocityY, 0, surfaceSizeBuffer.x - width + 1, 0, surfaceSizeBuffer.y - height + 1);
        return true;
    }

    public static class ScrollResult {
        public boolean canScrollX;
    }

}
