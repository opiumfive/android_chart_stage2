package com.opiumfive.telechart.chart.render;

import com.opiumfive.telechart.chart.model.PointValue;

import java.util.ArrayList;
import java.util.List;

public class LinePathOptimizer {

    private ChartViewrectHandler chartViewrectHandler;

    public LinePathOptimizer(ChartViewrectHandler chartViewrectHandler) {
        this.chartViewrectHandler = chartViewrectHandler;
    }

    // one point is (approx) on line between near points, no sense to draw 2+ lines instead of one
    // especially on a preview chart, where maxAngleVariation can be more
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

        return optimizedList;
    }

    private float angleBetween(PointValue center, PointValue current, PointValue previous) {
        return Math.abs(
            180f - (float) Math.toDegrees(Math.atan2(
                chartViewrectHandler.computeRawX(current.getX()) - chartViewrectHandler.computeRawX(center.getX()),
                chartViewrectHandler.computeRawY(current.getY()) - chartViewrectHandler.computeRawY(center.getY()))
                    - Math.atan2(chartViewrectHandler.computeRawX(previous.getX()) - chartViewrectHandler.computeRawX(center.getX()),
                chartViewrectHandler.computeRawY(previous.getY()) - chartViewrectHandler.computeRawY(center.getY())
            ))
        );
    }
}
