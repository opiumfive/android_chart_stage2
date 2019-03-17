package com.opiumfive.telechart.chart.touchControl;

import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;


public class ZoomerCompat {

    private static final int DEFAULT_SHORT_ANIMATION_DURATION = 200;

    private Interpolator mInterpolator;
    private long mAnimationDurationMillis;
    private boolean mFinished = true;
    private float mCurrentZoom;
    private long mStartRTC;
    private float mEndZoom;

    public ZoomerCompat() {
        mInterpolator = new DecelerateInterpolator();
        mAnimationDurationMillis = DEFAULT_SHORT_ANIMATION_DURATION;
    }

    public void forceFinished(boolean finished) {
        mFinished = finished;
    }

    public void abortAnimation() {
        mFinished = true;
        mCurrentZoom = mEndZoom;
    }

    public void startZoom(float endZoom) {
        mStartRTC = SystemClock.elapsedRealtime();
        mEndZoom = endZoom;

        mFinished = false;
        mCurrentZoom = 1f;
    }

    public boolean computeZoom() {
        if (mFinished) {
            return false;
        }

        long tRTC = SystemClock.elapsedRealtime() - mStartRTC;
        if (tRTC >= mAnimationDurationMillis) {
            mFinished = true;
            mCurrentZoom = mEndZoom;
            return false;
        }

        float t = tRTC * 1f / mAnimationDurationMillis;
        mCurrentZoom = mEndZoom * mInterpolator.getInterpolation(t);
        return true;
    }

    public float getCurrZoom() {
        return mCurrentZoom;
    }
}
