package com.incode_it.spychat.effects;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.incode_it.spychat.R;
import com.incode_it.spychat.animation.BaseViewAnimator;
import com.incode_it.spychat.animation.BlinkAnimator;
import com.incode_it.spychat.animation.BounceInAnimator;
import com.incode_it.spychat.animation.RotateXAnimator;
import com.incode_it.spychat.animation.RotateYAnimator;
import com.incode_it.spychat.animation.ShakeAnimator;
import com.incode_it.spychat.utils.FontHelper;
import com.incode_it.spychat.utils.Metric;

import java.io.Serializable;

public class TextStyle implements Serializable {

    public static final String ANIMATION_TYPE = "ANIMATION_TYPE";
    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_BLINK = 1;
    public static final int ANIMATION_BOUNCE = 2;
    public static final int ANIMATION_SHAKE = 3;
    public static final int ANIMATION_ROTATION_X = 4;
    public static final int ANIMATION_ROTATION_Y = 5;


    private transient BaseViewAnimator animator;


    private int color = 0xff000000;
    private float size = 16;
    private String font;
    private int animationType = ANIMATION_NONE;

    public TextStyle(Context context) {
        size = Metric.pixelsToSp(context, context.getResources().getDimension(R.dimen.chat_text_size));
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getAnimationType() {
        return animationType;
    }

    public void setAnimationType(int animationType) {
        this.animationType = animationType;
    }

    public void refresh(Context context, View view) {
        color = 0xff000000;
        size = Metric.pixelsToSp(context, context.getResources().getDimensionPixelSize(R.dimen.chat_text_size));
        font = null;
        animationType = ANIMATION_NONE;

        if (animator != null) {
            animator.cancel();
            animator.reset(view);
            animator = null;
        }
    }

    public void animate(View view) {
        animate(view, animationType);
    }

    public void animate(final View view, final int animationType) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Log.d("chatm", "animate: " + animationType);
                TextStyle.this.animationType = animationType;

                if (animator != null) {
                    animator.cancel();
                    animator.reset(view);
                    animator = null;
                }

                switch (animationType) {
                    case ANIMATION_BLINK: {
                        animator = new BlinkAnimator();
                        break;
                    }
                    case ANIMATION_BOUNCE: {
                        animator = new BounceInAnimator();
                        break;
                    }
                    case ANIMATION_SHAKE: {
                        animator = new ShakeAnimator();
                        break;
                    }
                    case ANIMATION_ROTATION_X: {
                        animator = new RotateXAnimator();
                        animator.setDuration(3000);
                        break;
                    }
                    case ANIMATION_ROTATION_Y: {
                        animator = new RotateYAnimator();
                        animator.setDuration(3000);
                        break;
                    }
                }

                if (animator != null) {
                    animator.animate(view);
                }
            }
        });

    }

    public void setStyle(final TextView view) {
        view.setTextColor(getColor());
        view.setTextSize(getSize());
        FontHelper.setCustomFont(view.getContext(), view, getFont());
        animate(view);
    }

    @Override
    public String toString() {
        return "TextStyle{" +
                "color=" + color +
                ", size=" + size +
                ", font='" + font + '\'' +
                ", animationType=" + animationType +
                '}';
    }
}
