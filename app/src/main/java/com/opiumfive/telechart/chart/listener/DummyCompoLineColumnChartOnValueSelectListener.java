package com.opiumfive.telechart.chart.listener;


import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SubcolumnValue;

public class DummyCompoLineColumnChartOnValueSelectListener implements ComboLineColumnChartOnValueSelectListener {

    @Override
    public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {

    }

    @Override
    public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {

    }

    @Override
    public void onValueDeselected() {

    }
}
