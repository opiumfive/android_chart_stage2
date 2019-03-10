package com.opiumfive.telechart;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.opiumfive.telechart.chart.gesture.ZoomType;
import com.opiumfive.telechart.chart.listener.ViewportChangeListener;
import com.opiumfive.telechart.chart.model.Axis;
import com.opiumfive.telechart.chart.model.Line;
import com.opiumfive.telechart.chart.model.LineChartData;
import com.opiumfive.telechart.chart.model.PointValue;
import com.opiumfive.telechart.chart.model.Viewport;
import com.opiumfive.telechart.chart.util.ChartUtils;
import com.opiumfive.telechart.chart.view.LineChartView;
import com.opiumfive.telechart.chart.view.PreviewLineChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends ChangeThemeActivity {

    private static final String CHART_DATA_NAME = "chart_data.json";

    private LineChartView chart;
    private PreviewLineChartView previewChart;
    private LineChartData data;
    private LineChartData previewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        chart = findViewById(R.id.chart);
        previewChart = findViewById(R.id.chart_preview);

        ChartData chartData = getDataFromJson();

        generateDefaultData();

        chart.setLineChartData(data);
        // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
        // zoom/scroll is unnecessary.
        chart.setZoomEnabled(false);
        chart.setScrollEnabled(false);

        previewChart.setLineChartData(previewData);
        previewChart.setViewportChangeListener(new ViewportListener());

        previewX(false);
    }

    private ChartData getDataFromJson() {
        String json = null;
        try {
            InputStream is = getAssets().open(CHART_DATA_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ChartData chartData = new ChartData();

        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray obj = new JSONArray(json);
                JSONObject chart1 = obj.getJSONObject(0);

                JSONArray columns = chart1.getJSONArray("columns");
                JSONObject types = chart1.getJSONObject("types");
                JSONObject names = chart1.getJSONObject("names");
                JSONObject colors = chart1.getJSONObject("colors");

                List<ColumnData> columnsList = new ArrayList<>(columns.length());

                for (int i = 0; i < columns.length(); i++) {
                    JSONArray column = columns.getJSONArray(i);
                    ColumnData columnData = new ColumnData();
                    columnData.setTitle(column.getString(0));
                    List<Long> valuesList = new ArrayList<>(column.length() - 1);
                    for (int j = 1; j < columns.length(); j++) {
                        valuesList.add(column.getLong(j));
                    }
                    columnData.setList(valuesList);
                    columnsList.add(columnData);
                }

                Map<String, String> typesMap = new HashMap<>(types.names().length());
                for(Iterator<String> keys = types.keys(); keys.hasNext();) {
                    String key = keys.next();
                    typesMap.put(key, types.getString(key));
                }

                Map<String, String> namesMap = new HashMap<>(names.names().length());
                for(Iterator<String> keys = names.keys(); keys.hasNext();) {
                    String key = keys.next();
                    namesMap.put(key, names.getString(key));
                }

                Map<String, String> colorsMap = new HashMap<>(colors.names().length());
                for(Iterator<String> keys = colors.keys(); keys.hasNext();) {
                    String key = keys.next();
                    colorsMap.put(key, colors.getString(key));
                }

                chartData.setColors(colorsMap);
                chartData.setNames(namesMap);
                chartData.setTypes(typesMap);
                chartData.setColumns(columnsList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return chartData;
    }



    private void generateDefaultData() {
        int numValues = 50;

        List<PointValue> values = new ArrayList<PointValue>();
        for (int i = 0; i < numValues; ++i) {
            values.add(new PointValue(i, (float) Math.random() * 100f));
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_GREEN);
        line.setHasPoints(false);// too many values so don't draw points.

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        data = new LineChartData(lines);
        data.setAxisXBottom(new Axis());
        data.setAxisYLeft(new Axis().setHasLines(true));

        // prepare preview data, is better to use separate deep copy for preview chart.
        // Set color to grey to make preview area more visible.
        previewData = new LineChartData(data);
        previewData.getLines().get(0).setColor(ChartUtils.DEFAULT_DARKEN_COLOR);
    }

    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart.
            chart.setCurrentViewport(newViewport);
        }

    }

    private void previewX(boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stats_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_theme) {
            View actionView = findViewById(R.id.action_theme);
            int[] location = new int[2];
            actionView.getLocationOnScreen(location);
            changeTheme(location[0], location[1]);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
