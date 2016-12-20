package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class BlinkAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {

        ObjectAnimator alpha = ObjectAnimator.ofFloat(target,"alpha", 1,0.1f);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);

        getAnimatorAgent().playTogether(alpha);

    }
}