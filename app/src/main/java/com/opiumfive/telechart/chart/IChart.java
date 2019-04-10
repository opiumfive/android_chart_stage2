package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.draw.LineChartRenderer;

public interface IChart {

    LineChartData getChartData();
    LineChartRenderer getChartRenderer();
    void setChartRenderer(LineChartRenderer renderer);
    ChartViewrectHandler getChartViewrectHandler();
    void setCurrentViewrect(Viewrect targetViewrect);
    void setMaximumViewrect(Viewrect maximumViewrect);
    Viewrect getMaximumViewrect();
    CType getType();
    CType getTargetType();
    void setScrollEnabled(boolean isScrollEnabled);
    void toggleAxisAnim(Viewrect targetViewrect);
    void postDrawIfNeeded();
    void postDrawWithMorphFactor(float factor);
    void startMorphling(CType cType);
}
