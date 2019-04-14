package com.opiumfive.telechart.chart;

import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.draw.GodChartRenderer;

public interface IChart {

    LineChartData getChartData();
    GodChartRenderer getChartRenderer();
    void setChartRenderer(GodChartRenderer renderer);
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
    boolean isSelectionOnHover();
}
