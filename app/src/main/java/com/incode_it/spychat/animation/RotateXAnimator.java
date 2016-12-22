package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RotateXAnimator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {

        ObjectAnimator rotatingAnimator = ObjectAnimator.ofFloat(target, "rotationX", 0, -50, 50, 0);
        rotatingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotatingAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotatingAnimator.setInterpolator(new LinearInterpolator());

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(target, "alpha", 0, 1);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setRepeatMode(ValueAnimator.RESTART);

        getAnimatorAgent().playTogether(rotatingAnimator);
    }
}
