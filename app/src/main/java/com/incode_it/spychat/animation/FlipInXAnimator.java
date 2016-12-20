package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;


public class FlipInXAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {

        /*ObjectAnimator alpha = ObjectAnimator.ofFloat(target, "alpha", 0.25f, 0.5f, 0.75f, 1);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);*/

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(target, "rotationX", 0, 45, 0, -45, 0);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);

        getAnimatorAgent().playTogether(rotationX);
    }
}
