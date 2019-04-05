package com.opiumfive.telechart.chart.animator;

import android.animation.Animator;
import android.animation.ValueAnimator;

import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartLabelAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    private final static int FAST_ANIMATION_DURATION = 550;

    private final IChart chart;
    private ValueAnimator animator;
    private ChartAnimationListener animationListener;

    public ChartLabelAnimator(IChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setDuration(FAST_ANIMATION_DURATION);
    }

    public void startAnimation( Viewrect targetViewrect) {
        animator.setDuration(FAST_ANIMATION_DURATION);
        animator.start();
        chart.toggleAxisAnim(targetViewrect);
    }

    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        chart.postDrawIfNeeded();
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
