package com.opiumfive.telechart.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.view.MenuItem;
import android.widget.ListView;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.animator.ChartAnimationListener;
import com.opiumfive.telechart.chart.valueFormat.DateValueFormatter;
import com.opiumfive.telechart.chart.animator.ViewrectChangeListener;
import com.opiumfive.telechart.chart.model.Axis;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartView;
import com.opiumfive.telechart.chart.PreviewChartView;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.DataMapper;
import com.opiumfive.telechart.theming.ChangeThemeActivity;

import static com.opiumfive.telechart.Settings.INITIAL_PREVIEW_SCALE;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;

public class StatisticsActivity extends ChangeThemeActivity {

    public static final String CHART_EXTRA_KEY = "chart";
    public static final String LINES_EXTRA_KEY = "lines";
    public static final String VIEWRECT_EXTRA_KEY = "rect";

    private ChartView chart;
    private PreviewChartView previewChart;
    private ListView checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    private ShowLineAdapter showLineAdapter;
    private ChartData chartData;
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

            chart.postDrawIfNeeded();
            chart.toggleAxisAnim();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);
        checkboxList = findViewById(R.id.checkboxList);

        Viewrect savedViewrect = null;
        boolean[] linesState = null;

        // try to restore state
        if (savedInstanceState == null) {
            chartData = getIntent().getParcelableExtra(CHART_EXTRA_KEY);
            savedViewrect = getIntent().getParcelableExtra(VIEWRECT_EXTRA_KEY);
            linesState = getIntent().getBooleanArrayExtra(LINES_EXTRA_KEY);
        } else {
            chartData = savedInstanceState.getParcelable(CHART_EXTRA_KEY);
            savedViewrect = savedInstanceState.getParcelable(VIEWRECT_EXTRA_KEY);
            linesState = savedInstanceState.getBooleanArray(LINES_EXTRA_KEY);
        }

        inflateChart(chartData, savedViewrect, linesState);
    }

    private void inflateChart(@Nullable ChartData chartData, Viewrect savedViewrect, boolean[] linesState) {
        data = DataMapper.mapFromPlainData(chartData);
        data.setAxisYLeft(
            new Axis()
                .setHasLines(true)
                .setLineColor(getColorFromAttr(this, R.attr.dividerColor))
                .setTextColor(getColorFromAttr(this, R.attr.labelColor))
        );
        data.setAxisXBottom(
            new Axis()
                .setHasLines(false)
                .setFormatter(new DateValueFormatter())
                .setInside(false)
                .setTextColor(getColorFromAttr(this, R.attr.labelColor))
        );

        if (linesState != null) {
            for (int i = 0; i < data.getLines().size(); i++) {
                Line line = data.getLines().get(i);
                line.setActive(linesState[i]);
                if (!linesState[i]) {
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
        previewChart.setPreviewColor(getColorFromAttr(this, R.attr.previewFrameColor));
        previewChart.setPreviewBackgroundColor(getColorFromAttr(this, R.attr.previewBackColor));

        // for y-animation trigger
        previewChart.setOnUpTouchListener(() -> {
            chart.toggleAxisAnim();
            chart.postDrawIfNeeded();
        });

        if (savedViewrect == null) {
            Viewrect tempViewrect = new Viewrect(chart.getMaximumViewrect());
            float dx = tempViewrect.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
            tempViewrect.inset(dx, 0);
            previewChart.setCurrentViewrect(tempViewrect);
        } else {
            previewChart.setCurrentViewrect(savedViewrect);
        }

        showLineAdapter = new ShowLineAdapter(this, data.getLines(), pos -> {
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
        checkboxList.setAdapter(showLineAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backToChoosing();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        backToChoosing();
    }

    private void backToChoosing() {
        startActivity(new Intent(this, ChooseActivity.class));
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    @Override
    public Bundle getDataForSaveState() {
        Bundle bundle = new Bundle();
        saveState(bundle);
        return bundle;
    }

    private void saveState(Bundle bundle) {
        bundle.putParcelable(CHART_EXTRA_KEY, chartData);
        Viewrect rect = new Viewrect(previewChart.getCurrentViewrect());
        bundle.putParcelable(VIEWRECT_EXTRA_KEY, rect);
        boolean[] linesChecked = new boolean[data.getLines().size()];
        for (int i = 0; i < data.getLines().size(); i++) linesChecked[i] = data.getLines().get(i).isActive();
        bundle.putBooleanArray(LINES_EXTRA_KEY, linesChecked);
    }
}
