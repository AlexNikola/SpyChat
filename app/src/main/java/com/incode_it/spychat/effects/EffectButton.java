package com.incode_it.spychat.effects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.amazonaws.auth.policy.Statement;
import com.incode_it.spychat.R;

public class EffectButton extends FrameLayout {

    private ImageView imageView;
    private int effect = EffectsView.EFFECT_NONE;

    public EffectButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imageView = (ImageView) findViewById(R.id.effect_image_view);
    }

    public void setEffect(int effect) {
        this.effect = effect;
        switch (effect) {
            case EffectsView.EFFECT_BALLOON: {
                imageView.setImageResource(R.drawable.ic_balloons);
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.balloon_3));
                break;
            }
            case EffectsView.EFFECT_FIREWORK: {
                imageView.setImageResource(R.drawable.ic_exploding);
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.firework_3));
                break;
            }
            case EffectsView.EFFECT_LOVE: {
                imageView.setImageResource(R.drawable.ic_heart);
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.red));
                break;
            }
            case EffectsView.EFFECT_PARTY: {
                imageView.setImageResource(R.drawable.ic_confetti);
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.confetty_4));
                break;
            }
            default: {
                imageView.setImageResource(R.drawable.ic_arrow_upward_black_24dp);
                imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.chat_effects_btn));
            }
        }

    }


    public int getEffect() {
        return effect;
    }

    static class SavedState extends BaseSavedState {
        int effect;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            effect = (Integer) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(effect);
        }

        @Override
        public String toString() {
            return "CompoundButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " effect=" + effect + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.effect = effect;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setEffect(ss.effect);
        requestLayout();
    }
}
