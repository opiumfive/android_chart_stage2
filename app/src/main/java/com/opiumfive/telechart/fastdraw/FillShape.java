package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.graphics.Paint;

class FillShape extends Shape {

    FillShape(int color) {
        this.color = color;
    }

    @Override
    void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        canvas.drawPaint(paint);
    }
}
