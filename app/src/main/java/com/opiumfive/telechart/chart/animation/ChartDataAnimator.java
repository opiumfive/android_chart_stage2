package com.opiumfive.telechart.chart.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;

import com.opiumfive.telechart.chart.view.Chart;

@SuppressLint("NewApi")
public class ChartDataAnimator implements AnimatorListener, AnimatorUpdateListener {

    private final static int DEFAULT_ANIMATION_DURATION = 500;

    private final Chart chart;
    private ValueAnimator animator;
    private ChartAnimationListener animationListener = new DummyChartAnimationListener();

    public ChartDataAnimator(Chart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
    }

    public void startAnimation(long duration) {
        if (duration >= 0) {
            animator.setDuration(duration);
        } else {
            animator.setDuration(DEFAULT_ANIMATION_DURATION);
        }
        animator.start();
    }

    public void cancelAnimation() {
        animator.cancel();
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        chart.animationDataUpdate(animation.getAnimatedFraction());
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        chart.animationDataFinished();
        animationListener.onAnimationFinished();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
        animationListener.onAnimationStarted();
    }

    public boolean isAnimationStarted() {
        return animator.isStarted();
    }

    public void setChartAnimationListener(ChartAnimationListener animationListener) {
        if (null == animationListener) {
            this.animationListener = new DummyChartAnimationListener();
        } else {
            this.animationListener = animationListener;
        }
    }

}
