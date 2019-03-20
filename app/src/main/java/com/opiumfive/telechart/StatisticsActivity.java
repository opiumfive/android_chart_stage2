package com.opiumfive.telechart;

import android.content.Intent;
import android.graphics.Color;
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
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.Viewrect;
import com.opiumfive.telechart.chart.ChartView;
import com.opiumfive.telechart.chart.PreviewChartView;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.data.ColumnData;
import com.opiumfive.telechart.theming.ChangeThemeActivity;

import java.util.ArrayList;
import java.util.List;

import static com.opiumfive.telechart.Settings.INITIAL_PREVIEW_SCALE;
import static com.opiumfive.telechart.chart.Util.getColorFromAttr;

public class StatisticsActivity extends ChangeThemeActivity {

    private ChartView chart;
    private PreviewChartView previewChart;
    private RecyclerView checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    //private List<ChartData> chartDataList;
    private ShowLineAdapter showLineAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        boolean isRecreatedByThemeChange = intent.hasExtra(KEY_EXTRA_CIRCULAR_REVEAL);

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);
        checkboxList = findViewById(R.id.checkboxList);

        checkboxList.setLayoutManager(new LinearLayoutManager(this));
        checkboxList.setHasFixedSize(true);
        checkboxList.addItemDecoration(new ListDividerDecorator(this, getResources().getDimensionPixelSize(R.dimen.divider_margin)));

        if (isRecreatedByThemeChange) {

            //TODO from save state
            ChartData chartData = getIntent().getParcelableExtra("chart");
            inflateChart(chartData);
        } else {
            //showShowChartDialog();
            ChartData chartData = getIntent().getParcelableExtra("chart");
            inflateChart(chartData);
        }
    }

    private void inflateChart(@Nullable ChartData chartData) {
        if (chartData == null) return;

        List<List<PointValue>> plotsValuesList = new ArrayList<>(chartData.getColumns().size() - 1);

        ColumnData xValuesData = chartData.getColumns().get(0);
        for (int columnIndex = 1; columnIndex < chartData.getColumns().size(); columnIndex++) {
            ColumnData yValuesData = chartData.getColumns().get(columnIndex);
            List<PointValue> values = new ArrayList<>(yValuesData.getList().size());

            for (int i = 0; i < chartData.getColumns().get(0).getList().size(); i++) {
                values.add(new PointValue(xValuesData.getList().get(i), yValuesData.getList().get(i)));
            }

            plotsValuesList.add(values);
        }

        final List<Line> lines = new ArrayList<>(chartData.getColumns().size() - 1);
        for (int columnIndex = 1; columnIndex < chartData.getColumns().size(); columnIndex++) {
            Line line = new Line(plotsValuesList.get(columnIndex - 1));
            String lineId = chartData.getColumns().get(columnIndex).getTitle();
            line.setColor(Color.parseColor(chartData.getColors().get(lineId)));
            line.setTitle(chartData.getNames().get(lineId));
            lines.add(line);
        }

        showLineAdapter = new ShowLineAdapter(lines, onLineCheckListener);
        checkboxList.setAdapter(showLineAdapter);

        data = new LineChartData(lines);
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
        previewChart.setViewportChangeListener(new ViewrectListener());
        previewChart.setViewportCalculationEnabled(false);
        previewChart.setPreviewColor(getColorFromAttr(this, R.attr.previewFrameColor));
        previewChart.setPreviewBackgroundColor(getColorFromAttr(this, R.attr.previewBackColor));

        Viewrect tempViewrect = new Viewrect(chart.getMaximumViewrect());
        float dx = tempViewrect.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
        tempViewrect.inset(dx, 0);

        previewChart.setCurrentViewrect(tempViewrect);
    }

    private class ViewrectListener implements ViewrectChangeListener {

        @Override
        public void onViewportChanged(Viewrect newViewrect) {
            chart.setCurrentViewrectAdjustingRect(newViewrect);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            startActivity(new Intent(this, ChooseActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ChooseActivity.class));
        finish();
    }
}
