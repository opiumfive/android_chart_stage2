package com.opiumfive.telechart.chart.listener;


import com.opiumfive.telechart.chart.model.PointValue;

public interface LineChartOnValueSelectListener extends OnValueDeselectListener {

    public void onValueSelected(int lineIndex, int pointIndex, PointValue value);

}
