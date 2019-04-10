package com.opiumfive.telechart.chart.animator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.opiumfive.telechart.chart.IChart;

public class ChartMorphAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private final static int FAST_ANIMATION_DURATION = 350;

    private final IChart chart;
    private ValueAnimator animator;
    private ChartAnimationListener animationListener;
    private TimeInterpolator lineInterpolator = new LinearInterpolator();

    public ChartMorphAnimator(IChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setInterpolator(lineInterpolator);
        animator.setDuration(FAST_ANIMATION_DURATION);
    }

    public void startAnimation() {
        animator.setDuration(FAST_ANIMATION_DURATION);
        animator.start();
    }

    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float scale = animation.getAnimatedFraction();
        chart.postDrawWithMorphFactor(scale);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (animationListener != null) {
            animationListener.onAnimationFinished();
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (animationListener != null) {
            animationListener.onAnimationStarted();
        }
    }

    public boolean isAnimationStarted() {
        return animator.isStarted();
    }

    public void setChartAnimationListener(ChartAnimationListener animationListener) {
        this.animationListener = animationListener;
    }
}