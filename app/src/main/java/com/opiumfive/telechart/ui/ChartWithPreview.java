package com.opiumfive.telechart.ui;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.CType;
import com.opiumfive.telechart.chart.GodChartView;
import com.opiumfive.telechart.chart.PreviewGodChartView;
import com.opiumfive.telechart.chart.animator.ChartAnimationListener;
import com.opiumfive.telechart.chart.animator.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.Axis;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.valueFormat.DateValueFormatter;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.DataMapper;
import com.opiumfive.telechart.data.State;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.opiumfive.telechart.Settings.INITIAL_PREVIEW_SCALE;
import static com.opiumfive.telechart.Settings.TOP_RECT_DATE_FORMAT;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;

public class ChartWithPreview extends LinearLayout {

    private GodChartView chart;
    private PreviewGodChartView previewChart;
    private CheckerList checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    private TextSwitcher name;
    private TextSwitcher dates;
    private boolean shouldAnimateRect = false;
    private boolean isAnimatingPreview = true;
    private String chartTitle;
    private boolean zoomState = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(TOP_RECT_DATE_FORMAT, Locale.ENGLISH);

    private ViewrectChangeListener previewRectListener = new ViewrectChangeListener() {
        @Override
        public void onViewrectChanged(Viewrect newViewrect, float distanceX) {
            if (isAnimatingPreview) {
                chart.setCurrentViewrectAdjustingRect(newViewrect, shouldAnimateRect, distanceX);
                shouldAnimateRect = true;

                String date = dateFormat.format((long) newViewrect.left) + " - " + dateFormat.format((long) newViewrect.right);
                setDatesTextAnimated(date);
            }
        }
    };

    private ChartAnimationListener previewAnimListener = new ChartAnimationListener() {
        @Override
        public void onAnimationStarted() {
            isAnimatingPreview = false;
            checkboxList.setEnabled(false);
        }

        @Override
        public void onAnimationFinished() {
            isAnimatingPreview = true;
            checkboxList.setEnabled(true);
        }
    };

    public ChartWithPreview(Context context, ChartData chartData, String title, State state, CType cType) {
        super(context);
        inflate(context, R.layout.chart_with_preview, this);

        chartTitle = title;

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);
        checkboxList = findViewById(R.id.checkboxList);
        name = findViewById(R.id.name);
        dates = findViewById(R.id.dates);

        dates.setFactory(() -> {
            TextView textView = new TextView(getContext());
            textView.setTextAppearance(getContext(), R.style.DatesTextView);
            return textView;
        });
        dates.setInAnimation(context, R.anim.text_fade_in);
        dates.setOutAnimation(context, R.anim.text_fade_out);
        dates.setAnimateFirstView(false);

