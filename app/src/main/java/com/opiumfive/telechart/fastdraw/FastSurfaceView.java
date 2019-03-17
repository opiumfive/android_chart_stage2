package com.opiumfive.telechart.fastdraw;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.ChartDataProvider;
import com.opiumfive.telechart.chart.listener.ChartAnimationListener;
import com.opiumfive.telechart.chart.listener.ChartViewportAnimator;
import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartTouchHandler;
import com.opiumfive.telechart.chart.listener.ViewportChangeListener;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.SelectedValues;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.render.AxesRenderer;
import com.opiumfive.telechart.chart.render.LineChartRenderer;

public class FastSurfaceView extends SurfaceView implements ILineChart, SurfaceHolder.Callback, ChartDataProvider {

    protected LineChartData data;



    protected ChartViewportHandler chartViewportHandler;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected LineChartRenderer chartRenderer;
    protected ChartViewportAnimator viewportAnimator;
    protected boolean isInteractive = true;
    protected boolean isContainerScrollEnabled = false;


    private ChartCanvasDrawer chartCanvasDrawer;

    private DrawingThread drawingThread;

    public FastSurfaceView(Context context) {
        this(context, null, 0);
    }

    public FastSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);

        chartViewportHandler = new ChartViewportHandler();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);
        this.viewportAnimator = new ChartViewportAnimator(this);

        setChartRenderer(new LineChartRenderer(context, this, this));
        setChartData(LineChartData.generateDummyData());

        chartCanvasDrawer = new ChartCanvasDrawer(chartViewportHandler, axesRenderer, chartRenderer);
    }

    public void updateCanvasDrawer() {
        chartCanvasDrawer = new ChartCanvasDrawer(chartViewportHandler, axesRenderer, chartRenderer);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do something
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawingThread = new DrawingThread(new SurfaceViewHolder(holder), chartCanvasDrawer);
        drawingThread.setRunning(true);
        drawingThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        drawingThread.setRunning(false);

        while (retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setChartData(LineChartData data) {

        if (null == data) {
            this.data = LineChartData.generateDummyData();
        } else {
            this.data = data;
        }

        onChartDataChange();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        chartViewportHandler.setContentRect(getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        chartRenderer.onChartSizeChanged();
        axesRenderer.onChartSizeChanged();
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (isInteractive) {

            boolean needInvalidate;

            if (isContainerScrollEnabled) {
                needInvalidate = touchHandler.handleTouchEvent(event, getParent());
            } else {
                needInvalidate = touchHandler.handleTouchEvent(event);
            }

            if (needInvalidate) {
                ViewCompat.postInvalidateOnAnimation(this);
            }

            return true;
        } else {

            return false;
        }
    }

    public void computeScroll() {
        super.computeScroll();
        if (isInteractive) {
            if (touchHandler.computeScroll()) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    public void animationDataUpdate(float scale) {
        getChartData().update(scale);
        chartRenderer.onChartViewportChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void animationDataFinished() {
        getChartData().finish();
        chartRenderer.onChartViewportChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setViewportAnimationListener(ChartAnimationListener animationListener) {
        viewportAnimator.setChartAnimationListener(animationListener);
    }

    public void setViewportChangeListener(ViewportChangeListener viewportChangeListener) {
        chartViewportHandler.setViewportChangeListener(viewportChangeListener);
    }

    public LineChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(LineChartRenderer renderer) {
        chartRenderer = renderer;
        resetRendererAndTouchHandler();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public AxesRenderer getAxesRenderer() {
        return axesRenderer;
    }

    public ChartViewportHandler getChartViewportHandler() {
        return chartViewportHandler;
    }

    public ChartTouchHandler getTouchHandler() {
        return touchHandler;
    }

    public boolean isInteractive() {
        return isInteractive;
    }

    public void setInteractive(boolean isInteractive) {
        this.isInteractive = isInteractive;
    }

    public boolean isZoomEnabled() {
        return touchHandler.isZoomEnabled();
    }

    public void setZoomEnabled(boolean isZoomEnabled) {
        touchHandler.setZoomEnabled(isZoomEnabled);
    }

    public boolean isScrollEnabled() {
        return touchHandler.isScrollEnabled();
    }

    public void setScrollEnabled(boolean isScrollEnabled) {
        touchHandler.setScrollEnabled(isScrollEnabled);
    }

    public void moveTo(float x, float y) {
        Viewrect scrollViewrect = computeScrollViewport(x, y);
        setCurrentViewport(scrollViewrect);
    }

    public void moveToWithAnimation(float x, float y) {
        Viewrect scrollViewrect = computeScrollViewport(x, y);
        setCurrentViewportWithAnimation(scrollViewrect);
    }

    private Viewrect computeScrollViewport(float x, float y) {
        Viewrect maxViewrect = getMaximumViewport();
        Viewrect currentViewrect = getCurrentViewport();
        Viewrect scrollViewrect = new Viewrect(currentViewrect);

        if (maxViewrect.contains(x, y)) {
            final float width = currentViewrect.width();
            final float height = currentViewrect.height();

            final float halfWidth = width / 2;
            final float halfHeight = height / 2;

            float left = x - halfWidth;
            float top = y + halfHeight;

            left = Math.max(maxViewrect.left, Math.min(left, maxViewrect.right - width));
            top = Math.max(maxViewrect.bottom + height, Math.min(top, maxViewrect.top));

            scrollViewrect.set(left, top, left + width, top - height);
        }

        return scrollViewrect;
    }

    public boolean isValueTouchEnabled() {
        return touchHandler.isValueTouchEnabled();
    }

    public void setValueTouchEnabled(boolean isValueTouchEnabled) {
        touchHandler.setValueTouchEnabled(isValueTouchEnabled);
    }

    public float getMaxZoom() {
        return chartViewportHandler.getMaxZoom();
    }

    public void setMaxZoom(float maxZoom) {
        chartViewportHandler.setMaxZoom(maxZoom);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public float getZoomLevel() {
        Viewrect maxViewrect = getMaximumViewport();
        Viewrect currentViewrect = getCurrentViewport();

        return Math.max(maxViewrect.width() / currentViewrect.width(), maxViewrect.height() / currentViewrect.height());

    }

    public void setZoomLevel(float x, float y, float zoomLevel) {
        Viewrect zoomViewrect = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewport(zoomViewrect);
    }

    public void setZoomLevelWithAnimation(float x, float y, float zoomLevel) {
        Viewrect zoomViewrect = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewportWithAnimation(zoomViewrect);
    }

    private Viewrect computeZoomViewport(float x, float y, float zoomLevel) {
        final Viewrect maxViewrect = getMaximumViewport();
        Viewrect zoomViewrect = new Viewrect(getMaximumViewport());

        if (maxViewrect.contains(x, y)) {

            if (zoomLevel < 1) {
                zoomLevel = 1;
            } else if (zoomLevel > getMaxZoom()) {
                zoomLevel = getMaxZoom();
            }

            final float newWidth = zoomViewrect.width() / zoomLevel;
            final float halfWidth = newWidth / 2;

            float left = x - halfWidth;
            float right = x + halfWidth;

            if (left < maxViewrect.left) {
                left = maxViewrect.left;
                right = left + newWidth;
            } else if (right > maxViewrect.right) {
                right = maxViewrect.right;
                left = right - newWidth;
            }

            zoomViewrect.left = left;
            zoomViewrect.right = right;
        }
        return zoomViewrect;
    }

    public Viewrect getMaximumViewport() {
        return chartRenderer.getMaximumViewport();
    }

    public void setMaximumViewport(Viewrect maxViewrect) {
        chartRenderer.setMaximumViewport(maxViewrect);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewportWithAnimation(Viewrect targetViewrect) {
        if (null != targetViewrect) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewrect);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewportWithAnimation(Viewrect targetViewrect, long duration) {
        if (null != targetViewrect) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewrect, duration);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public Viewrect getCurrentViewport() {
        return getChartRenderer().getCurrentViewport();
    }

    public void setCurrentViewport(Viewrect targetViewrect) {
        if (null != targetViewrect) {
            chartRenderer.setCurrentViewport(targetViewrect);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void resetViewports() {
        chartRenderer.setMaximumViewport(null);
        chartRenderer.setCurrentViewport(null);
    }

    public boolean isViewportCalculationEnabled() {
        return chartRenderer.isViewportCalculationEnabled();
    }

    public void setViewportCalculationEnabled(boolean isEnabled) {
        chartRenderer.setViewportCalculationEnabled(isEnabled);
    }

    public boolean isValueSelectionEnabled() {
        return touchHandler.isValueSelectionEnabled();
    }

    public void setValueSelectionEnabled(boolean isValueSelectionEnabled) {
        touchHandler.setValueSelectionEnabled(isValueSelectionEnabled);
    }

    public void selectValue(SelectedValues selectedValues) {
        chartRenderer.selectValue(selectedValues);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public SelectedValues getSelectedValue() {
        return chartRenderer.getSelectedValues();
    }

    public boolean isContainerScrollEnabled() {
        return isContainerScrollEnabled;
    }

    public void setContainerScrollEnabled(boolean isContainerScrollEnabled) {
        this.isContainerScrollEnabled = isContainerScrollEnabled;
    }

    protected void onChartDataChange() {
        chartViewportHandler.resetContentRect();
        chartRenderer.onChartDataChanged();
        axesRenderer.onChartDataChanged();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    protected void resetRendererAndTouchHandler() {
        this.chartRenderer.resetRenderer();
        this.axesRenderer.resetRenderer();
        this.touchHandler.resetTouchHandler();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (getZoomLevel() <= 1.0) {
            return false;
        }
        final Viewrect currentViewrect = getCurrentViewport();
        final Viewrect maximumViewrect = getMaximumViewport();
        if (direction < 0) {
            return currentViewrect.left > maximumViewrect.left;
        } else {
            return currentViewrect.right < maximumViewrect.right;
        }
    }

    public LineChartData getChartData() {
        return data;
    }
}
