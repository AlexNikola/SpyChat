package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class RotateYAnimator extends BaseViewAnimator {
    @Override
    protected void prepare(View target) {
        ObjectAnimator rotatingAnimator = ObjectAnimator.ofFloat(target, "rotationY", 0, -20, 20, 0);
        rotatingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotatingAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotatingAnimator.setInterpolator(new LinearInterpolator());

        getAnimatorAgent().playTogether(rotatingAnimator);
    }
}
