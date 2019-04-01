package com.opiumfive.telechart.ui;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.theming.ChangeThemeActivity;
import java.util.List;

public class StatisticsActivity extends ChangeThemeActivity {

    public static final String CHART_EXTRA_KEY = "chart";
    public static final String LINES_EXTRA_KEY = "lines";
    public static final String VIEWRECT_EXTRA_KEY = "rect";

    private List<ChartData> chartDataList;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        container = findViewById(R.id.container);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        for (ChartData chartData: chartDataList) {
            ChartWithPreview chartWithPreview = new ChartWithPreview(this, chartData);
            container.addView(chartWithPreview);
        }
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
        /*bundle.putParcelable(CHART_EXTRA_KEY, chartData);
        Viewrect rect = new Viewrect(previewChart.getCurrentViewrect());
        bundle.putParcelable(VIEWRECT_EXTRA_KEY, rect);
        boolean[] linesChecked = new boolean[data.getLines().size()];
        for (int i = 0; i < data.getLines().size(); i++) linesChecked[i] = data.getLines().get(i).isActive();
        bundle.putBooleanArray(LINES_EXTRA_KEY, linesChecked);*/
    }
}
