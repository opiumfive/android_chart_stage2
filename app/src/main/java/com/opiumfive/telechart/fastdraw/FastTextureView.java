package com.opiumfive.telechart.fastdraw;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.LineChartDataProvider;
import com.opiumfive.telechart.chart.animation.ChartAnimationListener;
import com.opiumfive.telechart.chart.animation.ChartDataAnimator;
import com.opiumfive.telechart.chart.animation.ChartViewportAnimator;
import com.opiumfive.telechart.chart.renderer.ChartViewportHandler;
import com.opiumfive.telechart.chart.gesture.ChartTouchHandler;
import com.opiumfive.telechart.chart.listener.DummyLineChartOnValueSelectListener;
import com.opiumfive.telechart.chart.listener.LineChartOnValueSelectListener;
import com.opiumfive.telechart.chart.listener.ViewportChangeListener;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.renderer.AxesRenderer;
import com.opiumfive.telechart.chart.renderer.LineChartRenderer;


public class FastTextureView extends TextureView implements ILineChart, TextureView.SurfaceTextureListener, LineChartDataProvider {

    protected LineChartData data;
    protected LineChartOnValueSelectListener onValueTouchListener = new DummyLineChartOnValueSelectListener();


    protected ChartViewportHandler chartViewportHandler;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected LineChartRenderer chartRenderer;
    protected ChartDataAnimator dataAnimator;
    protected ChartViewportAnimator viewportAnimator;
    protected boolean isInteractive = true;
    protected boolean isContainerScrollEnabled = false;


    private ChartCanvasDrawer chartCanvasDrawer;

    private DrawingThread drawingThread;

    public FastTextureView(Context context) {
        this(context, null, 0);
    }

    public FastTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSurfaceTextureListener(this);
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        chartViewportHandler = new ChartViewportHandler();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);
        this.viewportAnimator = new ChartViewportAnimator(this);
        this.dataAnimator = new ChartDataAnimator(this);

        setChartRenderer(new LineChartRenderer(context, this, this));
        setLineChartData(LineChartData.generateDummyData());

        chartCanvasDrawer = new ChartCanvasDrawer(chartViewportHandler, axesRenderer, chartRenderer);
    }

    public void updateCanvasDrawer() {
        chartCanvasDrawer = new ChartCanvasDrawer(chartViewportHandler, axesRenderer, chartRenderer);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        drawingThread = new DrawingThread(new TextureViewHolder(this), chartCanvasDrawer);
        drawingThread.setRunning(true);
        drawingThread.start();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
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
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public LineChartData getLineChartData() {
        return data;
    }

    @Override
    public void setLineChartData(LineChartData data) {

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

    public void startDataAnimation() {
        dataAnimator.startAnimation(Long.MIN_VALUE);
    }

    public void startDataAnimation(long duration) {
        dataAnimator.startAnimation(duration);
    }

    public void cancelDataAnimation() {
        dataAnimator.cancelAnimation();
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

    public void setDataAnimationListener(ChartAnimationListener animationListener) {
        dataAnimator.setChartAnimationListener(animationListener);
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
        Viewport scrollViewport = computeScrollViewport(x, y);
        setCurrentViewport(scrollViewport);
    }

    public void moveToWithAnimation(float x, float y) {
        Viewport scrollViewport = computeScrollViewport(x, y);
        setCurrentViewportWithAnimation(scrollViewport);
    }

    private Viewport computeScrollViewport(float x, float y) {
        Viewport maxViewport = getMaximumViewport();
        Viewport currentViewport = getCurrentViewport();
        Viewport scrollViewport = new Viewport(currentViewport);

        if (maxViewport.contains(x, y)) {
            final float width = currentViewport.width();
            final float height = currentViewport.height();

            final float halfWidth = width / 2;
            final float halfHeight = height / 2;

            float left = x - halfWidth;
            float top = y + halfHeight;

            left = Math.max(maxViewport.left, Math.min(left, maxViewport.right - width));
            top = Math.max(maxViewport.bottom + height, Math.min(top, maxViewport.top));

            scrollViewport.set(left, top, left + width, top - height);
        }

        return scrollViewport;
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
        Viewport maxViewport = getMaximumViewport();
        Viewport currentViewport = getCurrentViewport();

        return Math.max(maxViewport.width() / currentViewport.width(), maxViewport.height() / currentViewport.height());

    }

    public void setZoomLevel(float x, float y, float zoomLevel) {
        Viewport zoomViewport = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewport(zoomViewport);
    }

    public void setZoomLevelWithAnimation(float x, float y, float zoomLevel) {
        Viewport zoomViewport = computeZoomViewport(x, y, zoomLevel);
        setCurrentViewportWithAnimation(zoomViewport);
    }

    private Viewport computeZoomViewport(float x, float y, float zoomLevel) {
        final Viewport maxViewport = getMaximumViewport();
        Viewport zoomViewport = new Viewport(getMaximumViewport());

        if (maxViewport.contains(x, y)) {

            if (zoomLevel < 1) {
                zoomLevel = 1;
            } else if (zoomLevel > getMaxZoom()) {
                zoomLevel = getMaxZoom();
            }

            final float newWidth = zoomViewport.width() / zoomLevel;
            final float halfWidth = newWidth / 2;

            float left = x - halfWidth;
            float right = x + halfWidth;

            if (left < maxViewport.left) {
                left = maxViewport.left;
                right = left + newWidth;
            } else if (right > maxViewport.right) {
                right = maxViewport.right;
                left = right - newWidth;
            }

            zoomViewport.left = left;
            zoomViewport.right = right;
        }
        return zoomViewport;
    }

    public Viewport getMaximumViewport() {
        return chartRenderer.getMaximumViewport();
    }

    public void setMaximumViewport(Viewport maxViewport) {
        chartRenderer.setMaximumViewport(maxViewport);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewportWithAnimation(Viewport targetViewport) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewport);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewportWithAnimation(Viewport targetViewport, long duration) {
        if (null != targetViewport) {
            viewportAnimator.cancelAnimation();
            viewportAnimator.startAnimation(getCurrentViewport(), targetViewport, duration);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public Viewport getCurrentViewport() {
        return getChartRenderer().getCurrentViewport();
    }

    public void setCurrentViewport(Viewport targetViewport) {
        if (null != targetViewport) {
            chartRenderer.setCurrentViewport(targetViewport);
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

    public void selectValue(SelectedValue selectedValue) {
        chartRenderer.selectValue(selectedValue);
        callTouchListener();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public SelectedValue getSelectedValue() {
        return chartRenderer.getSelectedValue();
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
        final Viewport currentViewport = getCurrentViewport();
        final Viewport maximumViewport = getMaximumViewport();
        if (direction < 0) {
            return currentViewport.left > maximumViewport.left;
        } else {
            return currentViewport.right < maximumViewport.right;
        }
    }

    public LineChartData getChartData() {
        return data;
    }

    public void callTouchListener() {
        SelectedValue selectedValue = chartRenderer.getSelectedValue();

        if (selectedValue.isSet()) {
            PointValue point = data.getLines().get(selectedValue.getFirstIndex()).getValues().get(selectedValue.getSecondIndex());
            onValueTouchListener.onValueSelected(selectedValue.getFirstIndex(), selectedValue.getSecondIndex(), point);
        } else {
            onValueTouchListener.onValueDeselected();
        }
    }

    public LineChartOnValueSelectListener getOnValueTouchListener() {
        return onValueTouchListener;
    }

    public void setOnValueTouchListener(LineChartOnValueSelectListener touchListener) {
        if (null != touchListener) {
            this.onValueTouchListener = touchListener;
        }
    }
}

