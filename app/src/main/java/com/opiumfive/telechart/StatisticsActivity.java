package com.opiumfive.telechart;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.opiumfive.telechart.chart.valueFormat.DateValueFormatter;
import com.opiumfive.telechart.chart.listener.ViewrectChangeListener;
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
    private List<ChartData> chartDataList;
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

        Intent intent = getIntent();
        boolean isRecreatedByThemeChange = intent.hasExtra(KEY_EXTRA_CIRCULAR_REVEAL);

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);
        checkboxList = findViewById(R.id.checkboxList);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        checkboxList.setLayoutManager(new LinearLayoutManager(this));
        checkboxList.setHasFixedSize(true);
        checkboxList.addItemDecoration(new ListDividerDecorator(this, getResources().getDimensionPixelSize(R.dimen.divider_margin)));

        if (isRecreatedByThemeChange) {

            //TODO from save state
            chooseChart(0);
        } else {
            showShowChartDialog();
            //chooseChart(2);
        }
    }

    private void showShowChartDialog() {
        String[] chartNames = new String[chartDataList.size()];
        for (int i = 0; i < chartNames.length; i++) {
            chartNames[i] = String.valueOf(i);
        }
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.chart_number)
                .setSingleChoiceItems(chartNames, -1, (dialog, which) -> {
                    dialog.dismiss();
                    chooseChart(which);
                })
                .setOnCancelListener(d -> chooseChart(0))
                .show();
    }

    private void chooseChart(int pos) {
        ChartData chartData = chartDataList.get(pos);
        inflateChart(chartData);
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
}
