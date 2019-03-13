package com.opiumfive.telechart.theming;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.theming.ThemeHolder;

public class ChangeThemeActivity extends AppCompatActivity {

    private static final String KEY_EXTRA_CIRCULAR_REVEAL = "KEY_EXTRA_CIRCULAR_REVEAL";
    private static final String KEY_EXTRA_CIRCULAR_REVEAL_X = "KEY_EXTRA_CIRCULAR_REVEAL_X";
    private static final String KEY_EXTRA_CIRCULAR_REVEAL_Y = "KEY_EXTRA_CIRCULAR_REVEAL_Y ";

    private View mainView = null;
    private Drawable previousWindowBackground = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemeHolder.getCurrentTheme(this));
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        startRevealAnimation();
    }

    protected void changeTheme(int centerX, int centerY) {
        int oldTheme = ThemeHolder.getCurrentTheme(this);
        int newTheme = oldTheme == R.style.LightTheme ? R.style.DarkTheme : R.style.LightTheme;
        ThemeHolder.setCurrentTheme(this, newTheme);
        recreateActivityCircular(centerX, centerY);
    }

    private View getMainView() {
        if (mainView == null) {
            mainView = findViewById(android.R.id.content);
        }
        return mainView;
    }

    private void startRevealAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getIntent().getBooleanExtra(KEY_EXTRA_CIRCULAR_REVEAL, false)) {
            Intent intent = getIntent();
            Window window = getWindow();

            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL, false);

            previousWindowBackground = window.getDecorView().getBackground();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getMainView().setVisibility(View.INVISIBLE);
            ViewTreeObserver viewTreeObserver = getMainView().getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        int screenHeight = getResources().getDisplayMetrics().heightPixels;
                        int centerX = getIntent().getIntExtra(KEY_EXTRA_CIRCULAR_REVEAL_X, screenWidth);
                        int centerY = getIntent().getIntExtra(KEY_EXTRA_CIRCULAR_REVEAL_Y, screenHeight);
                        revealActivity(centerX, centerY);
                        getMainView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }

    private void revealActivity(int centerX, int centerY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            float finalRadius = Math.max(screenWidth, screenHeight) * 1.4f;

            Animator circularReveal = ViewAnimationUtils.createCircularReveal(getMainView(), centerX, centerY, 0f, finalRadius);
            int animDuration = getResources().getInteger(R.integer.change_theme_time);
            circularReveal.setDuration(animDuration);
            circularReveal.setInterpolator(new AccelerateInterpolator());
            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    getWindow().setBackgroundDrawable(previousWindowBackground);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            getMainView().setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }

    private void recreateActivityCircular(int centerX, int centerY) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(this, this.getClass());
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL, true);
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL_X, centerX);
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL_Y, centerY);

            startActivity(intent);
            int animDuration = getResources().getInteger(R.integer.change_theme_time);
            overridePendingTransition(android.R.anim.fade_in, R.anim.fade_out_long);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }, animDuration * 2);
        } else {
            recreate();
        }
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
