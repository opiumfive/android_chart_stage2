package com.opiumfive.telechart.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.theming.ThemeHolder;

import java.util.ArrayList;
import java.util.List;

import static com.opiumfive.telechart.ui.StatisticsActivity.CHART_EXTRA_KEY;

public class ChooseActivity extends Activity {

    private List<ChartData> chartDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeHolder.getCurrentTheme(this));
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_choose);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        List<String> charts = new ArrayList<>(chartDataList.size());
        for (int i = 1; i <= chartDataList.size(); i++) charts.add("Chart #" + i);
        ListView list = findViewById(R.id.list);
        ChartAdapter chartAdapter = new ChartAdapter(this, charts);

        list.setOnItemClickListener(((parent, view, pos, id) -> {
            Intent intent = new Intent(ChooseActivity.this, StatisticsActivity.class).putExtra(CHART_EXTRA_KEY, chartDataList.get(pos));
            startActivity(intent);
            finish();
        }));

        list.setAdapter(chartAdapter);
    }
}
