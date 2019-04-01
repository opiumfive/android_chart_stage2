package com.opiumfive.telechart.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class SlopScrollView extends ScrollView {

    private int touchSlopX;
    private float previousX;

    public SlopScrollView(Context context) {
        this(context, null, 0);
    }

    public SlopScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlopScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlopX = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                MotionEvent me = MotionEvent.obtain(event);
                previousX = me.getX();
                me.recycle();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - previousX);

                if (xDiff > touchSlopX) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}
