package com.opiumfive.telechart.chart.listener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartViewrectAnimator implements AnimatorListener, AnimatorUpdateListener {

    private final static int FAST_ANIMATION_DURATION = 300;

    private final ILineChart chart;
    private ValueAnimator animator;
    private Viewrect startViewrect = new Viewrect();
    private Viewrect targetViewrect = new Viewrect();
    private Viewrect newViewrect = new Viewrect();
    private ChartAnimationListener animationListener;

    public ChartViewrectAnimator(ILineChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setDuration(FAST_ANIMATION_DURATION);
    }

    public void startAnimation(Viewrect startViewrect, Viewrect targetViewrect) {
        this.startViewrect.set(startViewrect);
        this.targetViewrect.set(targetViewrect);
        animator.setDuration(FAST_ANIMATION_DURATION);
        animator.start();
    }

    public void startAnimation(Viewrect startViewrect, Viewrect targetViewrect, long duration) {
        this.startViewrect.set(startViewrect);
        this.targetViewrect.set(targetViewrect);
        animator.setDuration(duration);
        animator.start();
    }

    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float scale = animation.getAnimatedFraction();
        float diffLeft = (targetViewrect.left - startViewrect.left) * scale;
        float diffTop = (targetViewrect.top - startViewrect.top) * scale;
        float diffRight = (targetViewrect.right - startViewrect.right) * scale;
        float diffBottom = (targetViewrect.bottom - startViewrect.bottom) * scale;
        newViewrect.set(startViewrect.left + diffLeft, startViewrect.top + diffTop, startViewrect.right + diffRight, startViewrect.bottom + diffBottom);
        chart.setCurrentViewport(newViewrect);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        chart.setCurrentViewport(targetViewrect);
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
