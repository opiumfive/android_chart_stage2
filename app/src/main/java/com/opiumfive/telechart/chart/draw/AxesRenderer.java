package com.opiumfive.telechart.chart.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;

import com.opiumfive.telechart.chart.Util;
import com.opiumfive.telechart.chart.IChart;
import com.opiumfive.telechart.chart.model.Axis;

import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.AxisAutoValues;

public class AxesRenderer {

    private static final int DEFAULT_AXIS_MARGIN_DP = 0;
    private static final int LEFT = 1;
    private static final int BOTTOM = 3;

    private static final char[] labelWidthChars = new char[]{
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

    private IChart chart;
    private ChartViewrectHandler chartViewrectHandler;
    private int axisMargin;
    private float density;
    private float scaledDensity;
    private Paint[] labelPaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private Paint[] linePaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private float[] labelBaselineTab = new float[4];
    private int[] labelWidthTab = new int[4];
    private int[] labelTextAscentTab = new int[4];
    private int[] labelTextDescentTab = new int[4];
    private int[] labelDimensionForMarginsTab = new int[4];
    private int[] labelDimensionForStepsTab = new int[4];
    private FontMetricsInt[] fontMetricsTab = new FontMetricsInt[]{new FontMetricsInt(), new FontMetricsInt(), new FontMetricsInt(), new FontMetricsInt()};

    private char[] labelBuffer = new char[64];

    private int[] valuesToDrawNumTab = new int[4];

    private float[][] rawValuesTab = new float[4][0];

    private float[][] autoValuesToDrawTab = new float[4][0];

    private float[][] linesDrawBufferTab = new float[4][0];

    private AxisAutoValues[] autoValuesBufferTab = new AxisAutoValues[]{new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues()};

    public AxesRenderer(Context context, IChart chart) {
        this.chart = chart;
        chartViewrectHandler = chart.getChartViewrectHandler();
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        axisMargin = Util.dp2px(density, DEFAULT_AXIS_MARGIN_DP);

        for (int position = 0; position < 4; ++position) {
            labelPaintTab[position].setStyle(Paint.Style.FILL);
            labelPaintTab[position].setAntiAlias(true);
            linePaintTab[position].setStyle(Paint.Style.STROKE);
            linePaintTab[position].setAntiAlias(true);
        }
    }

    public void onChartSizeChanged() {
        onChartDataOrSizeChanged();
    }

    public void onChartDataChanged() {
        onChartDataOrSizeChanged();
    }

    private void onChartDataOrSizeChanged() {
        initAxis(chart.getChartData().getAxisYLeft(), LEFT);
        initAxis(chart.getChartData().getAxisXBottom(), BOTTOM);
    }

    public void resetRenderer() {
        this.chartViewrectHandler = chart.getChartViewrectHandler();
    }

    private void initAxis(Axis axis, int position) {
        if (null == axis) {
            return;
        }
        initAxisAttributes(axis, position);
        initAxisMargin(axis, position);
        initAxisMeasurements(axis, position);
    }

    private void initAxisAttributes(Axis axis, int position) {
        initAxisPaints(axis, position);
        initAxisTextAlignment(axis, position);
        initAxisDimension(position);
    }

    private void initAxisPaints(Axis axis, int position) {
        labelPaintTab[position].setColor(axis.getTextColor());
        labelPaintTab[position].setTextSize(Util.sp2px(scaledDensity, axis.getTextSize()));
        labelPaintTab[position].getFontMetricsInt(fontMetricsTab[position]);

        linePaintTab[position].setColor(axis.getLineColor());

        labelTextAscentTab[position] = Math.abs(fontMetricsTab[position].ascent);
        labelTextDescentTab[position] = Math.abs(fontMetricsTab[position].descent);
        labelWidthTab[position] = (int) labelPaintTab[position].measureText(labelWidthChars, 0, Axis.DEFAULT_MAX_AXIS_LABEL_CHARS);
    }

    private void initAxisTextAlignment(Axis axis, int position) {
        if (BOTTOM == position) {
            labelPaintTab[position].setTextAlign(Align.CENTER);
        } else if (LEFT == position) {
            if (axis.isInside()) {
                labelPaintTab[position].setTextAlign(Align.LEFT);
            } else {
                labelPaintTab[position].setTextAlign(Align.RIGHT);
            }
        }
    }

    private void initAxisDimension(int position) {
        if (LEFT == position) {
            labelDimensionForMarginsTab[position] = labelWidthTab[position];
            labelDimensionForStepsTab[position] = labelTextAscentTab[position];
        } else if (BOTTOM == position) {
            labelDimensionForMarginsTab[position] = labelTextAscentTab[position] + labelTextDescentTab[position];
            labelDimensionForStepsTab[position] = labelWidthTab[position];
        }
    }

    private void initAxisMargin(Axis axis, int position) {
        int margin = 0;
        if (!axis.isInside()) {
            margin += axisMargin + labelDimensionForMarginsTab[position];
        }
        insetContentRectWithAxesMargins(margin, position);
    }

    private void insetContentRectWithAxesMargins(int axisMargin, int position) {
        if (LEFT == position) {
            chart.getChartViewrectHandler().insetContentRect(axisMargin, 0, 0, 0);
        } else if (BOTTOM == position) {
            chart.getChartViewrectHandler().insetContentRect(0, 0, 0, axisMargin);
        }
    }

    private void initAxisMeasurements(Axis axis, int position) {
        if (LEFT == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = chartViewrectHandler.getContentRectMinusAllMargins().left + axisMargin;
            } else {
                labelBaselineTab[position] = chartViewrectHandler.getContentRectMinusAxesMargins().left - axisMargin;
            }
        } else if (BOTTOM == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = chartViewrectHandler.getContentRectMinusAllMargins().bottom - axisMargin - labelTextDescentTab[position];
            } else {
                labelBaselineTab[position] = chartViewrectHandler.getContentRectMinusAxesMargins().bottom + axisMargin + labelTextAscentTab[position];
            }
        }
    }

    public void drawInBackground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            prepareAxisToDraw(axis, LEFT);
            drawAxisLines(canvas, axis, LEFT);
        }

        axis = chart.getChartData().getAxisXBottom();
        if (null != axis) {
            prepareAxisToDraw(axis, BOTTOM);
            drawAxisLines(canvas, axis, BOTTOM);
        }
    }

    private void prepareAxisToDraw(Axis axis, int position) {
        final Viewrect visibleViewrect = chartViewrectHandler.getVisibleViewport();
        final Rect contentRect = chartViewrectHandler.getContentRectMinusAllMargins();
        boolean isAxisVertical = isAxisVertical(position);
        float start, stop;
        int contentRectDimension;
        if (isAxisVertical) {
            start = visibleViewrect.bottom;
            stop = visibleViewrect.top;
            contentRectDimension = contentRect.height();
        } else {
            start = visibleViewrect.left;
            stop = visibleViewrect.right;
            contentRectDimension = contentRect.width();
        }

        int dim = labelDimensionForStepsTab[position] * 4;
        if (dim == 0) dim = 1;
        int steps = Math.abs(contentRectDimension) / dim;

        Util.generatedAxisValues(start, stop, steps, autoValuesBufferTab[position]);

        if (axis.hasLines() && (linesDrawBufferTab[position].length < autoValuesBufferTab[position].valuesNumber * 4)) {
            linesDrawBufferTab[position] = new float[autoValuesBufferTab[position].valuesNumber * 4];
        }

        if (rawValuesTab[position].length < autoValuesBufferTab[position].valuesNumber) {
            rawValuesTab[position] = new float[autoValuesBufferTab[position].valuesNumber];
        }
        if (autoValuesToDrawTab[position].length < autoValuesBufferTab[position].valuesNumber) {
            autoValuesToDrawTab[position] = new float[autoValuesBufferTab[position].valuesNumber];
        }

        float rawValue;
        int valueToDrawIndex = 0;
        for (int i = 0; i < autoValuesBufferTab[position].valuesNumber; ++i) {
            if (isAxisVertical) {
                rawValue = chartViewrectHandler.computeRawY(autoValuesBufferTab[position].values[i]);
            } else {
                rawValue = chartViewrectHandler.computeRawX(autoValuesBufferTab[position].values[i]);
            }
            if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical)) {
                rawValuesTab[position][valueToDrawIndex] = rawValue;
                autoValuesToDrawTab[position][valueToDrawIndex] = autoValuesBufferTab[position].values[i];
                ++valueToDrawIndex;
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex;
    }

    public void drawInForeground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            drawAxisLabels(canvas, axis, LEFT);
        }

        axis = chart.getChartData().getAxisXBottom();
        if (null != axis) {
            drawAxisLabels(canvas, axis, BOTTOM);
        }
    }

    private boolean checkRawValue(Rect rect, float rawValue, boolean axisInside, int position, boolean isVertical) {
        if (axisInside) {
            if (isVertical) {
                float marginBottom = labelTextAscentTab[BOTTOM] + axisMargin;
                return rawValue <= rect.bottom - marginBottom && rawValue >= rect.top;
            } else {
                float margin = labelWidthTab[position] / 2f;
                return rawValue >= rect.left + margin && rawValue <= rect.right - margin;
            }
        }
        return true;
    }

    private void drawAxisLines(Canvas canvas, Axis axis, int position) {
        final Rect contentRectMargins = chartViewrectHandler.getContentRectMinusAxesMargins();
        float lineX1, lineY1, lineX2, lineY2;
        lineX1 = lineY1 = lineX2 = lineY2 = 0;
        boolean isAxisVertical = isAxisVertical(position);

        if (LEFT == position) {
            lineX1 = contentRectMargins.left;
            lineX2 = contentRectMargins.right;
        } else if (BOTTOM == position) {
            lineY1 = contentRectMargins.top;
            lineY2 = contentRectMargins.bottom;
        }

        if (axis.hasLines()) {
            int valueToDrawIndex = 0;
            for (; valueToDrawIndex < valuesToDrawNumTab[position]; ++valueToDrawIndex) {
                if (isAxisVertical) {
                    lineY1 = lineY2 = rawValuesTab[position][valueToDrawIndex];
                } else {
                    lineX1 = lineX2 = rawValuesTab[position][valueToDrawIndex];
                }
                linesDrawBufferTab[position][valueToDrawIndex * 4] = lineX1;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 1] = lineY1;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 2] = lineX2;
                linesDrawBufferTab[position][valueToDrawIndex * 4 + 3] = lineY2;
            }
            canvas.drawLines(linesDrawBufferTab[position], 0, valueToDrawIndex * 4, linePaintTab[position]);
        }
    }

    private void drawAxisLabels(Canvas canvas, Axis axis, int position) {
        float labelX, labelY;
        labelX = labelY = 0;
        boolean isAxisVertical = isAxisVertical(position);
        if (LEFT == position) {
            labelX = labelBaselineTab[position];
        } else if (BOTTOM == position) {
            labelY = labelBaselineTab[position];
        }

        for (int valueToDrawIndex = 0; valueToDrawIndex < valuesToDrawNumTab[position]; ++valueToDrawIndex) {
            int charsNumber = 0;

            final float value = autoValuesToDrawTab[position][valueToDrawIndex];
            charsNumber = axis.getFormatter().formatValue(labelBuffer, value);

            if (isAxisVertical) {
                labelY = rawValuesTab[position][valueToDrawIndex] - Util.dp2px(density, 4);
            } else {
                labelX = rawValuesTab[position][valueToDrawIndex];
            }

            canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY, labelPaintTab[position]);
        }
    }

    private boolean isAxisVertical(int position) {
        return LEFT == position;
    }

}