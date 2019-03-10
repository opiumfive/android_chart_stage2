package com.opiumfive.telechart.chart.listener;


import com.opiumfive.telechart.chart.model.BubbleValue;

public interface BubbleChartOnValueSelectListener extends OnValueDeselectListener {

    public void onValueSelected(int bubbleIndex, BubbleValue value);

}
