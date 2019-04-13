package com.opiumfive.telechart.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.opiumfive.telechart.chart.animator.ChartAnimationListener;
import com.opiumfive.telechart.chart.animator.ChartLabelAnimator;
import com.opiumfive.telechart.chart.animator.ChartMorphAnimator;
import com.opiumfive.telechart.chart.animator.ChartViewrectAnimator;

import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.touchControl.ChartTouchHandler;
import com.opiumfive.telechart.chart.animator.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.draw.AxesRenderer;
import com.opiumfive.telechart.chart.draw.LineChartRenderer;

import java.util.List;


public class LineChartView extends View implements IChart, ChartDataProvider {

    protected LineChartData data;
    protected CType cType = CType.LINE;
    protected CType targetType = CType.LINE;
    protected CType originType = CType.LINE;

    protected ChartViewrectHandler chartViewrectHandler;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected LineChartRenderer chartRenderer;
    protected ChartViewrectAnimator viewrectAnimator;
    protected ChartLabelAnimator labelAnimator;
    protected ChartMorphAnimator morphAnimator;
    protected boolean isSelectionOnHover = true;
    protected ZoomInListener zoomInListener;


    public LineChartView(Context context) {
        this(context, null, 0);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        chartViewrectHandler = new ChartViewrectHandler();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);
        this.viewrectAnimator = new ChartViewrectAnimator(this);
        this.labelAnimator = new ChartLabelAnimator(this);
        this.morphAnimator = new ChartMorphAnimator(this);
        this.morphAnimator.setChartAnimationListener(new ChartAnimationListener() {
            @Override
            public void onAnimationStarted() {
            }

            @Override
            public void onAnimationFinished() {
                cType = targetType;
                chartRenderer.setMorphFactor(0f);
                postInvalidateOnAnimation();
            }
        });

