package com.incode_it.spychat.text_effects;

import android.content.Context;

import com.incode_it.spychat.R;
import com.incode_it.spychat.utils.Metric;

import java.io.Serializable;

public class TextStyle implements Serializable {

    private int color = 0xff000000;
    private float size = 16;
    private String font;
    private boolean isAnimated;

    public TextStyle(Context context) {
        size = Metric.pixelsToSp(context, context.getResources().getDimension(R.dimen.chat_text_size));
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    public void refresh(Context context) {
        color = 0xff000000;
        size = Metric.pixelsToSp(context, context.getResources().getDimensionPixelSize(R.dimen.chat_text_size));
        font = null;
        isAnimated = false;
    }
}
