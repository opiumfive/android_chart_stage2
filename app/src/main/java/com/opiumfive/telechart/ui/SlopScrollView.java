package com.opiumfive.telechart.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class SlopScrollView extends ScrollView {

    private float previousX = 0;
    private float previousY = 0;
    private boolean blockScrolling = false;
    private boolean doScrolling = false;

    public SlopScrollView(Context context) {
        this(context, null, 0);
    }

    public SlopScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlopScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                doScrolling = false;
                blockScrolling = false;
                super.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - previousX);
                float dy = Math.abs(y - previousY);

                if (dx > 150 && !doScrolling) {
                    blockScrolling = true;
                }

                if (dy > 150 && !blockScrolling) {
                    doScrolling = true;
                }

                if (!blockScrolling) {
                    super.onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!blockScrolling) {
                    super.onTouchEvent(ev);
                }
                doScrolling = false;
                blockScrolling = false;
                return false;
            default:
                break;
        }
        previousX = x;
        previousY = y;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}
