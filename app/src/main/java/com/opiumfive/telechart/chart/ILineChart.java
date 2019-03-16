package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartTouchHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.render.AxesRenderer;
import com.opiumfive.telechart.chart.render.LineChartRenderer;

public interface ILineChart {

    public LineChartData getChartData();
    public LineChartRenderer getChartRenderer();
    public void setChartRenderer(LineChartRenderer renderer);
    public AxesRenderer getAxesRenderer();
    public ChartViewportHandler getChartViewportHandler();
    public ChartTouchHandler getTouchHandler();
    public void setCurrentViewport(Viewport targetViewport);

    public void animationDataUpdate(float scale);
    public void animationDataFinished();
    public Viewport getMaximumViewport();
    public void setZoomEnabled(boolean isZoomEnabled);
    public void setScrollEnabled(boolean isScrollEnabled);
}
