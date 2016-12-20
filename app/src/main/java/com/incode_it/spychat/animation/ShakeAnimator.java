package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ShakeAnimator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(target,"translationX", 0,10,-10);
        translationX.setRepeatCount(ValueAnimator.INFINITE);
        translationX.setRepeatMode(ValueAnimator.REVERSE);

        getAnimatorAgent().playTogether(translationX);
    }
}
