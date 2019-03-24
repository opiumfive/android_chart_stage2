package com.opiumfive.telechart.chart.model;


import android.support.annotation.Nullable;

import java.util.Arrays;

public class AxisValues {

    public float[] values = new float[]{};
    public int valuesNumber = 0;
    public float alpha = 1.0f;
    public int step = 0;

    public AxisValues() {
    }

    public AxisValues(AxisValues axisValues) {
        this.values = Arrays.copyOf(axisValues.values, axisValues.values.length);
        this.valuesNumber = axisValues.valuesNumber;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        AxisValues other = (AxisValues) obj;
        return valuesNumber == other.valuesNumber && Arrays.equals(values, other.values);
    }
}
