package com.opiumfive.telechart.chart.touchControl;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.model.Viewrect;


public class PreviewChartTouchHandler extends ChartTouchHandler {

    private static final float SIDE_DRAG_ZONE = 0.05f; // percent from width

    private GestureDetectorCompat gestureDetector;
    private PreviewChartGestureListener gestureListener;

    private float sideDragZone;

    public PreviewChartTouchHandler(Context context, IChart chart) {
        super(context, chart);
        gestureListener = new PreviewChartGestureListener();
        gestureDetector = new GestureDetectorCompat(context, gestureListener);

        isValueTouchEnabled = false;
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        Viewrect currentViewrect = chartViewrectHandler.getCurrentViewrect();
        final Viewrect maxViewrect = chartViewrectHandler.getMaximumViewport();
        final float left = chartViewrectHandler.computeRawX(currentViewrect.left);
        final float right = chartViewrectHandler.computeRawX(currentViewrect.right);

        float touchX = event.getX();

        sideDragZone = chartViewrectHandler.computeSideScrollTrigger(SIDE_DRAG_ZONE);

        if (Math.abs(left - touchX) <= sideDragZone) {
            if (currentViewrect.left >= maxViewrect.left) {
                gestureListener.setScrollMode(ScrollMode.LEFT_SIDE);
                return gestureDetector.onTouchEvent(event);
            }
        } else if (Math.abs(right - touchX) <= sideDragZone) {
            if (currentViewrect.right <= maxViewrect.right) {
                gestureListener.setScrollMode(ScrollMode.RIGHT_SIDE);
                return gestureDetector.onTouchEvent(event);
            }
        } else if (touchX > left && touchX < right) {
            gestureListener.setScrollMode(ScrollMode.FULL);
            return gestureDetector.onTouchEvent(event);
        } else {
            return false;
        }

        return false;
    }

    protected class PreviewChartGestureListener extends GestureDetector.SimpleOnGestureListener {

        protected ChartScroller.ScrollResult scrollResult = new ChartScroller.ScrollResult();
        private ScrollMode scrollMode = ScrollMode.FULL;

        @Override
        public boolean onDown(MotionEvent e) {
            if (isScrollEnabled) {
                disallowParentInterceptTouchEvent();
                return chartScroller.startScroll(chartViewrectHandler, scrollMode);
            }

            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScrollEnabled) {
                boolean canScroll = chartScroller.scroll(chartViewrectHandler, -distanceX, scrollResult);
                allowParentInterceptTouchEvent(scrollResult);
                return canScroll;
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isScrollEnabled && scrollMode == ScrollMode.FULL) {
                return chartScroller.fling((int) velocityX, (int) velocityY, chartViewrectHandler);
            }

            return false;
        }

        public ScrollMode getScrollMode() {
            return scrollMode;
        }

        public void setScrollMode(ScrollMode scrollMode) {
            this.scrollMode = scrollMode;
        }
    }

    public enum ScrollMode { FULL, LEFT_SIDE, RIGHT_SIDE }

}
