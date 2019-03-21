package com.opiumfive.telechart.chart.animator;

import com.opiumfive.telechart.chart.model.Viewrect;

public interface ViewrectChangeListener {

    public void onViewportChanged(Viewrect viewrect, float distanceX);

}
