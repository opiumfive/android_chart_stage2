package com.opiumfive.telechart.chart.draw;

import android.graphics.Point;
import android.graphics.Rect;

import com.opiumfive.telechart.chart.animator.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.Viewrect;

public class ChartViewrectHandler {

    protected static final float MAXIMUM_ZOOM = 20f;
    private static final float KALMAN_FILTER_FACTOR = 0.1f;
    private static final float KALMAN_FILTER_NOISE_FACTOR = 25f;

    protected int chartWidth;
    protected int chartHeight;

    protected Rect contentRectMinusAllMargins = new Rect();
    protected Rect contentRectMinusAxesMargins = new Rect();
    protected Rect maxContentRect = new Rect();

    protected Viewrect currentViewrect = new Viewrect();
    protected Viewrect maxViewrect = new Viewrect();
    protected float minViewportWidth;
    protected float minViewportHeight;

    protected ViewrectChangeListener viewrectChangeListener;

    private float yMaxFilterBuff = 1f;
    private float yMinFilterBuff = 1f;
    private float filterFactor = KALMAN_FILTER_FACTOR;
    private float filterNoise = KALMAN_FILTER_NOISE_FACTOR;

    public ChartViewrectHandler() {
    }

    public void setContentRect(int width, int height, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        chartWidth = width;
        chartHeight = height;
        maxContentRect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom);
        contentRectMinusAxesMargins.set(maxContentRect);
        contentRectMinusAllMargins.set(maxContentRect);
    }

    public void resetContentRect() {
        contentRectMinusAxesMargins.set(maxContentRect);
        contentRectMinusAllMargins.set(maxContentRect);
    }

    public void insetContentRect(int deltaLeft, int deltaTop, int deltaRight, int deltaBottom) {
        contentRectMinusAxesMargins.left = contentRectMinusAxesMargins.left + deltaLeft;
        contentRectMinusAxesMargins.top = contentRectMinusAxesMargins.top + deltaTop;
        contentRectMinusAxesMargins.right = contentRectMinusAxesMargins.right - deltaRight;
        contentRectMinusAxesMargins.bottom = contentRectMinusAxesMargins.bottom - deltaBottom;
    }

    public void limitViewport(float left, float top, float right, float bottom, float distanceX) {
        if (right - left < minViewportWidth) {
            right = left + minViewportWidth;
            if (left < maxViewrect.left) {
                left = maxViewrect.left;
                right = left + minViewportWidth;
            } else if (right > maxViewrect.right) {
                right = maxViewrect.right;
                left = right - minViewportWidth;
            }
        }

        if (top - bottom < minViewportHeight) {
            bottom = top - minViewportHeight;
            if (top > maxViewrect.top) {
                top = maxViewrect.top;
                bottom = top - minViewportHeight;
            } else if (bottom < maxViewrect.bottom) {
                bottom = maxViewrect.bottom;
                top = bottom + minViewportHeight;
            }
        }

        currentViewrect.left = left;
        currentViewrect.top = top;
        currentViewrect.right = right;
        currentViewrect.bottom = bottom;

        if (viewrectChangeListener != null) {
            viewrectChangeListener.onViewportChanged(currentViewrect, distanceX);
        }
    }

    public void setViewportTopLeft(float left, float top, float distanceX) {
        final float curWidth = currentViewrect.width();
        final float curHeight = currentViewrect.height();

        left = Math.max(maxViewrect.left, Math.min(left, maxViewrect.right - curWidth));
        top = Math.max(maxViewrect.bottom + curHeight, Math.min(top, maxViewrect.top));
        limitViewport(left, top, left + curWidth, top - curHeight, distanceX);
    }

    public float computeRawX(float valueX) {
        final float pixelOffset = (valueX - currentViewrect.left) * (contentRectMinusAllMargins.width() / currentViewrect.width());
        return contentRectMinusAllMargins.left + pixelOffset;
    }

    public float computeSideScrollTrigger(float factor) {
        return contentRectMinusAllMargins.width() * factor;
    }

    public float computeRawY(float valueY) {
        final float pixelOffset = (valueY - currentViewrect.bottom) * (contentRectMinusAllMargins.height() / currentViewrect.height());
        return contentRectMinusAllMargins.bottom - pixelOffset;
    }

    public void computeScrollSurfaceSize(Point out) {
        out.set((int) (maxViewrect.width() * contentRectMinusAllMargins.width() / currentViewrect.width()),
                (int) (maxViewrect.height() * contentRectMinusAllMargins.height() / currentViewrect.height()));
    }

    public Rect getContentRectMinusAllMargins() {
        return contentRectMinusAllMargins;
    }

    public Rect getContentRectMinusAxesMargins() {
        return contentRectMinusAxesMargins;
    }

    public Viewrect getCurrentViewrect() {
        return currentViewrect;
    }

    public void setCurrentViewrect(Viewrect viewrect) {
        limitViewport(viewrect.left, viewrect.top, viewrect.right, viewrect.bottom, 0f);
    }

    public void setCurrentViewport(float left, float top, float right, float bottom, float distanceX) {
        limitViewport(left, top, right, bottom, distanceX);
    }

    public Viewrect getMaximumViewport() {
        return maxViewrect;
    }

    public void setMaxViewrect(Viewrect maxViewrect) {
        setMaxViewport(maxViewrect.left, maxViewrect.top, maxViewrect.right, maxViewrect.bottom);
    }

    // Kalman filter for smooth min-max morphling
    public void filterSmooth(Viewrect current, Viewrect targetAdjustedViewrect, float distanceX) {

        if (distanceX != 0f) {
            filterFactor = KALMAN_FILTER_FACTOR * Math.abs(distanceX);
        } else {
            filterFactor = KALMAN_FILTER_FACTOR;
        }

        float targetYMax = targetAdjustedViewrect.top;
        float targetYMin = targetAdjustedViewrect.bottom;
        float currentYMax = current.top;
        float currentYMin = current.bottom;

        float yMaxFilterPrediction = yMaxFilterBuff + filterFactor;
        float factor = yMaxFilterPrediction / (yMaxFilterPrediction + filterNoise);
        targetYMax = currentYMax + factor * (targetYMax - currentYMax);
        yMaxFilterBuff = (1f - factor) * yMaxFilterPrediction;

        float yMinFilterPrediction = yMinFilterBuff + filterFactor;
        factor = yMinFilterPrediction / (yMinFilterPrediction + filterNoise);
        targetYMin = currentYMin + factor * (targetYMin - currentYMin);
        yMinFilterBuff = (1f - factor) * yMinFilterPrediction;

        targetAdjustedViewrect.top = targetYMax;
        targetAdjustedViewrect.bottom = targetYMin;
    }

    public void setMaxViewport(float left, float top, float right, float bottom) {
        this.maxViewrect.set(left, top, right, bottom);
        computeMinimumWidthAndHeight();
    }

    public Viewrect getVisibleViewport() {
        return currentViewrect;
    }

    public void setViewrectChangeListener(ViewrectChangeListener viewrectChangeListener) {
        this.viewrectChangeListener = viewrectChangeListener;
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    private void computeMinimumWidthAndHeight() {
        minViewportWidth = this.maxViewrect.width() / MAXIMUM_ZOOM;
        minViewportHeight = this.maxViewrect.height() / MAXIMUM_ZOOM;
    }
}