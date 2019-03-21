package com.opiumfive.telechart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ViewHolderImpl> {

    private int count = 0;
    private OnChartCheckListener listener;

    public ChartAdapter(int count, OnChartCheckListener listener) {
        this.count = count;
        this.listener = listener;
    }

    @Override
    public ViewHolderImpl onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderImpl(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chart, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(final ViewHolderImpl holder, final int position) {
        holder.bind(holder.getAdapterPosition());
    }

    @Override
    public int getItemCount() {
        return count;
    }

    static class ViewHolderImpl extends RecyclerView.ViewHolder {

        TextView title;

        ViewHolderImpl(View view, final OnChartCheckListener listener) {
            super(view);
            title = view.findViewById(R.id.name);

            title.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChart(getAdapterPosition());
                }
            });
        }

        void bind(int pos) {
            title.setText("Chart #" + String.valueOf(pos + 1));
        }
    }

    public interface OnChartCheckListener {
        void onChart(int position);
    }
}