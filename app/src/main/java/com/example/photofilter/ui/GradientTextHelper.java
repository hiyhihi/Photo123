package com.example.photofilter.ui;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.photofilter.R;

/** Paints the HATFilter brand gradient (blue → cyan) onto a TextView's text. */
final class GradientTextHelper {

    private GradientTextHelper() {
    }

    static void applyBrandGradient(TextView textView) {
        textView.post(() -> {
            int width = textView.getPaint().measureText(textView.getText().toString()) > 0
                    ? (int) textView.getPaint().measureText(textView.getText().toString())
                    : textView.getWidth();
            if (width <= 0) {
                return;
            }
            Shader shader = new LinearGradient(
                    0, 0, width, 0,
                    ContextCompat.getColor(textView.getContext(), R.color.accent_blue),
                    ContextCompat.getColor(textView.getContext(), R.color.accent_cyan),
                    Shader.TileMode.CLAMP);
            textView.getPaint().setShader(shader);
            textView.invalidate();
        });
    }
}
