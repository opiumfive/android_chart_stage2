package com.opiumfive.telechart.ui;

import android.content.Context;

import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.opiumfive.telechart.R;
import com.opiumfive.telechart.chart.model.Line;

import java.util.ArrayList;
import java.util.List;

// list view just to reduce apk size
public class ShowLineAdapter extends ArrayAdapter<Line> {

    private LineCheckListener listener;
    private List<Line> lines = new ArrayList<>();
    private boolean isUncheckingEnabled = true;
    private boolean isEnabled = true;

    public ShowLineAdapter(Context c, List<Line> list, LineCheckListener listener) {
        super(c, 0);
        this.listener = listener;
        lines.addAll(list);
    }

    @Nullable
    @Override
    public Line getItem(int position) {
        return lines.get(position);
    }

    @Override
    public int getCount() {
        return lines.size();
    }

    public void setUncheckingEnabled(boolean uncheckingEnabled) {
        isUncheckingEnabled = uncheckingEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder(listener);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_show_chart, parent, false);
            viewHolder.checkbox = convertView.findViewById(R.id.checkbox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Line line = getItem(position);

        viewHolder.bind(line, position);

        return convertView;
    }

    private class ViewHolder {

        CheckBox checkbox;
        LineCheckListener listener;
        boolean shouldNotif = true;

        public ViewHolder(LineCheckListener listener) {
            this.listener = listener;
        }

        void bind(Line line, int position) {
            shouldNotif = false;
            checkbox.setChecked(line.isActive());
            shouldNotif = true;
            checkbox.setText(line.getTitle());
            CompoundButtonCompat.setButtonTintList(checkbox, ColorStateList.valueOf(line.getColor()));

            checkbox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                if (shouldNotif) {
                    if (isEnabled) {
                        if (!isChecked && !isUncheckingEnabled) {
                            shouldNotif = false;
                            buttonView.setChecked(true);
                            shouldNotif = true;
                        } else {
                            if (listener != null) {
                                listener.onLineCheck(position);
                            }
                        }
                    } else {
                        shouldNotif = false;
                        buttonView.setChecked(!isChecked);
                        shouldNotif = true;
                    }
                }
            }));
        }
    }

    interface LineCheckListener {
        void onLineCheck(int pos);
    }

}
