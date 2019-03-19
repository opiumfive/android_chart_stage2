package com.opiumfive.telechart;

import android.content.res.ColorStateList;
import android.support.v4.widget.CompoundButtonCompat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.opiumfive.telechart.chart.model.Line;

import java.util.ArrayList;
import java.util.List;

public class ShowLineAdapter extends RecyclerView.Adapter<ShowLineAdapter.ViewHolderImpl> {

    private List<Line> list = new ArrayList<>();
    private OnLineCheckListener listener;

    public ShowLineAdapter(List<Line> lines, OnLineCheckListener listener) {
        if (lines != null && !lines.isEmpty()) {
            list.addAll(lines);
        }
        this.listener = listener;
    }

    public void recheck(int pos) {
        notifyItemChanged(pos);
    }

    @Override
    public ViewHolderImpl onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderImpl(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show_chart, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolderImpl holder, final int position) {
        holder.bind(getItem(holder.getAdapterPosition()));
    }

    private Line getItem(int position) {
        return list == null ? null : list.get(position);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolderImpl extends RecyclerView.ViewHolder {

        CheckBox checkbox;

        ViewHolderImpl(View view, final OnLineCheckListener listener) {
            super(view);
            checkbox = view.findViewById(R.id.checkbox);

            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onLineToggle(getAdapterPosition());
                }
            });
        }

        void bind(Line line) {
            checkbox.setChecked(line.isActive());
            checkbox.setText(line.getTitle());
            CompoundButtonCompat.setButtonTintList(checkbox, ColorStateList.valueOf(line.getColor()));
        }
    }

    public interface OnLineCheckListener {
        void onLineToggle(int position);
    }
}
