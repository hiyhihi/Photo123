package com.example.photofilter.data;

/** One row of edit history: which filter was used to save a photo, and when. */
public final class HistoryEntry {

    private final long id;
    private final String filterName;
    private final String imageUri;
    private final long timestampMillis;

    public HistoryEntry(long id, String filterName, String imageUri, long timestampMillis) {
        this.id = id;
        this.filterName = filterName;
        this.imageUri = imageUri;
        this.timestampMillis = timestampMillis;
    }

    public long getId() {
        return id;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
