package com.opiumfive.telechart.fastdraw;

import android.graphics.Canvas;
import android.util.Log;

public class DrawingThread extends Thread {

    private ChartCanvasDrawer chartCanvasDrawer;

    final private ISurfaceHolder surfaceHolder;
    private boolean isRunning = false;
    private long previousTime;
    private final int fps = 70;

    public DrawingThread(ISurfaceHolder surfaceHolder, ChartCanvasDrawer chartCanvasDrawer) {
        this.surfaceHolder = surfaceHolder;
        this.chartCanvasDrawer = chartCanvasDrawer;

        setPriority(MIN_PRIORITY);

        previousTime = System.currentTimeMillis();
    }

    public void setRunning(boolean run) {
        isRunning = run;
    }

    @Override
    public void run() {
        Canvas canvas;

        while (isRunning) {

            long currentTimeMillis = System.currentTimeMillis();
            long elapsedTimeMs = currentTimeMillis - previousTime;
            long sleepTimeMs = (long) (1000f / fps - elapsedTimeMs);

            //Log.d("drawing_thread + " + this.getId(), "elapsedTimeMs = " + elapsedTimeMs + "; sleepTimeMs = " + sleepTimeMs);

            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();

                if (canvas == null) {
                    Thread.sleep(1);
                    continue;
                } else if (sleepTimeMs > 0 && elapsedTimeMs <= 16){
                    Thread.sleep(sleepTimeMs);
                }

                synchronized (surfaceHolder) {
                    chartCanvasDrawer.drawOn(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    long time = System.currentTimeMillis();
                    surfaceHolder.unlockCanvasAndPost(canvas);
                    Log.d("drawing_thread" + this.getId(), "elapsedTimeMs = " + (System.currentTimeMillis() - time));
                    previousTime = currentTimeMillis;
                }
            }
        }
    }
}
