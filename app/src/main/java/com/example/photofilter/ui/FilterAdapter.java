package com.example.photofilter.ui;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.photofilter.R;
import com.example.photofilter.data.FilterItem;
import com.example.photofilter.presenter.FilterThumbnail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Horizontal strip of filter previews. Pure View — no filter logic lives here. */
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    public interface OnFilterClickListener {
        void onFilterClicked(FilterItem filterItem);
    }

    private final OnFilterClickListener listener;
    private final List<FilterItem> items = new ArrayList<>();
    private final Map<String, Bitmap> thumbnailsById = new HashMap<>();
    private String selectedId;

    public FilterAdapter(OnFilterClickListener listener) {
        this.listener = listener;
    }

    public void submitFilterList(List<FilterItem> filterItems) {
        items.clear();
        items.addAll(filterItems);
        notifyDataSetChanged();
    }

    public void submitThumbnails(List<FilterThumbnail> thumbnails) {
        thumbnailsById.clear();
        for (FilterThumbnail thumbnail : thumbnails) {
            thumbnailsById.put(thumbnail.getFilterItem().getId(), thumbnail.getThumbnail());
        }
        notifyDataSetChanged();
    }

    public void setSelectedId(String filterId) {
        selectedId = filterId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter_thumbnail, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        FilterItem item = items.get(position);
        holder.nameText.setText(item.getDisplayName());
        holder.itemView.setSelected(item.getId().equals(selectedId));

        Bitmap thumbnail = thumbnailsById.get(item.getId());
        if (thumbnail != null) {
            Glide.with(holder.thumbnailImage).load(thumbnail).centerCrop().into(holder.thumbnailImage);
        } else {
            Glide.with(holder.thumbnailImage).clear(holder.thumbnailImage);
            holder.thumbnailImage.setImageDrawable(null);
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedId(item.getId());
            listener.onFilterClicked(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnailImage;
        final TextView nameText;

        FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.filterThumbnailImage);
            nameText = itemView.findViewById(R.id.filterNameText);
        }
    }
}
