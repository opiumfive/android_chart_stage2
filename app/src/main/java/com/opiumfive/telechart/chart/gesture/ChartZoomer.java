package com.opiumfive.telechart.chart.gesture;

import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.opiumfive.telechart.chart.render.ChartViewportHandler;
import com.opiumfive.telechart.chart.model.Viewrect;


public class ChartZoomer {

    public static final float ZOOM_AMOUNT = 0.25f;

    private ZoomerCompat zoomer;
    private PointF zoomFocalPoint = new PointF();
    private PointF viewportFocus = new PointF();
    private Viewrect scrollerStartViewrect = new Viewrect();

    public ChartZoomer(Context context) {
        zoomer = new ZoomerCompat(context);
    }

    public boolean startZoom(MotionEvent e, ChartViewportHandler computator) {
        zoomer.forceFinished(true);
        scrollerStartViewrect.set(computator.getCurrentViewrect());
        if (!computator.rawPixelsToDataPoint(e.getX(), e.getY(), zoomFocalPoint)) {
            return false;
        }
        zoomer.startZoom(ZOOM_AMOUNT);
        return true;
    }

    public boolean computeZoom(ChartViewportHandler computator) {
        if (zoomer.computeZoom()) {
            final float newWidth = (1.0f - zoomer.getCurrZoom()) * scrollerStartViewrect.width();
            final float newHeight = (1.0f - zoomer.getCurrZoom()) * scrollerStartViewrect.height();
            final float pointWithinViewportX = (zoomFocalPoint.x - scrollerStartViewrect.left) / scrollerStartViewrect.width();
            final float pointWithinViewportY = (zoomFocalPoint.y - scrollerStartViewrect.bottom) / scrollerStartViewrect.height();

            float left = zoomFocalPoint.x - newWidth * pointWithinViewportX;
            float top = zoomFocalPoint.y + newHeight * (1 - pointWithinViewportY);
            float right = zoomFocalPoint.x + newWidth * (1 - pointWithinViewportX);
            float bottom = zoomFocalPoint.y - newHeight * pointWithinViewportY;
            setCurrentViewport(computator, left, top, right, bottom);
            return true;
        }
        return false;
    }

    public boolean scale(ChartViewportHandler computator, float focusX, float focusY, float scale) {

        final float newWidth = scale * computator.getCurrentViewrect().width();
        final float newHeight = scale * computator.getCurrentViewrect().height();
        if (!computator.rawPixelsToDataPoint(focusX, focusY, viewportFocus)) {
            return false;
        }

        float left = viewportFocus.x - (focusX - computator.getContentRectMinusAllMargins().left) * (newWidth / computator.getContentRectMinusAllMargins().width());
        float top = viewportFocus.y + (focusY - computator.getContentRectMinusAllMargins().top) * (newHeight / computator.getContentRectMinusAllMargins().height());
        float right = left + newWidth;
        float bottom = top - newHeight;
        setCurrentViewport(computator, left, top, right, bottom);
        return true;
    }

    private void setCurrentViewport(ChartViewportHandler computator, float left, float top, float right, float bottom) {
        Viewrect currentViewrect = computator.getCurrentViewrect();
        computator.setCurrentViewport(left, currentViewrect.top, right, currentViewrect.bottom);
    }
}
