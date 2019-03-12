package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.LineChartView;


public class PreviewChartTouchHandler extends ChartTouchHandler {

    public PreviewChartTouchHandler(Context context, ILineChart chart) {
        super(context, chart);
        gestureDetector = new GestureDetectorCompat(context, new PreviewChartGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ChartScaleGestureListener());

        isValueTouchEnabled = false;
        isValueSelectionEnabled = false;
    }

    protected class ChartScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (isZoomEnabled) {
                float scale = detector.getCurrentSpan() / detector.getPreviousSpan();
                if (Float.isInfinite(scale)) {
                    scale = 1;
                }
                return chartZoomer.scale(computator, detector.getFocusX(), detector.getFocusY(), scale);
            }

            return false;
        }
    }

    protected class PreviewChartGestureListener extends ChartGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, -distanceX, -distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, -velocityX, -velocityY);
        }
    }

}
