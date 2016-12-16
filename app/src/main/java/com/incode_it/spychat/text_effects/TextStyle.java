package com.incode_it.spychat.text_effects;

import android.widget.TextView;

import java.io.Serializable;

public class TextStyle implements Serializable {

    private int color = 0x000000;
    private float size = 16;
    private String font;
    private boolean isAnimated;

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

    public void setStyle(TextView textView) {
        textView.setTextColor(color);
        textView.setTextSize(size);

    }
}
