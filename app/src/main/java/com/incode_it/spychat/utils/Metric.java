package com.incode_it.spychat.utils;

import android.content.Context;

public class Metric {
    public static float pixelsToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px/scaledDensity;
    }
}
