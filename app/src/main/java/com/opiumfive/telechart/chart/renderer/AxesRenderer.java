package com.opiumfive.telechart.chart.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.opiumfive.telechart.chart.ILineChart;
import com.opiumfive.telechart.chart.model.Axis;
import com.opiumfive.telechart.chart.model.AxisValue;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.util.AxisAutoValues;
import com.opiumfive.telechart.chart.util.ChartUtils;
import com.opiumfive.telechart.chart.util.FloatUtils;

public class AxesRenderer {

    private static final int DEFAULT_AXIS_MARGIN_DP = 2;
    private static final int LEFT = 1;
    private static final int BOTTOM = 3;

    private static final char[] labelWidthChars = new char[]{
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};

    private ILineChart chart;
    private ChartViewportHandler computator;
    private int axisMargin;
    private float density;
    private float scaledDensity;
    private Paint[] labelPaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private Paint[] linePaintTab = new Paint[]{new Paint(), new Paint(), new Paint(), new Paint()};
    private float[] nameBaselineTab = new float[4];
    private float[] labelBaselineTab = new float[4];
    private float[] separationLineTab = new float[4];
    private int[] labelWidthTab = new int[4];
    private int[] labelTextAscentTab = new int[4];
    private int[] labelTextDescentTab = new int[4];
    private int[] labelDimensionForMarginsTab = new int[4];
    private int[] labelDimensionForStepsTab = new int[4];
    private int[] tiltedLabelXTranslation = new int[4];
    private int[] tiltedLabelYTranslation = new int[4];
    private FontMetricsInt[] fontMetricsTab = new FontMetricsInt[]{new FontMetricsInt(), new FontMetricsInt(), new FontMetricsInt(), new FontMetricsInt()};

    private char[] labelBuffer = new char[64];

    private int[] valuesToDrawNumTab = new int[4];

    private float[][] rawValuesTab = new float[4][0];

    private float[][] autoValuesToDrawTab = new float[4][0];

    private AxisValue[][] valuesToDrawTab = new AxisValue[4][0];

    private float[][] linesDrawBufferTab = new float[4][0];

    private AxisAutoValues[] autoValuesBufferTab = new AxisAutoValues[]{new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues(), new AxisAutoValues()};

    public AxesRenderer(Context context, ILineChart chart) {
        this.chart = chart;
        computator = chart.getChartViewportHandler();
        density = context.getResources().getDisplayMetrics().density;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        axisMargin = ChartUtils.dp2px(density, DEFAULT_AXIS_MARGIN_DP);

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
    }

    public void resetRenderer() {
        this.computator = chart.getChartViewportHandler();
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
        if (axis.hasTiltedLabels()) {
            initAxisDimensionForTiltedLabels(position);
            intiTiltedLabelsTranslation(axis, position);
        } else {
            initAxisDimension(position);
        }
    }

