package com.opiumfive.telechart.fastdraw;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.LineChartDataProvider;
import com.opiumfive.telechart.chart.animation.ChartDataAnimator;
import com.opiumfive.telechart.chart.animation.ChartViewportAnimator;
import com.opiumfive.telechart.chart.computator.ChartComputator;
import com.opiumfive.telechart.chart.gesture.ChartTouchHandler;
import com.opiumfive.telechart.chart.listener.DummyLineChartOnValueSelectListener;
import com.opiumfive.telechart.chart.listener.LineChartOnValueSelectListener;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.SelectedValue;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.renderer.AxesRenderer;
import com.opiumfive.telechart.chart.renderer.LineChartRenderer;


public class FastTextureView extends TextureView implements ILineChart, TextureView.SurfaceTextureListener, LineChartDataProvider {

    protected LineChartData data;
    protected LineChartOnValueSelectListener onValueTouchListener = new DummyLineChartOnValueSelectListener();


    protected ChartComputator chartComputator;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected LineChartRenderer chartRenderer;
    protected ChartDataAnimator dataAnimator;
    protected ChartViewportAnimator viewportAnimator;
    protected boolean isInteractive = true;
    protected boolean isContainerScrollEnabled = false;


    private SceneModelComposer sceneComposer;

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

        sceneComposer = new SceneModelComposer(chartComputator, axesRenderer, chartRenderer);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        //setLayerType(View.LAYER_TYPE_HARDWARE, null);

        chartComputator = new ChartComputator();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);
        this.viewportAnimator = new ChartViewportAnimator(this);
        this.dataAnimator = new ChartDataAnimator(this);

        setChartRenderer(new LineChartRenderer(context, this, this));
        setLineChartData(LineChartData.generateDummyData());



        invalidate();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        drawingThread = new DrawingThread(new TextureViewHolder(this));
        drawingThread.setRunning(true);
        drawingThread.setSceneComposer(sceneComposer);
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void invalidate() {
        super.invalidate();

    }

    @Override
    public LineChartData getChartData() {
        return data;
    }

    @Override
    public LineChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    @Override
    public void setChartRenderer(LineChartRenderer renderer) {
        chartRenderer = renderer;
        chartRenderer.resetRenderer();
        axesRenderer.resetRenderer();
        touchHandler.resetTouchHandler();
    }

    @Override
    public AxesRenderer getAxesRenderer() {
        return axesRenderer;
    }

    @Override
    public ChartComputator getChartComputator() {
        return chartComputator;
    }

    @Override
    public ChartTouchHandler getTouchHandler() {
        return touchHandler;
    }

    @Override
    public void callTouchListener() {
        SelectedValue selectedValue = chartRenderer.getSelectedValue();

        if (selectedValue.isSet()) {
            PointValue point = data.getLines().get(selectedValue.getFirstIndex()).getValues().get(selectedValue.getSecondIndex());
            onValueTouchListener.onValueSelected(selectedValue.getFirstIndex(), selectedValue.getSecondIndex(), point);
        } else {
            onValueTouchListener.onValueDeselected();
        }
    }

    @Override
    public void setCurrentViewport(Viewport targetViewport) {
        if (null != targetViewport) {
            chartRenderer.setCurrentViewport(targetViewport);
        }
    }

    @Override
    public void animationDataUpdate(float scale) {
        getChartData().update(scale);
        chartRenderer.onChartViewportChanged();
    }

    @Override
    public void animationDataFinished() {
        getChartData().finish();
        chartRenderer.onChartViewportChanged();
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

        chartComputator.resetContentRect();
        chartRenderer.onChartDataChanged();
        axesRenderer.onChartDataChanged();

        invalidate();
    }

    @Override
    public Viewport getMaximumViewport() {
        return chartRenderer.getMaximumViewport();
    }

    public void setZoomEnabled(boolean isZoomEnabled) {
        touchHandler.setZoomEnabled(isZoomEnabled);
    }

    public void setScrollEnabled(boolean isScrollEnabled) {
        touchHandler.setScrollEnabled(isScrollEnabled);
    }
}

