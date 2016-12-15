package com.incode_it.spychat.effects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.incode_it.spychat.R;


public class MyRadioGroup extends LinearLayout implements View.OnClickListener {
    public MyRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

    }

    public interface Callback
    {
        void onCheckedChanged(boolean isChecked, int checkedId);
    }
}
