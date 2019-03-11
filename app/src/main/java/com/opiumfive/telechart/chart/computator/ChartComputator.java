package com.opiumfive.telechart.chart.computator;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import com.opiumfive.telechart.chart.listener.DummyVieportChangeListener;
import com.opiumfive.telechart.chart.listener.ViewportChangeListener;
import com.opiumfive.telechart.chart.model.Viewport;


public class ChartComputator {


    protected static final float DEFAULT_MAXIMUM_ZOOM = 20f;
    protected float maxZoom = DEFAULT_MAXIMUM_ZOOM;
    protected int chartWidth;
    protected int chartHeight;

    protected Rect contentRectMinusAllMargins = new Rect();
    protected Rect contentRectMinusAxesMargins = new Rect();
    protected Rect maxContentRect = new Rect();

    protected Viewport currentViewport = new Viewport();
    protected Viewport maxViewport = new Viewport();
    protected float minViewportWidth;
    protected float minViewportHeight;

    protected ViewportChangeListener viewportChangeListener = new DummyVieportChangeListener();


    public void setContentRect(int width, int height, int paddingLeft, int paddingTop, int paddingRight,
                               int paddingBottom) {
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

        insetContentRectByInternalMargins(deltaLeft, deltaTop, deltaRight, deltaBottom);
    }

    public void insetContentRectByInternalMargins(int deltaLeft, int deltaTop, int deltaRight, int deltaBottom) {
        contentRectMinusAllMargins.left = contentRectMinusAllMargins.left + deltaLeft;
        contentRectMinusAllMargins.top = contentRectMinusAllMargins.top + deltaTop;
        contentRectMinusAllMargins.right = contentRectMinusAllMargins.right - deltaRight;
        contentRectMinusAllMargins.bottom = contentRectMinusAllMargins.bottom - deltaBottom;
    }


    public void constrainViewport(float left, float top, float right, float bottom) {

        if (right - left < minViewportWidth) {
            right = left + minViewportWidth;
            if (left < maxViewport.left) {
                left = maxViewport.left;
                right = left + minViewportWidth;
            } else if (right > maxViewport.right) {
                right = maxViewport.right;
                left = right - minViewportWidth;
            }
        }

        if (top - bottom < minViewportHeight) {
            bottom = top - minViewportHeight;
            if (top > maxViewport.top) {
                top = maxViewport.top;
                bottom = top - minViewportHeight;
            } else if (bottom < maxViewport.bottom) {
                bottom = maxViewport.bottom;
                top = bottom + minViewportHeight;
            }
        }

        currentViewport.left = Math.max(maxViewport.left, left);
        currentViewport.top = Math.min(maxViewport.top, top);
        currentViewport.right = Math.min(maxViewport.right, right);
        currentViewport.bottom = Math.max(maxViewport.bottom, bottom);

        viewportChangeListener.onViewportChanged(currentViewport);
    }

    public void setViewportTopLeft(float left, float top) {
        final float curWidth = currentViewport.width();
        final float curHeight = currentViewport.height();

        left = Math.max(maxViewport.left, Math.min(left, maxViewport.right - curWidth));
        top = Math.max(maxViewport.bottom + curHeight, Math.min(top, maxViewport.top));
        constrainViewport(left, top, left + curWidth, top - curHeight);
    }

    public float computeRawX(float valueX) {
        final float pixelOffset = (valueX - currentViewport.left) * (contentRectMinusAllMargins.width() / currentViewport.width());
        return contentRectMinusAllMargins.left + pixelOffset;
    }

    public float computeRawY(float valueY) {
        final float pixelOffset = (valueY - currentViewport.bottom) * (contentRectMinusAllMargins.height() / currentViewport.height());
        return contentRectMinusAllMargins.bottom - pixelOffset;
    }

    public float computeRawDistanceX(float distance) {
        return distance * (contentRectMinusAllMargins.width() / currentViewport.width());
    }

    public float computeRawDistanceY(float distance) {
        return distance * (contentRectMinusAllMargins.height() / currentViewport.height());
    }

    public boolean rawPixelsToDataPoint(float x, float y, PointF dest) {
        if (!contentRectMinusAllMargins.contains((int) x, (int) y)) {
            return false;
        }
        dest.set(currentViewport.left + (x - contentRectMinusAllMargins.left) * currentViewport.width() / contentRectMinusAllMargins.width(),
                currentViewport.bottom + (y - contentRectMinusAllMargins.bottom) * currentViewport.height() / -contentRectMinusAllMargins.height());
        return true;
    }

    public void computeScrollSurfaceSize(Point out) {
        out.set((int) (maxViewport.width() * contentRectMinusAllMargins.width() / currentViewport.width()),
                (int) (maxViewport.height() * contentRectMinusAllMargins.height() / currentViewport.height()));
    }

    public boolean isWithinContentRect(float x, float y, float precision) {
        if (x >= contentRectMinusAllMargins.left - precision && x <= contentRectMinusAllMargins.right + precision) {
            return y <= contentRectMinusAllMargins.bottom + precision && y >= contentRectMinusAllMargins.top - precision;
        }
        return false;
    }

    public Rect getContentRectMinusAllMargins() {
        return contentRectMinusAllMargins;
    }

    public Rect getContentRectMinusAxesMargins() {
        return contentRectMinusAxesMargins;
    }

    public Viewport getCurrentViewport() {
        return currentViewport;
    }

    public void setCurrentViewport(Viewport viewport) {
        constrainViewport(viewport.left, viewport.top, viewport.right, viewport.bottom);
    }

    public void setCurrentViewport(float left, float top, float right, float bottom) {
        constrainViewport(left, top, right, bottom);
    }

    public Viewport getMaximumViewport() {
        return maxViewport;
    }

    public void setMaxViewport(Viewport maxViewport) {
        setMaxViewport(maxViewport.left, maxViewport.top, maxViewport.right, maxViewport.bottom);
    }

    public void setMaxViewport(float left, float top, float right, float bottom) {
        this.maxViewport.set(left, top, right, bottom);
        computeMinimumWidthAndHeight();
    }

    public Viewport getVisibleViewport() {
        return currentViewport;
    }

    public void setVisibleViewport(Viewport visibleViewport) {
        setCurrentViewport(visibleViewport);
    }

    public float getMinimumViewportWidth() {
        return minViewportWidth;
    }

    public float getMinimumViewportHeight() {
        return minViewportHeight;
    }

    public void setViewportChangeListener(ViewportChangeListener viewportChangeListener) {
        if (null == viewportChangeListener) {
            this.viewportChangeListener = new DummyVieportChangeListener();
        } else {
            this.viewportChangeListener = viewportChangeListener;
        }
    }

    public int getChartWidth() {
        return chartWidth;
    }

    public int getChartHeight() {
        return chartHeight;
    }

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        if (maxZoom < 1) {
            maxZoom = 1;
        }

        this.maxZoom = maxZoom;

        computeMinimumWidthAndHeight();

        setCurrentViewport(currentViewport);

    }

    private void computeMinimumWidthAndHeight() {
        minViewportWidth = this.maxViewport.width() / maxZoom;
        minViewportHeight = this.maxViewport.height() / maxZoom;
    }

}