package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.draw.LineChartRenderer;

public interface ILineChart {

    LineChartData getChartData();
    LineChartRenderer getChartRenderer();
    void setChartRenderer(LineChartRenderer renderer);
    ChartViewrectHandler getChartViewrectHandler();
    void setCurrentViewrect(Viewrect targetViewrect);
    Viewrect getMaximumViewrect();
    void setScrollEnabled(boolean isScrollEnabled);
}
