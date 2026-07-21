package com.example.photofilter.data;

import android.content.Context;

import com.example.photofilter.R;
import com.example.photofilter.domain.filter.BrightnessContrastFilter;
import com.example.photofilter.domain.filter.ColorToneFilter;
import com.example.photofilter.domain.filter.GrayscaleFilter;
import com.example.photofilter.domain.filter.NegativeFilter;
import com.example.photofilter.domain.filter.OriginalFilter;
import com.example.photofilter.domain.filter.SepiaFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for which filters the app offers. Adding a new
 * filter preset only requires one new line here (Factory / registry pattern) —
 * no other layer needs to change.
 */
public class FilterRepository {

    public List<FilterItem> getAvailableFilters(Context context) {
        List<FilterItem> items = new ArrayList<>();
        items.add(new FilterItem("original", context.getString(R.string.filter_original), new OriginalFilter()));
        items.add(new FilterItem("bw", context.getString(R.string.filter_bw), new GrayscaleFilter()));
        items.add(new FilterItem("negative", context.getString(R.string.filter_negative), new NegativeFilter()));
        items.add(new FilterItem("sepia", context.getString(R.string.filter_sepia), new SepiaFilter()));
        items.add(new FilterItem("warm", context.getString(R.string.filter_warm), new ColorToneFilter(ColorToneFilter.Tone.WARM)));
        items.add(new FilterItem("cool", context.getString(R.string.filter_cool), new ColorToneFilter(ColorToneFilter.Tone.COOL)));
        items.add(new FilterItem("bright", context.getString(R.string.filter_bright), new BrightnessContrastFilter(20f, 1.15f)));
        return Collections.unmodifiableList(items);
    }
}
