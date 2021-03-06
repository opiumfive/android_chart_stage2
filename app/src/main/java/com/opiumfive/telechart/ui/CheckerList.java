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
                        lineCheckListener.onLineToggle(new int[] {checkboxes.indexOf(checkbox)}, new boolean[] {isChecked});
                    }
                }
            } else {
                checkbox.setChecked(!isChecked);
            }
        }

        @Override
        public void onLongTap(Checkbox checkbox) {
            int[] list = new int[checkboxes.size()];
            boolean[] checked = new boolean[checkboxes.size()];

            boolean someThingChanged = false;

            for (int i = 0; i < checkboxes.size(); i++) {
                Checkbox ch = checkboxes.get(i);
                boolean sameAsClicked = ch.equals(checkbox);
                if (ch.isChecked() != sameAsClicked) {
                    someThingChanged = true;
                }
                ch.setChecked(sameAsClicked);
                list[i] = i;
                checked[i] = sameAsClicked;
            }

            if (lineCheckListener != null && someThingChanged) {
                lineCheckListener.onLineToggle(list, checked);
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
        void onLineToggle(int[] list, boolean[] checked);
    }
}
