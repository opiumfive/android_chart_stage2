package com.opiumfive.telechart;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHolder {

    private static final String KEY_THEME = "key_theme";
    private static final String PREFS_NAME = "theme_prefs";

    public static int getCurrentTheme(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPref.getInt(KEY_THEME, R.style.LightTheme);
    }

    public static void setCurrentTheme(Context context, int theme) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPref.edit().putInt(KEY_THEME, theme).apply();
    }
}