        setChartRenderer(new LineChartRenderer(context, this, this));
        setChartData(LineChartData.generateDummyData());
    }

    public void setOnUpTouchListener(ChartTouchHandler.OnUpTouchListener listener) {
        touchHandler.setOnUpTouchListener(listener);
    }

    public CType getType() {
        return cType;
    }

    public CType getTargetType() {
        return targetType;
    }

    public void setType(CType cType) {
        this.cType = cType;
        this.originType = cType;
        this.targetType = cType;
    }

    public void setZoomInListener(ZoomInListener zoomInListener) {
        this.zoomInListener = zoomInListener;
    }

    @Override
    public boolean isSelectionOnHover() {
        return isSelectionOnHover;
    }

    public void setSelectionOnHover(boolean selectionOnHover) {
        isSelectionOnHover = selectionOnHover;
    }

    @Override
    public LineChartData getChartData() {
        return data;
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
        chartViewrectHandler.setContentRect(getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        axesRenderer.onChartSizeChanged();
        chartRenderer.onChartSizeChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        chartRenderer.draw(canvas);

        if (!getType().equals(CType.PIE)) {
            axesRenderer.drawInBackground(canvas);
            axesRenderer.drawInForeground(canvas);
        }

        chartRenderer.drawSelectedValues(canvas);
    }

    public void toggleAxisAnim(Viewrect targetViewrect) {
        axesRenderer.setToggleAnimation(targetViewrect);
    }

    public void postDrawIfNeeded() {
        postInvalidateOnAnimation();
    }

    @Override
    public void postDrawWithMorphFactor(float factor) {
        chartRenderer.setMorphFactor(factor);
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        boolean needInvalidate = touchHandler.handleTouchEvent(event);

        if (needInvalidate) {
            postInvalidateOnAnimation();
        }

        return needInvalidate;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (touchHandler.computeScroll()) {
            postInvalidateOnAnimation();
        }
    }

    public void setViewrectAnimationListener(ChartAnimationListener animationListener) {
        viewrectAnimator.setChartAnimationListener(animationListener);
    }

    public void setViewrectChangeListener(ViewrectChangeListener viewrectChangeListener) {
        chartViewrectHandler.setViewrectChangeListener(viewrectChangeListener);
    }

    public LineChartRenderer getChartRenderer() {
        return chartRenderer;
    }

    public void setChartRenderer(LineChartRenderer renderer) {
        chartRenderer = renderer;
        resetRendererAndTouchHandler();
        postInvalidateOnAnimation();
    }

    public ChartViewrectHandler getChartViewrectHandler() {
        return chartViewrectHandler;
    }

    public void setScrollEnabled(boolean isScrollEnabled) {
        touchHandler.setScrollEnabled(isScrollEnabled);
    }

    public void setValueTouchEnabled(boolean isValueTouchEnabled) {
        touchHandler.setValueTouchEnabled(isValueTouchEnabled);
    }

    public Viewrect getMaximumViewrect() {
        return chartRenderer.getMaximumViewrect();
    }

    public void setMaximumViewrect(Viewrect maxViewrect) {
        chartRenderer.setMaximumViewrect(maxViewrect);
        postInvalidateOnAnimation();
    }

    public void setCurrentViewrectAnimated(Viewrect targetViewrect) {

        if (null != targetViewrect) {
            viewrectAnimator.cancelAnimation();
            Viewrect current = getCurrentViewrect();
            viewrectAnimator.startAnimation(current, targetViewrect);
        }
        postInvalidateOnAnimation();
    }

    @Override
    public void startMorphling(CType cType) {
        if (cType.equals(this.cType)) return;
        targetType = cType;

        if (zoomInListener != null) {
            zoomInListener.zoomed(!cType.equals(originType));
        }

        morphAnimator.cancelAnimation();
        morphAnimator.startAnimation();
    }

    public void setCurrentViewrectAnimatedAdjustingMax(Viewrect targetViewrect, List<Line> lines) {

        if (null != targetViewrect) {
            viewrectAnimator.cancelAnimation();
            Viewrect current = getCurrentViewrect();
            Viewrect targetAdjustedViewrect = chartRenderer.calculateAdjustedViewrect(targetViewrect);
            viewrectAnimator.startAnimationWithToggleLine(current, targetAdjustedViewrect, lines);
        }
        postInvalidateOnAnimation();
    }

    public Viewrect getCurrentViewrect() {
        return getChartRenderer().getCurrentViewrect();
    }

    public void setCurrentViewrect(Viewrect targetViewrect) {

        if (null != targetViewrect) {
            chartRenderer.setCurrentViewrect(targetViewrect);
        }
        postInvalidateOnAnimation();
    }

    public void setCurrentViewrectAdjustingRect(Viewrect targetViewrect, boolean useFilter, float distanceX) {

        if (null != targetViewrect) {
            Viewrect targetAdjustedViewrect = chartRenderer.calculateAdjustedViewrect(targetViewrect);
            Viewrect current = getCurrentViewrect();
            if (useFilter) {
                // Kalman filter to provide smooth min-max depending on scroll speed
                chartViewrectHandler.filterSmooth(current, targetAdjustedViewrect, distanceX);
            }
            chartRenderer.setCurrentViewrect(targetAdjustedViewrect);

            if (axesRenderer.getCurrentLabelViewrect() != null) {
                float diff = (axesRenderer.getCurrentLabelViewrect().top - axesRenderer.getCurrentLabelViewrect().bottom) * 0.05f;

                if (!axesRenderer.isCurrentlyAnimatingLabels() && !labelAnimator.isAnimationStarted() &&
                        (Math.abs(axesRenderer.getCurrentLabelViewrect().top - targetAdjustedViewrect.top) >= diff)) {
                    labelAnimator.startAnimation(targetAdjustedViewrect);
                    Log.d("labelanim", "startAnimation");
                }
            }
        }
        postInvalidateOnAnimation();
    }

    public void setViewrectRecalculation(boolean isEnabled) {
        chartRenderer.setViewrectCalculationEnabled(isEnabled);
    }

    public void recalculateMax() {
        chartRenderer.recalculateMax();
    }

    public void onChartDataChange() {
        chartViewrectHandler.resetContentRect();
        chartRenderer.onChartDataChanged();
        axesRenderer.onChartDataChanged();
        postInvalidateOnAnimation();
    }

    protected void resetRendererAndTouchHandler() {
        this.chartRenderer.resetRenderer();
        this.axesRenderer.resetRenderer();
        this.touchHandler.resetTouchHandler();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final Viewrect currentViewrect = getCurrentViewrect();
        final Viewrect maximumViewrect = getMaximumViewrect();
        if (direction < 0) {
            return currentViewrect.left > maximumViewrect.left;
        } else {
            return currentViewrect.right < maximumViewrect.right;
        }
    }

    public interface ZoomInListener {
        void zoomed(boolean zoomState);
    }
}
