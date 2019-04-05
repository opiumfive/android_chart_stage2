package com.opiumfive.telechart.chart.draw;

import android.util.Log;

import com.opiumfive.telechart.chart.model.PointValue;

import java.util.ArrayList;
import java.util.List;

public class LinePathOptimizer {

    float xFactor = 1f;
    float yFactor = 1f;
    float minX = 0f;
    float minY = 0f;

    public LinePathOptimizer() {
    }

    public List<PointValue> optimizeLine(List<PointValue> values, float maxAngleVariation) {
        if (values.size() > 150) {

            float minX = values.get(0).getX();
            float maxX = values.get(values.size() - 1).getX();
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;

            for (PointValue pointValue : values) {
                if (pointValue.getY() > maxY) maxY = pointValue.getY();
                if (pointValue.getY() < minY) minY = pointValue.getY();
            }

            xFactor = maxX - minX;
            yFactor = maxY - minY;

            List<PointValue> optimizedList = new ArrayList<>();
            optimizedList.add(values.get(0));
            int xPos = 1;
                while (xPos < values.size() - 1) {
                    float angle = angleBetween(values.get(xPos), values.get(xPos + 1), optimizedList.get(optimizedList.size() - 1));
                    if (angle > maxAngleVariation) {
                        optimizedList.add(values.get(xPos));
                    }

                    xPos++;
                }

                if (values.size() > 1) {
                    optimizedList.add(values.get(values.size() - 1));
            }

            int pointsOverall = values.size();
            int pointsOptimized = optimizedList.size();

            Log.d("drawing_optimize", "points = " + pointsOverall + "; optimized = " + pointsOptimized);
            return optimizedList;
        } else {
            return values;
        }
    }

    private float normalizeX(float x) {
        return (x - minX) / xFactor;
    }

    private float normalizeY(float y) {
        return (y - minY) / yFactor;
    }

    private float angleBetween(PointValue center, PointValue current, PointValue previous) {
        return Math.abs(180f - (float) Math.toDegrees(
                Math.atan2(normalizeX(current.getX()) - normalizeX(center.getX()), normalizeY(current.getY()) - normalizeY(center.getY()))
                - Math.atan2(normalizeX(previous.getX()) - normalizeX(center.getX()), normalizeY(previous.getY()) - normalizeY(center.getY()))));
    }
}
