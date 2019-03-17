package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.render.ChartViewrectHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.render.LineChartRenderer;

public interface ILineChart {

    LineChartData getChartData();
    LineChartRenderer getChartRenderer();
    void setChartRenderer(LineChartRenderer renderer);
    ChartViewrectHandler getChartViewrectHandler();
    void setCurrentViewrect(Viewrect targetViewrect);
    Viewrect getMaximumViewrect();
    void setScrollEnabled(boolean isScrollEnabled);
}
