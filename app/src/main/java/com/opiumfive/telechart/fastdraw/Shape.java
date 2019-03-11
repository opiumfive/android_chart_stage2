package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Shape {
    
    protected int color;

    abstract void draw(Canvas canvas, Paint paint);
}