package com.opiumfive.telechart.chart;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

import com.opiumfive.telechart.chart.model.AxisValues;

public class Util {

    public static final int DEFAULT_COLOR = Color.parseColor("#DFDFDF");

    @ColorInt
    public static int getColorFromAttr(Context context, @AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static Drawable getDrawableFromAttr(Context context, @AttrRes int resId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[] { resId });
        int attributeResourceId = a.getResourceId(0, 0);
        return ContextCompat.getDrawable(context, attributeResourceId);
    }

    public static int dp2px(float density, int dp) {
        if (dp == 0) {
            return 0;
        }
        return (int) (dp * density + 0.5f);

    }

    public static int sp2px(float scaledDensity, int sp) {
        if (sp == 0) {
            return 0;
        }
        return (int) (sp * scaledDensity + 0.5f);
    }

    public static double nextUp(double d) {
        if (Double.isNaN(d) || d == Double.POSITIVE_INFINITY) {
            return d;
        } else {
            d += 0.0;
            return Double.longBitsToDouble(Double.doubleToRawLongBits(d) + ((d >= 0.0) ? +1 : -1));
        }
    }

    public static float roundToOneSignificantFigure(double num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        int power = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    public static int formatFloat(final char[] formattedValue, float value, int endIndex) {
        if (value == 0) {
            formattedValue[endIndex - 1] = '0';
            return 1;
        }

        long lval = Math.round(value);
        int index = endIndex - 1;
        int charsNumber = 0;
        while (lval != 0) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            formattedValue[index--] = (char) (digit + '0');
            charsNumber++;
        }

        return charsNumber;
    }

    public static void generatedAxisValues(float start, float stop, int steps, AxisValues outValues) {
        double range = stop - start;
        if (steps == 0 || range <= 0) {
            outValues.values = new float[]{};
            outValues.valuesNumber = 0;
            return;
        }

        double rawInterval = range / steps;
        double interval = roundToOneSignificantFigure(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            interval = Math.floor(10 * intervalMagnitude);
        }

        double first = Math.ceil(start / interval) * interval;
        double last = nextUp(Math.floor(stop / interval) * interval);

        double intervalValue;
        int valueIndex;
        int valuesNum = 0;
        for (intervalValue = first; intervalValue <= last; intervalValue += interval) {
            ++valuesNum;
        }

        outValues.valuesNumber = valuesNum;

        if (outValues.values.length < valuesNum) {
            outValues.values = new float[valuesNum];
        }

        for (intervalValue = first, valueIndex = 0; valueIndex < valuesNum; intervalValue += interval, ++valueIndex) {
            outValues.values[valueIndex] = (float) intervalValue;
        }
    }

    public static void generatedVerticalAxisValues(float start, float stop, int steps, AxisValues outValues) {

        double range = stop - start;
        if (steps == 0 || range <= 0) {
            outValues.values = new float[]{};
            outValues.valuesNumber = 0;
            return;
        }

        float interval = (stop - start) / steps;

        float first = start + interval / 2;

        int valuesNum = steps;

        outValues.valuesNumber = valuesNum;

        if (outValues.values.length < valuesNum) {
            outValues.values = new float[valuesNum];
        }

        float intervalValue;
        int valueIndex;
        for (intervalValue = first, valueIndex = 0; valueIndex < valuesNum; intervalValue += interval, ++valueIndex) {
            outValues.values[valueIndex] = round(intervalValue);
        }
    }

    public static float round(float num) {
        final float d = (float) Math.ceil((float) Math.log10(num < 0 ? -num : num));
        int power = 3 - (int) d;
        if (power > 0) power = 0;
        final float magnitude = (float) Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }
}
