package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.opiumfive.telechart.chart.computator.ChartComputator;
import com.opiumfive.telechart.chart.renderer.AxesRenderer;
import com.opiumfive.telechart.chart.renderer.LineChartRenderer;


public class SceneModelComposer {

    private ChartComputator chartComputator;
    private AxesRenderer axesRenderer;
    private LineChartRenderer chartRenderer;

    private final Paint paint = new Paint() {{
        setStyle(Paint.Style.FILL);
        setColor(Color.YELLOW);
    }};

    public SceneModelComposer(ChartComputator chartComputator, AxesRenderer axesRenderer, LineChartRenderer chartRenderer) {
        this.chartComputator = chartComputator;
        this.axesRenderer = axesRenderer;
        this.chartRenderer = chartRenderer;
    }

    public synchronized void drawOn(Canvas canvas) {
        synchronized (paint) {
            canvas.drawPaint(paint);

            axesRenderer.drawInForeground(canvas);
            int clipRestoreCount = canvas.save();
            canvas.clipRect(chartComputator.getContentRectMinusAllMargins());
            chartRenderer.draw(canvas);
            canvas.restoreToCount(clipRestoreCount);
            chartRenderer.drawUnclipped(canvas);
            axesRenderer.drawInForeground(canvas);
        }
    }
}
