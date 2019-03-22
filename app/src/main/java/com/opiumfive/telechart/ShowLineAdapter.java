package com.opiumfive.telechart;

import android.content.Context;

import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.opiumfive.telechart.chart.model.Line;

import java.util.ArrayList;
import java.util.List;

// list view just to reduce apk size
public class ShowLineAdapter extends ArrayAdapter<Line> {

    private LineCheckListener listener;
    private List<Line> lines = new ArrayList<>();

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_show_chart, parent, false);
            viewHolder.checkbox = convertView.findViewById(R.id.checkbox);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Line line = getItem(position);

        viewHolder.checkbox.setChecked(line.isActive());
        viewHolder.checkbox.setText(line.getTitle());

        viewHolder.checkbox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onLineCheck(position);
            }
        }));

        CompoundButtonCompat.setButtonTintList(viewHolder.checkbox, ColorStateList.valueOf(line.getColor()));

        return convertView;
    }

    private static class ViewHolder {
        CheckBox checkbox;
    }

    interface LineCheckListener {
        void onLineCheck(int pos);
    }

}
