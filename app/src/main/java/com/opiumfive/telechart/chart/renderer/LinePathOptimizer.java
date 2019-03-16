package com.opiumfive.telechart.chart.renderer;

import com.opiumfive.telechart.chart.model.PointValue;

import java.util.ArrayList;
import java.util.List;

public class LinePathOptimizer {

    private ChartViewportHandler chartViewportHandler;

    public LinePathOptimizer(ChartViewportHandler chartViewportHandler) {
        this.chartViewportHandler = chartViewportHandler;
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

        return optimizedList;
    }

    private float angleBetween(PointValue center, PointValue current, PointValue previous) {
        return Math.abs(180f - (float) Math.toDegrees(Math.atan2(
                chartViewportHandler.computeRawX(current.getX()) - chartViewportHandler.computeRawX(center.getX()),
                chartViewportHandler.computeRawY(current.getY()) - chartViewportHandler.computeRawY(center.getY()))
                    - Math.atan2(chartViewportHandler.computeRawX(previous.getX()) - chartViewportHandler.computeRawX(center.getX()),
                chartViewportHandler.computeRawY(previous.getY()) - chartViewportHandler.computeRawY(center.getY()))));
    }
}
