package com.example.photofilter.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofilter.R;
import com.example.photofilter.data.HistoryEntry;

import java.util.ArrayList;
import java.util.List;

/** Horizontal "Recent Photos" strip on the Home screen — read-only, backed by saved-edit history. */
public class RecentPhotoAdapter extends RecyclerView.Adapter<RecentPhotoAdapter.RecentPhotoViewHolder> {

    public interface OnRecentPhotoClickListener {
        void onRecentPhotoClicked(HistoryEntry entry);
    }

    private final List<HistoryEntry> entries = new ArrayList<>();
    private final OnRecentPhotoClickListener listener;

    public RecentPhotoAdapter(OnRecentPhotoClickListener listener) {
        this.listener = listener;
    }

    public void submitEntries(List<HistoryEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_photo, parent, false);
        return new RecentPhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentPhotoViewHolder holder, int position) {
        HistoryEntry entry = entries.get(position);
        holder.label.setText(entry.getFilterName());
        Glide.with(holder.image).load(Uri.parse(entry.getImageUri())).centerCrop().into(holder.image);
        holder.itemView.setOnClickListener(v -> listener.onRecentPhotoClicked(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class RecentPhotoViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView label;

        RecentPhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recentPhotoImage);
            label = itemView.findViewById(R.id.recentPhotoLabel);
        }
    }
}
