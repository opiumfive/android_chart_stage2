package com.opiumfive.telechart.chart;

import android.content.Context;
import android.util.AttributeSet;
import com.opiumfive.telechart.chart.draw.PreviewChartViewrectHandler;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.touchControl.ChartTouchHandler;
import com.opiumfive.telechart.chart.touchControl.PreviewChartTouchHandler;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.draw.PreviewLineChartRenderer;


public class PreviewLineChartView extends LineChartView {

    protected PreviewLineChartRenderer previewChartRenderer;
    protected ChartTouchHandler.OnUpTouchListener onUpTouchListener;

    public PreviewLineChartView(Context context) {
        this(context, null, 0);
    }

    public PreviewLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewLineChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chartViewrectHandler = new PreviewChartViewrectHandler();
        previewChartRenderer = new PreviewLineChartRenderer(context, this, this);
        touchHandler = new PreviewChartTouchHandler(context, this);
        setChartRenderer(previewChartRenderer);
        setChartData(LineChartData.generateDummyData());
    }

    public void setPreviewColor(int color) {
        previewChartRenderer.setPreviewColor(color);
        postInvalidateOnAnimation();
    }

    public void setPreviewBackgroundColor(int color) {
        previewChartRenderer.setBackgroundColor(color);
        postInvalidateOnAnimation();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    @Override
    public void setCurrentViewrectAnimated(Viewrect targetViewrect) {

        if (null != targetViewrect) {
            viewrectAnimator.cancelAnimation();
            Viewrect current = getCurrentViewrect();
            viewrectAnimator.startAnimation(current, targetViewrect, true);
        }
        postInvalidateOnAnimation();
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        previewChartRenderer.onChartSizeChanged();
    }
}
