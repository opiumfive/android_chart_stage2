package com.opiumfive.telechart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;

import com.opiumfive.telechart.chart.formatter.SimpleAxisValueFormatter;
import com.opiumfive.telechart.chart.listener.DummyLineChartOnValueSelectListener;
import com.opiumfive.telechart.chart.listener.ViewportChangeListener;
import com.opiumfive.telechart.chart.model.Axis;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.LineChartView;
import com.opiumfive.telechart.chart.PreviewLineChartView;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.data.ColumnData;
import com.opiumfive.telechart.theming.ChangeThemeActivity;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends ChangeThemeActivity {

    private static final float INITIAL_PREVIEW_SCALE = 0.3f;

    private LineChartView chart;
    private PreviewLineChartView previewChart;
    private RecyclerView checkboxList;
    private LineChartData data;
    private LineChartData previewData;
    private List<ChartData> chartDataList;

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
                .setSingleChoiceItems(chartNames, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        chooseChart(which);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        chooseChart(0);
                    }
                }).show();
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

        ShowLineAdapter showLineAdapter = new ShowLineAdapter(lines, new ShowLineAdapter.OnLineCheckListener() {
            @Override
            public void onLineToggle(int position) {
                Line line = lines.get(position);
                line.setActive(!line.isActive());

                chart.onChartDataChange();
                previewChart.onChartDataChange();

                Viewport tempViewport = new Viewport(chart.getMaximumViewport());
                float dx = tempViewport.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
                tempViewport.inset(dx, 0);

                previewChart.setCurrentViewportWithAnimation(tempViewport);
            }
        });
        checkboxList.setAdapter(showLineAdapter);

        data = new LineChartData(lines);
        data.setBaseValue(Float.NEGATIVE_INFINITY);
        data.setAxisYLeft(
            new Axis()
                .setHasLines(true)
                .setHasSeparationLine(false)
                .setLineColor(getColorFromAttr(this, R.attr.dividerColor))
                .setTextColor(getColorFromAttr(this, R.attr.labelColor))
        );
        data.setAxisXBottom(
            new Axis()
                .setFormatter(new SimpleAxisValueFormatter().setAppendedText("km".toCharArray()))
                .setInside(false)
                .setTextColor(getColorFromAttr(this, R.attr.labelColor))
        );

        previewData = new LineChartData(data);
        previewData.setAxisYLeft(null);
        previewData.setAxisXBottom(null);

        chart.setLineChartData(data);
        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);
        chart.setValueSelectionEnabled(true);
        chart.setValueTouchEnabled(true);
        chart.setOnValueTouchListener(new DummyLineChartOnValueSelectListener());


        previewChart.setLineChartData(previewData);
        previewChart.setViewportChangeListener(new ViewportListener());
        previewChart.setZoomEnabled(false);
        previewChart.setViewportCalculationEnabled(false);
        previewChart.setPreviewColor(getColorFromAttr(this, R.attr.previewFrameColor));
        previewChart.setPreviewBackgroundColor(getColorFromAttr(this, R.attr.previewBackColor));

        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() * (1f - INITIAL_PREVIEW_SCALE) / 2;
        tempViewport.inset(dx, 0);

        previewChart.setCurrentViewport(tempViewport);
    }

    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            chart.setCurrentViewport(newViewport);
        }

    }

    //TODO to utils
    @ColorInt
    private static int getColorFromAttr(Context context, @AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }
}
