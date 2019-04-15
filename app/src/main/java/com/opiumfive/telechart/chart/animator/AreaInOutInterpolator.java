package com.opiumfive.telechart.chart.animator;

import android.view.animation.Interpolator;

public class AreaInOutInterpolator implements Interpolator {

    public AreaInOutInterpolator() {}

    //EaseBackInOut
    public float getInterpolation(float input) {
        float s = 1.70158f;
        if ((input *= 2) < 1) {
            return 0.5f * (input * input * (((s *= (1.525f)) + 1) * input - s));
        }

        return 0.5f * ((input -= 2) * input * (((s *= (1.525f)) + 1) * input + s) + 2);
    }
}
