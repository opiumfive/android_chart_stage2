package com.opiumfive.telechart.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.opiumfive.telechart.chart.animator.ChartAnimationListener;
import com.opiumfive.telechart.chart.animator.ChartViewrectAnimator;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.draw.ChartViewrectHandler;
import com.opiumfive.telechart.chart.touchControl.ChartTouchHandler;
import com.opiumfive.telechart.chart.animator.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.draw.AxesRenderer;
import com.opiumfive.telechart.chart.draw.LineChartRenderer;


public class ChartView extends View implements IChart, ChartDataProvider {

    protected LineChartData data;

    protected ChartViewrectHandler chartViewrectHandler;
    protected AxesRenderer axesRenderer;
    protected ChartTouchHandler touchHandler;
    protected LineChartRenderer chartRenderer;
    protected ChartViewrectAnimator viewrectAnimator;

    public ChartView(Context context) {
        this(context, null, 0);
    }

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        chartViewrectHandler = new ChartViewrectHandler();
        touchHandler = new ChartTouchHandler(context, this);
        axesRenderer = new AxesRenderer(context, this);
        this.viewrectAnimator = new ChartViewrectAnimator(this);

        setChartRenderer(new LineChartRenderer(context, this, this));
        setChartData(LineChartData.generateDummyData());
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
        chartRenderer.onChartSizeChanged();
        axesRenderer.onChartSizeChanged();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isEnabled()) {
            axesRenderer.drawInBackground(canvas);
            int clipRestoreCount = canvas.save();
            canvas.clipRect(chartViewrectHandler.getContentRectMinusAllMargins());
            chartRenderer.draw(canvas);
            canvas.restoreToCount(clipRestoreCount);

            axesRenderer.drawInForeground(canvas);
            chartRenderer.drawSelectedValues(canvas);
        } else {
            canvas.drawColor(Util.DEFAULT_COLOR);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        boolean needInvalidate = touchHandler.handleTouchEvent(event);

        if (needInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (touchHandler.computeScroll()) {
            ViewCompat.postInvalidateOnAnimation(this);
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
        ViewCompat.postInvalidateOnAnimation(this);
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
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewrectAnimated(Viewrect targetViewrect) {

        if (null != targetViewrect) {
            viewrectAnimator.cancelAnimation();
            Viewrect current = getCurrentViewrect();
            viewrectAnimator.startAnimation(current, targetViewrect);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCurrentViewrectAnimatedAdjustingMax(Viewrect targetViewrect, Line line) {

        if (null != targetViewrect) {
            viewrectAnimator.cancelAnimation();
            Viewrect current = getCurrentViewrect();
            Viewrect targetAdjustedViewrect = chartRenderer.calculateAdjustedViewrect(targetViewrect);
            viewrectAnimator.startAnimationWithToggleLine(current, targetAdjustedViewrect, line);
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public Viewrect getCurrentViewrect() {
        return getChartRenderer().getCurrentViewrect();
    }

    public void setCurrentViewrect(Viewrect targetViewrect) {

        if (null != targetViewrect) {
            chartRenderer.setCurrentViewrect(targetViewrect);
        }
        ViewCompat.postInvalidateOnAnimation(this);
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
        }
        ViewCompat.postInvalidateOnAnimation(this);
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
        ViewCompat.postInvalidateOnAnimation(this);
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
}
