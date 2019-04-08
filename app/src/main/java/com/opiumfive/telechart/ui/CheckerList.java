package com.opiumfive.telechart.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.model.Line;

import java.util.ArrayList;
import java.util.List;

public class CheckerList extends FrameLayout {

    private FlowLayout flowLayout;
    private List<Checkbox> checkboxes = new ArrayList<>();
    private LineCheckListener lineCheckListener;
    private boolean isUncheckingEnabled = true;
    private boolean isEnabled = true;
    private Animation shakeAnimation;

    private Checkbox.OnCheckedListener onCheckedListener = new Checkbox.OnCheckedListener() {
        @Override
        public void onChecked(Checkbox checkbox, boolean isChecked) {
            if (isEnabled) {
                if (!isChecked && !isUncheckingEnabled) {
                    checkbox.setChecked(true);
                    shakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                    checkbox.startAnimation(shakeAnimation);
                } else {
                    if (lineCheckListener != null) {
                        lineCheckListener.onLineToggle(checkboxes.indexOf(checkbox), isChecked);
                    }
                }
            } else {
                checkbox.setChecked(!isChecked);
            }
        }

        @Override
        public void onLongTap(Checkbox checkbox) {
            for (Checkbox c : checkboxes) {
                if (!c.equals(checkbox)) {
                    c.setChecked(false);
                    if (lineCheckListener != null) {
                        lineCheckListener.onLineToggle(checkboxes.indexOf(c), false);
                    }
                } else {
                    c.setChecked(true);
                    if (lineCheckListener != null) {
                        lineCheckListener.onLineToggle(checkboxes.indexOf(c), true);
                    }
                }
            }
        }
    };

    public CheckerList(Context context) {
        this(context, null, 0);
    }

    public CheckerList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckerList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.checker_list, this);
        flowLayout = findViewById(R.id.flow);
    }

    public void setUncheckingEnabled(boolean uncheckingEnabled) {
        isUncheckingEnabled = uncheckingEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setData(List<Line> list, LineCheckListener listener) {
        lineCheckListener = listener;
        flowLayout.removeAllViews();
        checkboxes.clear();
        for (Line line : list) {
            Checkbox checkbox = new Checkbox(getContext(), line.getTitle(), line.getColor());
            checkbox.setChecked(line.isActive());
            checkbox.setOnCheckedChangeListener(onCheckedListener);
            checkboxes.add(checkbox);
            flowLayout.addView(checkbox);
        }
    }

    interface LineCheckListener {
        void onLineToggle(int pos, boolean checked);
    }
}
