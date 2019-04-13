package com.opiumfive.telechart.ui;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.CType;
import com.opiumfive.telechart.data.ChartData;
import com.opiumfive.telechart.data.ChartDataParser;
import com.opiumfive.telechart.data.State;
import com.opiumfive.telechart.theming.ChangeThemeActivity;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends ChangeThemeActivity {

    public static final String CHARTS_STATE_EXTRA_KEY = "state";
    public static final String SCROLL_STATE_EXTRA_KEY = "scroll_state";

    private List<ChartData> chartDataList;
    private LinearLayout container;
    private ChartWithPreview[] chartViews;
    private SlopScrollView slopScrollView;

    private  int[] scrollPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        container = findViewById(R.id.container);
        slopScrollView = findViewById(R.id.scrollContainer);

        chartDataList = ChartDataParser.loadAndParseInput(this);

        List<State> states = null;

        // try to restore state
        if (savedInstanceState == null) {
            states = getIntent().getParcelableArrayListExtra(CHARTS_STATE_EXTRA_KEY);
            scrollPosition = getIntent().getIntArrayExtra(SCROLL_STATE_EXTRA_KEY);
        } else {
            states = savedInstanceState.getParcelableArrayList(CHARTS_STATE_EXTRA_KEY);
            scrollPosition = savedInstanceState.getIntArray(SCROLL_STATE_EXTRA_KEY);
        }

        chartViews = new ChartWithPreview[chartDataList.size()];

        for (int i = 0; i < chartDataList.size(); i++) {
            State state = null;
            if (states != null && states.size() > i) {
                state = states.get(i);
            }

            ChartData chartData = chartDataList.get(i);

            CType cType = CType.LINE;
            String name = "";
            switch (i) {
                case 0:
                    cType = CType.LINE;
                    name = "Followers";
                    break;
                case 1:
                    cType = CType.LINE_2Y;
                    name = "Interactions";
                    break;
                case 2:
                    cType = CType.STACKED_BAR;
                    name = "Fruits";
                    break;
                case 3:
                    cType = CType.DAILY_BAR;
                    name = "Views";
                    break;
                case 4:
                    cType = CType.AREA;
                    name = "Fruits + Bonus";
                    break;
            }

            ChartWithPreview chartWithPreview = new ChartWithPreview(this, chartData, name, state, cType);


            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin));
            chartWithPreview.setLayoutParams(layoutParams);
            chartViews[i] = chartWithPreview;
            container.addView(chartWithPreview);
        }

        if (scrollPosition != null && scrollPosition.length > 1) {
            slopScrollView.post(() -> slopScrollView.scrollTo(scrollPosition[0], scrollPosition[1]));
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
        bundle.putParcelableArrayList(CHARTS_STATE_EXTRA_KEY, states);
        bundle.putIntArray(SCROLL_STATE_EXTRA_KEY, new int[] { slopScrollView.getScrollX(), slopScrollView.getScrollY()});
    }
}
