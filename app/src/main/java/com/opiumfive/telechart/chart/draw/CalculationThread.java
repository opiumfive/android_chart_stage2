package com.opiumfive.telechart.chart.draw;

import android.util.Log;

public class CalculationThread extends Thread {

    private boolean isRunning = false;
    private long previousTime;
    private LineChartRenderer lineChartRenderer;
    private AxesRenderer axesRenderer;
    private final int fps = 80;

    public CalculationThread(LineChartRenderer lineChartRenderer, AxesRenderer axesRenderer) {
        setPriority(MIN_PRIORITY);
        this.lineChartRenderer = lineChartRenderer;
        this.axesRenderer = axesRenderer;
        previousTime = System.currentTimeMillis();
    }

    public void setRunning(boolean run) {
        isRunning = run;
    }

    @Override
    public void run() {
        while (isRunning) {

            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTimeMs = currentTimeMillis - previousTime;
            long sleepTimeMs = (long) (1000f / fps - elapsedTimeMs);

            Log.d("theres", this.getId() + "run: " + currentTimeMillis);

            try {
                //lineChartRenderer.prepareDrawingData();

                Thread.sleep(5);
                //if (sleepTimeMs > 0) {
                //    Thread.sleep(sleepTimeMs);
                //}
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                previousTime = currentTimeMillis;
            }
        }
    }
}
