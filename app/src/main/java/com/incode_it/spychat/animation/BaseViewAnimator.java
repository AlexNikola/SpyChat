package com.incode_it.spychat.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.view.ViewHelper;

public abstract class BaseViewAnimator {

    public static final long DURATION_DEFAULT = 500;

    private AnimatorSet animatorSet = new AnimatorSet();
    private long duration = DURATION_DEFAULT;

    protected abstract void prepare(View target);

    public void animate(View target) {
        reset(target);
        prepare(target);
        start();
    }

    /**
     * reset the view to default status
     *
     * @param target
     */
    public void reset(View target) {
        ViewHelper.setAlpha(target, 1);
        ViewHelper.setScaleX(target, 1);
        ViewHelper.setScaleY(target, 1);
        ViewHelper.setTranslationX(target, 0);
        ViewHelper.setTranslationY(target, 0);
        ViewHelper.setRotation(target, 0);
        ViewHelper.setRotationY(target, 0);
        ViewHelper.setRotationX(target, 0);
        ViewHelper.setPivotX(target, target.getMeasuredWidth() / 2.0f);
        ViewHelper.setPivotY(target, target.getMeasuredHeight() / 2.0f);
    }

    /**
     * start to animate
     */
    public void start() {
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    public BaseViewAnimator setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public BaseViewAnimator setStartDelay(long delay) {
        getAnimatorAgent().setStartDelay(delay);
        return this;
    }

    public long getStartDelay() {
        return animatorSet.getStartDelay();
    }

    public BaseViewAnimator addAnimatorListener(Animator.AnimatorListener l) {
        animatorSet.addListener(l);
        return this;
    }

    public void cancel(){
        animatorSet.cancel();
    }

    public boolean isRunning(){
        return animatorSet.isRunning();
    }

    public boolean isStarted(){
        return animatorSet.isStarted();
    }

    public void removeAnimatorListener(Animator.AnimatorListener l) {
        animatorSet.removeListener(l);
    }

    public void removeAllListener() {
        animatorSet.removeAllListeners();
    }

    public BaseViewAnimator setInterpolator(Interpolator interpolator) {
        animatorSet.setInterpolator(interpolator);
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public AnimatorSet getAnimatorAgent() {
        return animatorSet;
    }

}