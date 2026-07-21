package com.example.photofilter.data;

import com.example.photofilter.domain.filter.Filter;

/** Pairs a {@link Filter} with the display metadata the UI needs to list it. */
public final class FilterItem {

    private final String id;
    private final String displayName;
    private final Filter filter;

    public FilterItem(String id, String displayName, Filter filter) {
        this.id = id;
        this.displayName = displayName;
        this.filter = filter;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Filter getFilter() {
        return filter;
    }
}
