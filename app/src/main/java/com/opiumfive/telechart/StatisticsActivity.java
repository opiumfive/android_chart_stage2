package com.opiumfive.telechart;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

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

    private ChartView chart;
    private PreviewChartView previewChart;
    private RecyclerView checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    private ShowLineAdapter showLineAdapter;
    private ChartData chartData;

    private ShowLineAdapter.OnLineCheckListener onLineCheckListener = new ShowLineAdapter.OnLineCheckListener() {
        @Override
        public void onLineToggle(int position) {
            Line line = data.getLines().get(position);

            int activeLines = 0;
            for (Line l : data.getLines()) if (l.isActive()) activeLines++;

            // prevent unchecking all
            if (!line.isActive() || line.isActive() && activeLines > 1) {
                line.setActive(!line.isActive());

                chart.onChartDataChange();
                previewChart.onChartDataChange();

                Viewrect current = chart.getCurrentViewrect();
                chart.recalculateMax();
                Viewrect target = new Viewrect(chart.getMaximumViewrect());

                target.left = current.left;
                target.right = current.right;

                //chart.setMaximumViewrect(target);

                chart.setCurrentViewrectAnimated(target, line);
            } else {
                checkboxList.post(() -> showLineAdapter.recheck(position));
            }
        }
    };

    private ViewrectChangeListener previewRectListener = new ViewrectChangeListener() {
        @Override
        public void onViewportChanged(Viewrect newViewrect) {
            chart.setCurrentViewrectAdjustingRect(newViewrect);
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

        checkboxList.setLayoutManager(new LinearLayoutManager(this));
        checkboxList.setHasFixedSize(true);
        checkboxList.addItemDecoration(new ListDividerDecorator(this, getResources().getDimensionPixelSize(R.dimen.divider_margin)));

        if (savedInstanceState == null) {
            chartData = getIntent().getParcelableExtra(CHART_EXTRA_KEY);
        } else {
            chartData = savedInstanceState.getParcelable(CHART_EXTRA_KEY);
        }

        inflateChart(chartData);
    }

    private void inflateChart(@Nullable ChartData chartData) {
        data = DataMapper.mapFromPlainData(chartData);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
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

        previewData = new LineChartData(data);
        previewData.setAxisYLeft(null);
        previewData.setAxisXBottom(null);

        chart.setChartData(data);
        chart.setScrollEnabled(false);

        chart.setViewportCalculationEnabled(false);
        chart.setValueTouchEnabled(true);

        previewChart.setChartData(previewData);
        previewChart.setViewportChangeListener(previewRectListener);
        previewChart.setViewportCalculationEnabled(false);
        previewChart.setPreviewColor(getColorFromAttr(this, R.attr.previewFrameColor));
        previewChart.setPreviewBackgroundColor(getColorFromAttr(this, R.attr.previewBackColor));

        Viewrect tempViewrect = new Viewrect(chart.getMaximumViewrect());
        float dx = tempViewrect.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
        tempViewrect.inset(dx, 0);

        previewChart.setCurrentViewrect(tempViewrect);

        showLineAdapter = new ShowLineAdapter(data.getLines(), onLineCheckListener);
        checkboxList.setAdapter(showLineAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
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
    }
}
