package com.incode_it.spychat.animation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class BounceInAnimator extends BaseViewAnimator {
    @Override
    public void prepare(View target) {

        ObjectAnimator translationY = ObjectAnimator.ofFloat(target,"translationY", 0,10,-10);
        translationY.setRepeatCount(ValueAnimator.INFINITE);
        translationY.setRepeatMode(ValueAnimator.REVERSE);

        getAnimatorAgent().playTogether(translationY);

    }
}
