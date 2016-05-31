package com.incode_it.spychat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewDigital extends TextView {

    public TextViewDigital(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FontText);

        if (typedArray != null)
        {
            int n = typedArray.getIndexCount();
            for(int i = 0; i < n; i++)
            {
                int attr = typedArray.getIndex(i);
                if (attr == R.styleable.FontText_typefaceAsset)
                {
                    String fontAsset = typedArray.getString(attr);
                    if (fontAsset != null)
                    {
                        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), fontAsset);
                        setTypeface(typeface);
                    }
                }
            }

            typedArray.recycle();
        }

    }
}
