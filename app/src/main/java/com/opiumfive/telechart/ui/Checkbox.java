package com.opiumfive.telechart.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opiumfive.telechart.R;

public class Checkbox extends LinearLayout implements Checkable {

    private int itemColor;
    private boolean isChecked = true;
    private ImageView icon;
    private TextView text;
    private View root;
    private OnCheckedListener onCheckedChangeListener;

    public Checkbox(Context context, String name, int color) {
        super(context);

        inflate(context, R.layout.checkbox, this);
        icon = findViewById(R.id.icon);
        text = findViewById(R.id.name);
        root = findViewById(R.id.root);
        text.setText(name);
        itemColor = color;
        Drawable background = root.getBackground();
        background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        setOnClickListener((v) -> {
            toggle();
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onChecked(this, isChecked);
            }
        });

        fill();
    }

    private void fill() {
        root.setSelected(isChecked);
        if (isChecked) {
            icon.setVisibility(View.VISIBLE);
            text.setTextColor(Color.WHITE);
        } else {
            icon.setVisibility(View.GONE);
            text.setTextColor(itemColor);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        isChecked = checked;
        fill();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
        fill();
    }

    public void setOnCheckedChangeListener(OnCheckedListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedListener {
        void onChecked(Checkbox checkbox, boolean isChecked);
    }
}
