package com.opiumfive.telechart.ui;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.data.State;
import com.opiumfive.telechart.theming.ChangeThemeActivity;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends ChangeThemeActivity {

    public static final String STATE_EXTRA_KEY = "state";

    private List<ChartData> chartDataList;
    private LinearLayout container;
    private ChartWithPreview[] chartViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        container = findViewById(R.id.container);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        List<State> states = null;

        // try to restore state
        if (savedInstanceState == null) {
            states = getIntent().getParcelableArrayListExtra(STATE_EXTRA_KEY);
        } else {
            states = savedInstanceState.getParcelableArrayList(STATE_EXTRA_KEY);
        }

        chartViews = new ChartWithPreview[chartDataList.size()];

        for (int i = 0; i < chartDataList.size(); i++) {
            State state = null;
            if (states != null && states.size() > i) {
                state = states.get(i);
            }

            ChartData chartData = chartDataList.get(i);
            ChartWithPreview chartWithPreview = new ChartWithPreview(this, chartData, state);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin));
            chartWithPreview.setLayoutParams(layoutParams);
            chartViews[i] = chartWithPreview;
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
        ArrayList<State> states = new ArrayList<>(chartViews.length);
        for (int i = 0; i < chartViews.length; i++) {
            states.add(chartViews[i].getState());
        }
        bundle.putParcelableArrayList(STATE_EXTRA_KEY, states);
    }
}
