package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.render.AxesRenderer;
import com.opiumfive.telechart.chart.render.LineChartRenderer;


public class ChartCanvasDrawer {

    private ChartViewportHandler chartViewportHandler;
    private AxesRenderer axesRenderer;
    private LineChartRenderer chartRenderer;

    private final Paint paint = new Paint() {{
        setStyle(Paint.Style.FILL);
        setColor(Color.WHITE);
    }};

    public ChartCanvasDrawer(ChartViewportHandler chartViewportHandler, AxesRenderer axesRenderer, LineChartRenderer chartRenderer) {
        this.chartViewportHandler = chartViewportHandler;
        this.axesRenderer = axesRenderer;
        this.chartRenderer = chartRenderer;
    }

    public synchronized void drawOn(Canvas canvas) {
        canvas.drawPaint(paint);
        axesRenderer.drawInForeground(canvas);
        int clipRestoreCount = canvas.save();
        canvas.clipRect(chartViewportHandler.getContentRectMinusAllMargins());
        chartRenderer.draw(canvas);
        canvas.restoreToCount(clipRestoreCount);
        chartRenderer.drawUnclipped(canvas);
        axesRenderer.drawInForeground(canvas);
    }
}
