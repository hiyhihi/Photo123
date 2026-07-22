package com.example.photofilter.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofilter.R;
import com.example.photofilter.data.HistoryEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Read-only list of past saves. No filter/business logic — just formatting. */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private final List<HistoryEntry> entries = new ArrayList<>();

    public void submitEntries(List<HistoryEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_entry, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryEntry entry = entries.get(position);
        holder.filterName.setText(entry.getFilterName());
        holder.timestamp.setText(DATE_FORMAT.format(new Date(entry.getTimestampMillis())));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final TextView filterName;
        final TextView timestamp;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            filterName = itemView.findViewById(R.id.historyFilterName);
            timestamp = itemView.findViewById(R.id.historyTimestamp);
        }
    }
}
