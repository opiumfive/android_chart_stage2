package com.opiumfive.telechart.chart.listener;


import com.opiumfive.telechart.chart.model.SliceValue;

public interface PieChartOnValueSelectListener extends OnValueDeselectListener {

    public void onValueSelected(int arcIndex, SliceValue value);

}
