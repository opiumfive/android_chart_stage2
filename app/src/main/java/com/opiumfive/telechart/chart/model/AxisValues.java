package com.opiumfive.telechart.chart.model;


import java.util.Arrays;

public class AxisValues {

    public float[] values = new float[]{};
    public float[] rawValues = new float[]{};
    public int valuesNumber = 0;
    public float alpha = 1.0f;
    public int step = 0;

    public AxisValues() {
    }

    public AxisValues(AxisValues axisValues) {
        this.values = Arrays.copyOf(axisValues.values, axisValues.values.length);
        this.rawValues = Arrays.copyOf(axisValues.rawValues, axisValues.rawValues.length);
        this.valuesNumber = axisValues.valuesNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        AxisValues other = (AxisValues) obj;
        float percent = Math.abs(values[values.length - 1] - values[0]) * 0.0001f;
        return valuesNumber == other.valuesNumber && Math.abs(values[0] - other.values[0]) <= percent &&
                Math.abs(values[values.length - 1] - other.values[values.length - 1]) <= percent;
    }
}
