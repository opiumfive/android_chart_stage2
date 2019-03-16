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
    public void callTouchListener();
    public void setCurrentViewport(Viewport targetViewport);

    public void animationDataUpdate(float scale);
    public void animationDataFinished();
    public Viewport getMaximumViewport();
    public void setZoomEnabled(boolean isZoomEnabled);
    public void setScrollEnabled(boolean isScrollEnabled);

    /*public void startDataAnimation();
    public void startDataAnimation(long duration);
    public void cancelDataAnimation();
    public boolean isViewportCalculationEnabled();
    public void setViewportCalculationEnabled(boolean isEnabled);
    public void setDataAnimationListener(ChartAnimationListener animationListener);
    public void setViewportAnimationListener(ChartAnimationListener animationListener);
    public void setViewportChangeListener(ViewportChangeListener viewportChangeListener);

    public boolean isInteractive();
    public void setInteractive(boolean isInteractive);
    public boolean isZoomEnabled();

    public boolean isScrollEnabled();

    public void moveTo(float x, float y);
    public void moveToWithAnimation(float x, float y);
    public ZoomType getZoomType();
    public void setZoomType(ZoomType zoomType);
    public float getMaxZoom();
    public void setMaxZoom(float maxZoom);
    public float getZoomLevel();
    public void setZoomLevel(float x, float y, float zoomLevel);
    public void setZoomLevelWithAnimation(float x, float y, float zoomLevel);
    public boolean isValueTouchEnabled();
    public void setValueTouchEnabled(boolean isValueTouchEnabled);

    public void setMaximumViewport(Viewport maxViewport);
    public Viewport getCurrentViewport();

    public void setCurrentViewportWithAnimation(Viewport targetViewport);
    public void setCurrentViewportWithAnimation(Viewport targetViewport, long duration);
    public void resetViewports();
    public boolean isValueSelectionEnabled();
    public void setValueSelectionEnabled(boolean isValueSelectionEnabled);
    public void selectValue(SelectedValue selectedValue);
    public SelectedValue getSelectedValue();
    public boolean isContainerScrollEnabled();
    public void setContainerScrollEnabled(boolean isContainerScrollEnabled, ContainerScrollType containerScrollType);*/
}