        name.setFactory(new ViewSwitcher.ViewFactory() {

            boolean titleInitiated = false;

            @Override
            public View makeView() {
                TextView textView = new TextView(getContext());
                textView.setTextAppearance(getContext(), titleInitiated ? R.style.TitleTextView : R.style.ZoomOutTextVuew);
                if (!titleInitiated) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_icon, 0, 0, 0);
                    textView.setCompoundDrawablePadding(context.getResources().getDimensionPixelSize(R.dimen.half_margin));
                }
                titleInitiated = true;
                return textView;
            }
        });

        name.setInAnimation(context, R.anim.zoom_fade_in);
        name.setOutAnimation(context, R.anim.zoom_fade_out);
        name.setAnimateFirstView(false);

        name.setOnClickListener((v) -> {
            if (chart.getType().equals(CType.PIE)) {
                chart.startMorphling(CType.AREA);
            }
        });

        chart.setType(cType);
        chart.setSelectionOnHover(!cType.equals(CType.AREA));

        chart.setZoomInListener(zoomState -> {
            this.zoomState = zoomState;
            name.setText(zoomState ? context.getString(R.string.zoom_out) : chartTitle);
            String date = dateFormat.format((long) chart.getCurrentViewrect().left) + " - " + dateFormat.format((long) chart.getCurrentViewrect().right);
            setDatesTextAnimated(date);
        });

        previewChart.setType(cType);
        name.setText(title);
        if (cType.equals(CType.DAILY_BAR)) {
            checkboxList.setVisibility(GONE);
        }
        setChartData(chartData, state);
    }

    public void setChartData(ChartData chartData, State state) {
        data = DataMapper.mapFromPlainData(chartData);

        data.setAxisXBottom(
                new Axis()
                        .setHasLines(false)
                        .setFormatter(new DateValueFormatter())
                        .setInside(false)
                        .setTextColor(getColorFromAttr(getContext(), R.attr.labelColor))
        );

        if (state != null && state.getLinesState() != null) {
            for (int i = 0; i < data.getLines().size(); i++) {
                Line line = data.getLines().get(i);
                line.setActive(state.getLinesState()[i]);
                if (!state.getLinesState()[i]) {
                    line.setAlpha(0f);
                }
            }
        }

        if (chart.getType().equals(CType.LINE_2Y)) {
            data.setAxisYRight(
                    new Axis()
                            .setHasLines(false)
                            .setLineColor(getColorFromAttr(getContext(), R.attr.gridColor))
                            .setTextColor(data.getLines().get(1).getColor())
            );

            data.setAxisYLeft(
                    new Axis()
                            .setHasLines(true)
                            .setLineColor(getColorFromAttr(getContext(), R.attr.gridColor))
                            .setTextColor(data.getLines().get(0).getColor())
            );

            data.prepare2YType();
        } else {
            data.setAxisYLeft(
                    new Axis()
                            .setHasLines(true)
                            .setLineColor(getColorFromAttr(getContext(), R.attr.gridColor))
                            .setTextColor(getColorFromAttr(getContext(), R.attr.labelColor))
            );
        }

        previewData = new LineChartData(data);
        previewData.setAxisYLeft(null);
        previewData.setAxisXBottom(null);
        previewData.setAxisYRight(null);

        chart.setChartData(data);
        chart.setScrollEnabled(false);

        chart.setViewrectRecalculation(false);
        chart.setValueTouchEnabled(true);

        previewChart.setChartData(previewData);
        previewChart.setViewrectChangeListener(previewRectListener);
        previewChart.setViewrectAnimationListener(previewAnimListener);
        previewChart.setViewrectRecalculation(false);
        previewChart.setPreviewColor(getColorFromAttr(getContext(), R.attr.previewFrameColor));
        previewChart.setPreviewBackgroundColor(getColorFromAttr(getContext(), R.attr.previewBackColor));

        // for y-animation trigger
        previewChart.setOnUpTouchListener(() -> {
            chart.initiateYAxisAnimation(chart.getCurrentViewrect(), true);
        });

        if (state != null && state.getCurrentViewrect() != null) {
            previewChart.setCurrentViewrect(state.getCurrentViewrect());
        } else {
            Viewrect tempViewrect = new Viewrect(chart.getMaximumViewrect());
            float dx = tempViewrect.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
            tempViewrect.inset(dx, 0);
            previewChart.setCurrentViewrect(tempViewrect);
        }

        checkboxList.setData(data.getLines(), (list, checked, isFirstLine) -> {

            List<Line> linesToAnimate = new ArrayList<>();

            for (int pos = 0; pos < list.length; pos++) {
                Line line = data.getLines().get(list[pos]);
                line.setActive(checked[pos]);
                linesToAnimate.add(line);
            }

            int activeLines = 0;
            for (Line l : data.getLines()) if (l.isActive()) activeLines++;

            // prevent unchecking all
            checkboxList.setUncheckingEnabled(activeLines > 1);

            chart.onChartDataChange();
            previewChart.onChartDataChange();

            Viewrect current = chart.getCurrentViewrect();
            chart.recalculateMax();
            Viewrect target = new Viewrect(chart.getMaximumViewrect());

            target.left = current.left;
            target.right = current.right;

            chart.setCurrentViewrectAnimatedAdjustingMax(target, linesToAnimate, isFirstLine);
            previewChart.setCurrentViewrectAnimated(target);
        });

        int active = 0;
        for (Line l : data.getLines()) if (l.isActive()) active++;
        checkboxList.setUncheckingEnabled(active > 1);
    }

    public State getState() {
        boolean[] linesChecked = new boolean[data.getLines().size()];
        for (int i = 0; i < data.getLines().size(); i++) linesChecked[i] = data.getLines().get(i).isActive();
        return new State(previewChart.getCurrentViewrect(), linesChecked);
    }

    private void setDatesTextAnimated(String date) {
        dates.setText(date);
    }
}
