package com.opiumfive.telechart.chart.animator;

import android.view.animation.Interpolator;

public class AreaInInterpolator implements Interpolator {

    public AreaInInterpolator() {}

    //EaseBackIn
    public float getInterpolation(float input) {
        return input * input * ((1.70158f + 1) * input - 1.70158f);
    }
}