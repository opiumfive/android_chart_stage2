package com.opiumfive.telechart;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class StatisticsActivity extends ChangeThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
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
