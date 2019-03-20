package com.opiumfive.telechart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.theming.ThemeHolder;

import java.util.List;

public class ChooseActivity extends Activity {

    private List<ChartData> chartDataList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeHolder.getCurrentTheme(this));
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_choose);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        RecyclerView list = findViewById(R.id.list);
        list.addItemDecoration(new ListDividerDecorator(this, 0));
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        ChartAdapter chartAdapter = new ChartAdapter(chartDataList.size(),
                (pos) -> {
                       startActivity(new Intent(ChooseActivity.this, StatisticsActivity.class).putExtra("chart", chartDataList.get(pos)));
                       finish();
                }
        );
        list.setAdapter(chartAdapter);
    }
}
