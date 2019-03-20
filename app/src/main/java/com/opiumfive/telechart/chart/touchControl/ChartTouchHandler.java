package com.opiumfive.telechart.chart.touchControl;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.touchControl.ChartScroller.ScrollResult;
import com.opiumfive.telechart.chart.model.SelectedValues;
import com.opiumfive.telechart.chart.draw.LineChartRenderer;

public class ChartTouchHandler {

    protected ChartScroller chartScroller;
    protected IChart chart;
    protected ChartViewrectHandler chartViewrectHandler;
    protected LineChartRenderer renderer;

    protected boolean isScrollEnabled = true;
    protected boolean isValueTouchEnabled = true;

    protected SelectedValues selectedValues = new SelectedValues();

    protected ViewParent viewParent;

    public ChartTouchHandler(Context context, IChart chart) {
        this.chart = chart;
        this.chartViewrectHandler = chart.getChartViewrectHandler();
        this.renderer = chart.getChartRenderer();
        chartScroller = new ChartScroller(context);
    }

    public void resetTouchHandler() {
        this.chartViewrectHandler = chart.getChartViewrectHandler();
        this.renderer = chart.getChartRenderer();
    }

    public boolean computeScroll() {
        boolean needInvalidate = false;
        if (isScrollEnabled && chartScroller.computeScrollOffset(chartViewrectHandler)) {
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
}
