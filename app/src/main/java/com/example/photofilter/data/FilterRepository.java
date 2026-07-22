package com.example.photofilter.data;

import android.content.Context;

import com.example.photofilter.R;
import com.example.photofilter.domain.filter.BlurFilter;
import com.example.photofilter.domain.filter.BrightnessContrastFilter;
import com.example.photofilter.domain.filter.ColorToneFilter;
import com.example.photofilter.domain.filter.FilmFilter;
import com.example.photofilter.domain.filter.GrayscaleFilter;
import com.example.photofilter.domain.filter.MonoFilter;
import com.example.photofilter.domain.filter.NegativeFilter;
import com.example.photofilter.domain.filter.OriginalFilter;
import com.example.photofilter.domain.filter.RetroFilter;
import com.example.photofilter.domain.filter.SepiaFilter;
import com.example.photofilter.domain.filter.VignetteFilter;
import com.example.photofilter.domain.filter.VintageFilter;

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
        items.add(new FilterItem("vintage", context.getString(R.string.filter_vintage), new VintageFilter()));
        items.add(new FilterItem("vignette", context.getString(R.string.filter_vignette), new VignetteFilter()));
        items.add(new FilterItem("blur", context.getString(R.string.filter_blur), new BlurFilter()));
        items.add(new FilterItem("film", context.getString(R.string.filter_film), new FilmFilter()));
        items.add(new FilterItem("mono", context.getString(R.string.filter_mono), new MonoFilter()));
        items.add(new FilterItem("retro", context.getString(R.string.filter_retro), new RetroFilter()));
        return Collections.unmodifiableList(items);
    }
}
