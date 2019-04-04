package com.opiumfive.telechart.chart.draw;

public class CalculationThread extends Thread {

    private boolean isRunning = false;
    private long previousTime;
    private final int fps = 70;

    public CalculationThread() {
        setPriority(MIN_PRIORITY);
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

            try {
                // calculate predraw
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                previousTime = currentTimeMillis;
            }
        }
    }
}
