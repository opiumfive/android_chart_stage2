package com.opiumfive.telechart.chart.listener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartViewrectAnimator implements AnimatorListener, AnimatorUpdateListener {

    private final static int FAST_ANIMATION_DURATION = 300;

    private final ILineChart chart;
    private ValueAnimator animator;
    private Viewrect startViewrect = new Viewrect();
    private Viewrect targetViewrect = new Viewrect();
    private Viewrect newViewrect = new Viewrect();
    private ChartAnimationListener animationListener;
    private Line animatingLine;

    public ChartViewrectAnimator(ILineChart chart) {
        this.chart = chart;
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.addListener(this);
        animator.addUpdateListener(this);
        animator.setDuration(FAST_ANIMATION_DURATION);
    }

    public void startAnimation(Viewrect startViewrect, Viewrect targetViewrect) {
        startAnimationWithToggleLine(startViewrect, targetViewrect, null);
    }

    public void startAnimationWithToggleLine(Viewrect startViewrect, Viewrect targetViewrect, Line line) {
        this.startViewrect.set(startViewrect);
        this.targetViewrect.set(targetViewrect);
        animatingLine = line;
        animator.setDuration(FAST_ANIMATION_DURATION);
        animator.start();
    }

    public void updateAnimation(Viewrect startViewrect, Viewrect targetViewrect) {
        //this.startViewrect.set(startViewrect);
        //this.targetViewrect.set(targetViewrect.left, this.targetViewrect.top, targetViewrect.right, this.targetViewrect.bottom);
    }

    public void cancelAnimation() {
        animator.cancel();
        animatingLine = null;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float scale = animation.getAnimatedFraction();
        float dLeft = targetViewrect.left - startViewrect.left;
        float dRight = targetViewrect.right - startViewrect.right;
        float dTop = targetViewrect.top - startViewrect.top;
        float dBot = targetViewrect.bottom - startViewrect.bottom;
        float diffLeft = dLeft != 0f ? dLeft * scale : 1f;
        float diffTop = dTop * scale;
        float diffRight = dRight != 0f ? dRight * scale : 1f;
        float diffBottom = dBot * scale;

        newViewrect.set(startViewrect.left + diffLeft, startViewrect.top + diffTop, startViewrect.right + diffRight, startViewrect.bottom + diffBottom);
        if (animatingLine != null) {
            float alpha = 0f;
            if (animatingLine.isActive()) {
                alpha = scale;
            } else {
                alpha = 1f - scale;
            }

            animatingLine.setAlpha(alpha);
        }
        chart.setCurrentViewrect(newViewrect);
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        chart.setCurrentViewrect(targetViewrect);
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