    private void initAxisPaints(Axis axis, int position) {
        Typeface typeface = axis.getTypeface();
        if (null != typeface) {
            labelPaintTab[position].setTypeface(typeface);
        }
        labelPaintTab[position].setColor(axis.getTextColor());
        labelPaintTab[position].setTextSize(ChartUtils.sp2px(scaledDensity, axis.getTextSize()));
        labelPaintTab[position].getFontMetricsInt(fontMetricsTab[position]);

        linePaintTab[position].setColor(axis.getLineColor());

        labelTextAscentTab[position] = Math.abs(fontMetricsTab[position].ascent);
        labelTextDescentTab[position] = Math.abs(fontMetricsTab[position].descent);
        labelWidthTab[position] = (int) labelPaintTab[position].measureText(labelWidthChars, 0, axis.getMaxLabelChars());
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

    private void initAxisDimensionForTiltedLabels(int position) {
        int pythagoreanFromLabelWidth = (int) Math.sqrt(Math.pow(labelWidthTab[position], 2) / 2);
        int pythagoreanFromAscent = (int) Math.sqrt(Math.pow(labelTextAscentTab[position], 2) / 2);
        labelDimensionForMarginsTab[position] = pythagoreanFromAscent + pythagoreanFromLabelWidth;
        labelDimensionForStepsTab[position] = Math.round(labelDimensionForMarginsTab[position] * 0.75f);
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

    private void intiTiltedLabelsTranslation(Axis axis, int position) {
        int pythagoreanFromLabelWidth = (int) Math.sqrt(Math.pow(labelWidthTab[position], 2) / 2);
        int pythagoreanFromAscent = (int) Math.sqrt(Math.pow(labelTextAscentTab[position], 2) / 2);
        int dx = 0;
        int dy = 0;
        if (axis.isInside()) {
            if (LEFT == position) {
                dx = pythagoreanFromAscent;
            } else if (BOTTOM == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            }
        } else {
            if (LEFT == position) {
                dy = -pythagoreanFromLabelWidth / 2;
            } else if (BOTTOM == position) {
                dy = (pythagoreanFromAscent + pythagoreanFromLabelWidth / 2) - labelTextAscentTab[position];
            }
        }
        tiltedLabelXTranslation[position] = dx;
        tiltedLabelYTranslation[position] = dy;
    }

    private void initAxisMargin(Axis axis, int position) {
        int margin = 0;
        if (!axis.isInside() && (axis.isAutoGenerated() || !axis.getValues().isEmpty())) {
            margin += axisMargin + labelDimensionForMarginsTab[position];
        }
        margin += getAxisNameMargin(axis, position);
        insetContentRectWithAxesMargins(margin, position);
    }

    private int getAxisNameMargin(Axis axis, int position) {
        int margin = 0;
        if (!TextUtils.isEmpty(axis.getName())) {
            margin += labelTextAscentTab[position];
            margin += labelTextDescentTab[position];
            margin += axisMargin;
        }
        return margin;
    }

    private void insetContentRectWithAxesMargins(int axisMargin, int position) {
        if (LEFT == position) {
            chart.getChartViewportHandler().insetContentRect(axisMargin, 0, 0, 0);
        } else if (BOTTOM == position) {
            chart.getChartViewportHandler().insetContentRect(0, 0, 0, axisMargin);
        }
    }

    private void initAxisMeasurements(Axis axis, int position) {
        if (LEFT == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().left + axisMargin;
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().left - axisMargin - labelTextDescentTab[position];
            } else {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().left - axisMargin;
                nameBaselineTab[position] = labelBaselineTab[position] - axisMargin - labelTextDescentTab[position] - labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().left;
        } else if (BOTTOM == position) {
            if (axis.isInside()) {
                labelBaselineTab[position] = computator.getContentRectMinusAllMargins().bottom - axisMargin - labelTextDescentTab[position];
                nameBaselineTab[position] = computator.getContentRectMinusAxesMargins().bottom + axisMargin + labelTextAscentTab[position];
            } else {
                labelBaselineTab[position] = computator.getContentRectMinusAxesMargins().bottom + axisMargin + labelTextAscentTab[position];
                nameBaselineTab[position] = labelBaselineTab[position] + axisMargin + labelDimensionForMarginsTab[position];
            }
            separationLineTab[position] = computator.getContentRectMinusAllMargins().bottom;
        } else {
            throw new IllegalArgumentException("Invalid axis position: " + position);
        }
    }

    public void drawInBackground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            prepareAxisToDraw(axis, LEFT);
            drawAxisLines(canvas, axis, LEFT);
        }
        Axis axisBot = chart.getChartData().getAxisXBottom();
        if (null != axisBot) {
            prepareAxisToDraw(axisBot, BOTTOM);
            drawAxisLines(canvas, axisBot, BOTTOM);
        }
    }

    private void prepareAxisToDraw(Axis axis, int position) {
        if (axis.isAutoGenerated()) {
            prepareAutoGeneratedAxis(axis, position);
        } else {
            prepareCustomAxis(axis, position);
        }
    }

    public void drawInForeground(Canvas canvas) {
        Axis axis = chart.getChartData().getAxisYLeft();
        if (null != axis) {
            drawAxisLabels(canvas, axis, LEFT);
        }
        Axis axisBot = chart.getChartData().getAxisXBottom();
        if (null != axisBot) {
            drawAxisLabels(canvas, axisBot, BOTTOM);
        }
    }

    private void prepareCustomAxis(Axis axis, int position) {
        final Viewport maxViewport = computator.getMaximumViewport();
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        boolean isAxisVertical = isAxisVertical(position);
        float viewportMin, viewportMax;
        float scale = 1;
        if (isAxisVertical) {
            if (maxViewport.height() > 0 && visibleViewport.height() > 0) {
                scale = contentRect.height() * (maxViewport.height() / visibleViewport.height());
            }
            viewportMin = visibleViewport.bottom;
            viewportMax = visibleViewport.top;
        } else {
            if (maxViewport.width() > 0 && visibleViewport.width() > 0) {
                scale = contentRect.width() * (maxViewport.width() / visibleViewport.width());
            }
            viewportMin = visibleViewport.left;
            viewportMax = visibleViewport.right;
        }
        if (scale == 0) {
            scale = 1;
        }
        int module = (int) Math.max(1, Math.ceil((axis.getValues().size() * labelDimensionForStepsTab[position] * 1.5) / scale));

        if (axis.hasLines() && (linesDrawBufferTab[position].length < axis.getValues().size() * 4)) {
            linesDrawBufferTab[position] = new float[axis.getValues().size() * 4];
        }

        if (rawValuesTab[position].length < axis.getValues().size()) {
            rawValuesTab[position] = new float[axis.getValues().size()];
        }

        if (valuesToDrawTab[position].length < axis.getValues().size()) {
            valuesToDrawTab[position] = new AxisValue[axis.getValues().size()];
        }

        float rawValue;
        int valueIndex = 0;
        int valueToDrawIndex = 0;
        for (AxisValue axisValue : axis.getValues()) {
            final float value = axisValue.getValue();
            if (value >= viewportMin && value <= viewportMax) {
                if (0 == valueIndex % module) {
                    if (isAxisVertical) {
                        rawValue = computator.computeRawY(value);
                    } else {
                        rawValue = computator.computeRawX(value);
                    }
                    if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical)) {
                        rawValuesTab[position][valueToDrawIndex] = rawValue;
                        valuesToDrawTab[position][valueToDrawIndex] = axisValue;
                        ++valueToDrawIndex;
                    }
                }
                ++valueIndex;
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex;
    }

