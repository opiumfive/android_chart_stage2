package com.opiumfive.telechart.chart.animation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.LineChartView;

public class ChartViewportAnimator implements AnimatorListener, AnimatorUpdateListener {

    private final static int FAST_ANIMATION_DURATION = 300;

    private final ILineChart chart;
    private ValueAnimator animator;
    private Viewport startViewport = new Viewport();
    private Viewport targetViewport = new Viewport();
    private Viewport newViewport = new Viewport();
    private ChartAnimationListener animationListener = new DefaultChartAnimationListener();

    public ChartViewportAnimator(ILineChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setDuration(FAST_ANIMATION_DURATION);
    }

    public void startAnimation(Viewport startViewport, Viewport targetViewport) {
        this.startViewport.set(startViewport);
        this.targetViewport.set(targetViewport);
        animator.setDuration(FAST_ANIMATION_DURATION);
        animator.start();
    }

    public void startAnimation(Viewport startViewport, Viewport targetViewport, long duration) {
        this.startViewport.set(startViewport);
        this.targetViewport.set(targetViewport);
        animator.setDuration(duration);
        animator.start();
    }

    public void cancelAnimation() {
        animator.cancel();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float scale = animation.getAnimatedFraction();
        float diffLeft = (targetViewport.left - startViewport.left) * scale;
        float diffTop = (targetViewport.top - startViewport.top) * scale;
        float diffRight = (targetViewport.right - startViewport.right) * scale;
        float diffBottom = (targetViewport.bottom - startViewport.bottom) * scale;
        newViewport.set(startViewport.left + diffLeft, startViewport.top + diffTop, startViewport.right + diffRight,
                startViewport.bottom + diffBottom);
        chart.setCurrentViewport(newViewport);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        chart.setCurrentViewport(targetViewport);
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
            this.animationListener = new DefaultChartAnimationListener();
        } else {
            this.animationListener = animationListener;
        }
    }

}
