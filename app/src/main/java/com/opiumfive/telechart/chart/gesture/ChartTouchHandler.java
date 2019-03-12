package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewParent;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.renderer.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartScroller.ScrollResult;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.renderer.LineChartRenderer;

public class ChartTouchHandler {

    protected GestureDetectorCompat gestureDetector;
    protected ScaleGestureDetector scaleGestureDetector;
    protected ChartScroller chartScroller;
    protected ChartZoomer chartZoomer;
    protected ILineChart chart;
    protected ChartViewportHandler computator;
    protected LineChartRenderer renderer;

    protected boolean isZoomEnabled = true;
    protected boolean isScrollEnabled = true;
    protected boolean isValueTouchEnabled = true;
    protected boolean isValueSelectionEnabled = false;

    protected SelectedValue selectionModeOldValue = new SelectedValue();

    protected SelectedValue selectedValue = new SelectedValue();
    protected SelectedValue oldSelectedValue = new SelectedValue();

    protected ViewParent viewParent;

    public ChartTouchHandler(Context context, ILineChart chart) {
        this.chart = chart;
        this.computator = chart.getChartViewportHandler();
        this.renderer = chart.getChartRenderer();
        gestureDetector = new GestureDetectorCompat(context, new ChartGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ChartScaleGestureListener());
        chartScroller = new ChartScroller(context);
        chartZoomer = new ChartZoomer(context);
    }

    public void resetTouchHandler() {
        this.computator = chart.getChartViewportHandler();
        this.renderer = chart.getChartRenderer();
    }

    public boolean computeScroll() {
        boolean needInvalidate = false;
        if (isScrollEnabled && chartScroller.computeScrollOffset(computator)) {
            needInvalidate = true;
        }
        if (isZoomEnabled && chartZoomer.computeZoom(computator)) {
            needInvalidate = true;
        }
        return needInvalidate;
    }

    public boolean handleTouchEvent(MotionEvent event) {
        boolean needInvalidate = false;

        needInvalidate = gestureDetector.onTouchEvent(event) | scaleGestureDetector.onTouchEvent(event);

        if (isZoomEnabled && scaleGestureDetector.isInProgress()) {
            disallowParentInterceptTouchEvent();
        }

        if (isValueTouchEnabled) {
            needInvalidate = computeTouch(event) || needInvalidate;
        }

        return needInvalidate;
    }

    public boolean handleTouchEvent(MotionEvent event, ViewParent viewParent) {
        this.viewParent = viewParent;

        return handleTouchEvent(event);
    }

    private void disallowParentInterceptTouchEvent() {
        if (null != viewParent) {
            viewParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void allowParentInterceptTouchEvent(ScrollResult scrollResult) {
        if (null != viewParent && !scrollResult.canScrollX && !scaleGestureDetector.isInProgress()) {
            viewParent.requestDisallowInterceptTouchEvent(false);
        }
    }

    private boolean computeTouch(MotionEvent event) {
        boolean needInvalidate = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean wasTouched = renderer.isTouched();
                boolean isTouched = checkTouch(event.getX(), event.getY());
                if (wasTouched != isTouched) {
                    needInvalidate = true;

                    if (isValueSelectionEnabled) {
                        selectionModeOldValue.clear();
                        if (wasTouched && !renderer.isTouched()) {
                            chart.callTouchListener();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (renderer.isTouched()) {
                    if (checkTouch(event.getX(), event.getY())) {
                        if (isValueSelectionEnabled) {
                            if (!selectionModeOldValue.equals(selectedValue)) {
                                selectionModeOldValue.set(selectedValue);
                                chart.callTouchListener();
                            }
                        } else {
                            chart.callTouchListener();
                            renderer.clearTouch();
                        }
                    } else {
                        renderer.clearTouch();
                    }
                    needInvalidate = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (renderer.isTouched()) {
                    if (!checkTouch(event.getX(), event.getY())) {
                        renderer.clearTouch();
                        needInvalidate = true;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (renderer.isTouched()) {
                    renderer.clearTouch();
                    needInvalidate = true;
                }
                break;
        }
        return needInvalidate;
    }

    private boolean checkTouch(float touchX, float touchY) {
        oldSelectedValue.set(selectedValue);
        selectedValue.clear();

        if (renderer.checkTouch(touchX, touchY)) {
            selectedValue.set(renderer.getSelectedValue());
        }

        if (oldSelectedValue.isSet() && selectedValue.isSet() && !oldSelectedValue.equals(selectedValue)) {
            return false;
        } else {
            return renderer.isTouched();
        }
    }

    public boolean isZoomEnabled() {
        return isZoomEnabled;
    }

    public void setZoomEnabled(boolean isZoomEnabled) {
        this.isZoomEnabled = isZoomEnabled;

    }

    public boolean isScrollEnabled() {
        return isScrollEnabled;
    }

    public void setScrollEnabled(boolean isScrollEnabled) {
        this.isScrollEnabled = isScrollEnabled;
    }

    public boolean isValueTouchEnabled() {
        return isValueTouchEnabled;
    }

    public void setValueTouchEnabled(boolean isValueTouchEnabled) {
        this.isValueTouchEnabled = isValueTouchEnabled;
    }

    public boolean isValueSelectionEnabled() {
        return isValueSelectionEnabled;
    }

    public void setValueSelectionEnabled(boolean isValueSelectionEnabled) {
        this.isValueSelectionEnabled = isValueSelectionEnabled;
    }

    protected class ChartScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (isZoomEnabled) {
                float scale = 2.0f - detector.getScaleFactor();
                if (Float.isInfinite(scale)) {
                    scale = 1;
                }
                return chartZoomer.scale(computator, detector.getFocusX(), detector.getFocusY(), scale);
            }

            return false;
        }
    }

    protected class ChartGestureListener extends GestureDetector.SimpleOnGestureListener {

        protected ScrollResult scrollResult = new ScrollResult();

        @Override
        public boolean onDown(MotionEvent e) {
            if (isScrollEnabled) {
                disallowParentInterceptTouchEvent();
                return chartScroller.startScroll(computator);
            }

            return false;

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("scrollins", "elapsedTimeMs = " + System.currentTimeMillis());
            if (isScrollEnabled) {
                boolean canScroll = chartScroller.scroll(computator, distanceX, distanceY, scrollResult);
                allowParentInterceptTouchEvent(scrollResult);
                return canScroll;
            }

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isScrollEnabled) {
                return chartScroller.fling((int) -velocityX, (int) -velocityY, computator);
            }

            return false;
        }
    }

}
