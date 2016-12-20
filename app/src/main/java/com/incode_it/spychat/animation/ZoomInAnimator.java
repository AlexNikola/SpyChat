package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class ZoomInAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {

        ObjectAnimator alpha = ObjectAnimator.ofFloat(target,"alpha", 1f,0.5f);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target,"scaleX",1f,0.7f);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target,"scaleY",1f,0.7f);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        getAnimatorAgent().playTogether(alpha, scaleX, scaleY);
    }
}
