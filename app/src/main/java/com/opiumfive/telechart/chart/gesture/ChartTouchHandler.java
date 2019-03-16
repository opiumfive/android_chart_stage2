package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.renderer.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartScroller.ScrollResult;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.renderer.LineChartRenderer;

public class ChartTouchHandler {


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

        if (isValueTouchEnabled) {
            needInvalidate = computeTouch(event);
        }

        return needInvalidate;
    }

    public boolean handleTouchEvent(MotionEvent event, ViewParent viewParent) {
        this.viewParent = viewParent;

        return handleTouchEvent(event);
    }

    protected void disallowParentInterceptTouchEvent() {
        if (null != viewParent) {
            viewParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    protected void allowParentInterceptTouchEvent(ScrollResult scrollResult) {
        if (null != viewParent && !scrollResult.canScrollX) {
            viewParent.requestDisallowInterceptTouchEvent(false);
        }
    }

    protected boolean computeTouch(MotionEvent event) {
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
}
