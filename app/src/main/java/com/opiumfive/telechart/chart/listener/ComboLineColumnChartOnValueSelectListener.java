package com.opiumfive.telechart.chart.listener;


import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SubcolumnValue;

public interface ComboLineColumnChartOnValueSelectListener extends OnValueDeselectListener {

    public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value);

    public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value);

}
