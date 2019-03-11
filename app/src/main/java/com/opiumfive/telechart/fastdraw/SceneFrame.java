package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class SceneFrame {

    ArrayList<Shape> shapes = new ArrayList<>();

    void drawOn(Canvas canvas, Paint paint) {
        for (Shape shape : shapes)
            shape.draw(canvas, paint);
    }

    void clear() {
        shapes.clear();
    }
}
