package com.opiumfive.telechart.chart.draw;

import android.util.Log;

import com.opiumfive.telechart.chart.model.PointValue;

import java.util.ArrayList;
import java.util.List;

public class LinePathOptimizer {

    private ChartViewrectHandler chartViewrectHandler;

    public LinePathOptimizer(ChartViewrectHandler chartViewrectHandler) {
        this.chartViewrectHandler = chartViewrectHandler;
    }

    public List<PointValue> optimizeLine(List<PointValue> values, float maxAngleVariation) {
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
    }

    private float angleBetween(PointValue center, PointValue current, PointValue previous) {
        return Math.abs(180f - (float) Math.toDegrees(Math.atan2(
                chartViewrectHandler.computeRawX(current.getX()) - chartViewrectHandler.computeRawX(center.getX()),
                chartViewrectHandler.computeRawY(current.getY()) - chartViewrectHandler.computeRawY(center.getY()))
                - Math.atan2(chartViewrectHandler.computeRawX(previous.getX()) - chartViewrectHandler.computeRawX(center.getX()),
                chartViewrectHandler.computeRawY(previous.getY()) - chartViewrectHandler.computeRawY(center.getY()))));
    }
}
