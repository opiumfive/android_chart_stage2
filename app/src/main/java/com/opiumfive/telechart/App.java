package com.opiumfive.telechart;

import android.app.Application;

public class App extends Application {

    private static App app;
    private boolean isChangingTheme = false;

    public App() {
        app = this;
    }

    public static boolean isChangingTheme() {
        return app.isChangingTheme;
    }

    public static void setChangingTheme(boolean set) {
        app.isChangingTheme = set;
    }
}
