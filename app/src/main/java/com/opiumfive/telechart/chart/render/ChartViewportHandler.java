package com.opiumfive.telechart.chart.render;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import com.opiumfive.telechart.chart.listener.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.Viewrect;

import java.util.List;


public class ChartViewportHandler {


    protected static final float DEFAULT_MAXIMUM_ZOOM = 20f;
    protected float maxZoom = DEFAULT_MAXIMUM_ZOOM;

    protected int chartWidth;
    protected int chartHeight;

    protected Rect contentRectMinusAllMargins = new Rect();
    protected Rect contentRectMinusAxesMargins = new Rect();
    protected Rect maxContentRect = new Rect();

    protected Viewrect currentViewrect = new Viewrect();
    protected Viewrect maxViewrect = new Viewrect();
    protected float minViewportWidth;
    protected float minViewportHeight;
    protected LinePathOptimizer linePathOptimizer;

    protected ViewrectChangeListener viewrectChangeListener;

    public ChartViewportHandler() {
        linePathOptimizer = new LinePathOptimizer(this);
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

        currentViewrect.left = Math.max(maxViewrect.left, left);
        currentViewrect.top = Math.min(maxViewrect.top, top);
        currentViewrect.right = Math.min(maxViewrect.right, right);
        currentViewrect.bottom = Math.max(maxViewrect.bottom, bottom);

        if (viewrectChangeListener != null) {
            viewrectChangeListener.onViewportChanged(currentViewrect);
        }
    }

    public void setViewportTopLeft(float left, float top) {
        final float curWidth = currentViewrect.width();
        final float curHeight = currentViewrect.height();

        left = Math.max(maxViewrect.left, Math.min(left, maxViewrect.right - curWidth));
        top = Math.max(maxViewrect.bottom + curHeight, Math.min(top, maxViewrect.top));
        constrainViewport(left, top, left + curWidth, top - curHeight);
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

    public boolean rawPixelsToDataPoint(float x, float y, PointF dest) {
        if (!contentRectMinusAllMargins.contains((int) x, (int) y)) {
            return false;
        }
        dest.set(currentViewrect.left + (x - contentRectMinusAllMargins.left) * currentViewrect.width() / contentRectMinusAllMargins.width(),
                currentViewrect.bottom + (y - contentRectMinusAllMargins.bottom) * currentViewrect.height() / -contentRectMinusAllMargins.height());
        return true;
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
        constrainViewport(viewrect.left, viewrect.top, viewrect.right, viewrect.bottom);
    }

    public void setCurrentViewport(float left, float top, float right, float bottom) {
        constrainViewport(left, top, right, bottom);
    }

    public Viewrect getMaximumViewport() {
        return maxViewrect;
    }

    public void setMaxViewrect(Viewrect maxViewrect) {
        setMaxViewport(maxViewrect.left, maxViewrect.top, maxViewrect.right, maxViewrect.bottom);
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

    public float getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(float maxZoom) {
        if (maxZoom < 1) {
            maxZoom = 1;
        }

        this.maxZoom = maxZoom;
        computeMinimumWidthAndHeight();
        setCurrentViewrect(currentViewrect);
    }

    private void computeMinimumWidthAndHeight() {
        minViewportWidth = this.maxViewrect.width() / maxZoom;
        minViewportHeight = this.maxViewrect.height() / maxZoom;
    }

    public List<PointValue> optimizeLine(List<PointValue> values, float maxAngleVariation) {
        return linePathOptimizer.optimizeLine(values, maxAngleVariation);
    }
}