    private void prepareAutoGeneratedAxis(Axis axis, int position) {
        final Viewport visibleViewport = computator.getVisibleViewport();
        final Rect contentRect = computator.getContentRectMinusAllMargins();
        boolean isAxisVertical = isAxisVertical(position);
        float start, stop;
        int contentRectDimension;
        if (isAxisVertical) {
            start = visibleViewport.bottom;
            stop = visibleViewport.top;
            contentRectDimension = contentRect.height();
        } else {
            start = visibleViewport.left;
            stop = visibleViewport.right;
            contentRectDimension = contentRect.width();
        }

        int dim = labelDimensionForStepsTab[position] * 2;
        if (dim == 0) dim = 1;
        int steps = Math.abs(contentRectDimension) / dim;

        FloatUtils.computeAutoGeneratedAxisValues(start, stop, steps, autoValuesBufferTab[position]);

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
                rawValue = computator.computeRawY(autoValuesBufferTab[position].values[i]);
            } else {
                rawValue = computator.computeRawX(autoValuesBufferTab[position].values[i]);
            }
            if (checkRawValue(contentRect, rawValue, axis.isInside(), position, isAxisVertical)) {
                rawValuesTab[position][valueToDrawIndex] = rawValue;
                autoValuesToDrawTab[position][valueToDrawIndex] = autoValuesBufferTab[position].values[i];
                ++valueToDrawIndex;
            }
        }
        valuesToDrawNumTab[position] = valueToDrawIndex;
    }

    private boolean checkRawValue(Rect rect, float rawValue, boolean axisInside, int position, boolean isVertical) {
        if (axisInside) {
            if (isVertical) {
                float marginBottom = labelTextAscentTab[BOTTOM] + axisMargin;
                if (rawValue <= rect.bottom - marginBottom && rawValue >= rect.top) {
                    return true;
                } else {
                    return false;
                }
            } else {
                float margin = labelWidthTab[position] / 2;
                if (rawValue >= rect.left + margin && rawValue <= rect.right - margin) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void drawAxisLines(Canvas canvas, Axis axis, int position) {
        final Rect contentRectMargins = computator.getContentRectMinusAxesMargins();
        float separationX1, separationY1, separationX2, separationY2;
        separationX1 = separationY1 = separationX2 = separationY2 = 0;
        float lineX1, lineY1, lineX2, lineY2;
        lineX1 = lineY1 = lineX2 = lineY2 = 0;
        boolean isAxisVertical = isAxisVertical(position);

        if (LEFT == position) {
            separationX1 = separationX2 = separationLineTab[position];
            separationY1 = contentRectMargins.bottom;
            separationY2 = contentRectMargins.top;
            lineX1 = contentRectMargins.left;
            lineX2 = contentRectMargins.right;
        } else if (BOTTOM == position) {
            separationX1 = contentRectMargins.left;
            separationX2 = contentRectMargins.right;
            separationY1 = separationY2 = separationLineTab[position];
            lineY1 = contentRectMargins.top;
            lineY2 = contentRectMargins.bottom;
        }

        if (axis.hasSeparationLine()) {
            canvas.drawLine(separationX1, separationY1, separationX2, separationY2, labelPaintTab[position]);
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
            if (axis.isAutoGenerated()) {
                final float value = autoValuesToDrawTab[position][valueToDrawIndex];
                charsNumber = axis.getFormatter().formatValueForAutoGeneratedAxis(labelBuffer, value, autoValuesBufferTab[position].decimals);
            } else {
                AxisValue axisValue = valuesToDrawTab[position][valueToDrawIndex];
                charsNumber = axis.getFormatter().formatValueForManualAxis(labelBuffer, axisValue);
            }

            if (isAxisVertical) {
                labelY = rawValuesTab[position][valueToDrawIndex];
            } else {
                labelX = rawValuesTab[position][valueToDrawIndex];
            }

            if (axis.hasTiltedLabels()) {
                canvas.save();
                canvas.translate(tiltedLabelXTranslation[position], tiltedLabelYTranslation[position]);
                canvas.rotate(-45, labelX, labelY);
                canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY, labelPaintTab[position]);
                canvas.restore();
            } else {
                canvas.drawText(labelBuffer, labelBuffer.length - charsNumber, charsNumber, labelX, labelY, labelPaintTab[position]);
            }
        }
    }

    private boolean isAxisVertical(int position) {
        return LEFT == position;
    }

}