package com.opiumfive.telechart.ui;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.CType;
import com.opiumfive.telechart.chart.LineChartView;
import com.opiumfive.telechart.chart.PreviewLineChartView;
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

import static com.opiumfive.telechart.Settings.INITIAL_PREVIEW_SCALE;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;

public class ChartWithPreview extends LinearLayout {

    private LineChartView chart;
    private PreviewLineChartView previewChart;
    private ListView checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    private ShowLineAdapter showLineAdapter;
    private boolean shouldAnimateRect = false;
    private boolean isAnimatingPreview = true;

    private ViewrectChangeListener previewRectListener = new ViewrectChangeListener() {
        @Override
        public void onViewportChanged(Viewrect newViewrect, float distanceX) {
            if (isAnimatingPreview) {
                chart.setCurrentViewrectAdjustingRect(newViewrect, shouldAnimateRect, distanceX);
                shouldAnimateRect = true;
            }
        }
    };

    private ChartAnimationListener previewAnimListener = new ChartAnimationListener() {
        @Override
        public void onAnimationStarted() {
            isAnimatingPreview = false;
            showLineAdapter.setEnabled(false);
        }

        @Override
        public void onAnimationFinished() {
            isAnimatingPreview = true;
            showLineAdapter.setEnabled(true);

            //chart.postDrawIfNeeded();
            //chart.toggleAxisAnim();
        }
    };

    public ChartWithPreview(Context context, ChartData chartData, State state, CType cType) {
        super(context);
        inflate(context, R.layout.chart_with_preview, this);

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);
        checkboxList = findViewById(R.id.checkboxList);
        chart.setType(cType);
        previewChart.setType(cType);
        if (cType.equals(CType.DAILY_BAR)) {
            checkboxList.setVisibility(GONE);
        }
        setChartData(chartData, state);
    }

    public void setChartData(ChartData chartData, State state) {
        data = DataMapper.mapFromPlainData(chartData);
        data.setAxisYLeft(
                new Axis()
                        .setHasLines(true)
                        .setLineColor(getColorFromAttr(getContext(), R.attr.dividerColor))
                        .setTextColor(getColorFromAttr(getContext(), R.attr.labelColor))
        );
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

        previewData = new LineChartData(data);
        previewData.setAxisYLeft(null);
        previewData.setAxisXBottom(null);

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
            //chart.toggleAxisAnim();
            //chart.postDrawIfNeeded();
        });


        if (state != null && state.getCurrentViewrect() != null) {
            previewChart.setCurrentViewrect(state.getCurrentViewrect());
        } else {
            Viewrect tempViewrect = new Viewrect(chart.getMaximumViewrect());
            float dx = tempViewrect.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
            tempViewrect.inset(dx, 0);
            previewChart.setCurrentViewrect(tempViewrect);
        }

        showLineAdapter = new ShowLineAdapter(getContext(), data.getLines(), pos -> {
            Line line = data.getLines().get(pos);

            line.setActive(!line.isActive());

            int activeLines = 0;
            for (Line l : data.getLines()) if (l.isActive()) activeLines++;

            // prevent unchecking all
            showLineAdapter.setUncheckingEnabled(activeLines > 1);

            chart.onChartDataChange();
            previewChart.onChartDataChange();

            Viewrect current = chart.getCurrentViewrect();
            chart.recalculateMax();
            Viewrect target = new Viewrect(chart.getMaximumViewrect());

            target.left = current.left;
            target.right = current.right;

            chart.setCurrentViewrectAnimatedAdjustingMax(target, line);
            previewChart.setCurrentViewrectAnimated(target);

        });

        int active = 0;
        for (Line l : data.getLines()) if (l.isActive()) active++;
        showLineAdapter.setUncheckingEnabled(active > 1);

        ViewGroup.LayoutParams layoutParams = checkboxList.getLayoutParams();
        layoutParams.height = (int) (getContext().getResources().getDimension(R.dimen.checkbox_height) * data.getLines().size() +
                getContext().getResources().getDimension(R.dimen.divider_height) * (data.getLines().size() - 1));
        checkboxList.setLayoutParams(layoutParams);

        checkboxList.setAdapter(showLineAdapter);
    }

    public State getState() {
        boolean[] linesChecked = new boolean[data.getLines().size()];
        for (int i = 0; i < data.getLines().size(); i++) linesChecked[i] = data.getLines().get(i).isActive();
        return new State(previewChart.getCurrentViewrect(), linesChecked);
    }
}
