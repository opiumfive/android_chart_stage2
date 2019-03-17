/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opiumfive.telechart.chart.touchControl;

import android.content.Context;
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
