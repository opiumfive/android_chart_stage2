package com.opiumfive.telechart.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.opiumfive.telechart.R;

import java.util.List;

// list view just to reduce apk size
public class ChartAdapter extends ArrayAdapter<String> {

    public ChartAdapter(Context c, List<String> list) {
        super(c, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_chart, parent, false);
            viewHolder.name = convertView.findViewById(R.id.name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(getItem(position));

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
    }

}