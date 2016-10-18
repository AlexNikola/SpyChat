package com.incode_it.spychat.utils;


import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class FontHelper {

//    public static void setCustomFont(TextView textview, Context context, String font) {
//
//        setCustomFont(textview, font, context);
//    }

    public static void setCustomFont(Context context, TextView textview, String font) {
        if(font == null) {
            textview.setTypeface(Typeface.DEFAULT);
            return;
        }

        Typeface tf = FontCache.get(context, font);
        if(tf != null) {
            textview.setTypeface(tf);
        }
    }

}