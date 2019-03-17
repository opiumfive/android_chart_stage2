package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartScroller.ScrollResult;
import com.opiumfive.telechart.chart.model.SelectedValues;
import com.opiumfive.telechart.chart.render.LineChartRenderer;

public class ChartTouchHandler {


    protected ChartScroller chartScroller;
    protected ChartZoomer chartZoomer;
    protected ILineChart chart;
    protected ChartViewportHandler chartViewportHandler;
    protected LineChartRenderer renderer;

    protected boolean isZoomEnabled = true;
    protected boolean isScrollEnabled = true;
    protected boolean isValueTouchEnabled = true;
    protected boolean isValueSelectionEnabled = false;

    protected SelectedValues selectedValues = new SelectedValues();

    protected ViewParent viewParent;

    public ChartTouchHandler(Context context, ILineChart chart) {
        this.chart = chart;
        this.chartViewportHandler = chart.getChartViewportHandler();
        this.renderer = chart.getChartRenderer();
        chartScroller = new ChartScroller(context);
        chartZoomer = new ChartZoomer(context);
    }

    public void resetTouchHandler() {
        this.chartViewportHandler = chart.getChartViewportHandler();
        this.renderer = chart.getChartRenderer();
    }

    public boolean computeScroll() {
        boolean needInvalidate = false;
        if (isScrollEnabled && chartScroller.computeScrollOffset(chartViewportHandler)) {
            needInvalidate = true;
        }
        if (isZoomEnabled && chartZoomer.computeZoom(chartViewportHandler)) {
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
                boolean isTouched = checkTouch(event.getX());
                if (isTouched) {
                    needInvalidate = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (renderer.isTouched()) {
                    renderer.clearTouch();
                    needInvalidate = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isTouched = checkTouch(event.getX());
                if (isTouched) {
                    needInvalidate = true;
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

    private boolean checkTouch(float touchX) {
        selectedValues.clear();

        if (renderer.checkTouch(touchX)) {
            selectedValues.set(renderer.getSelectedValues());
        }

        return renderer.isTouched();
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
