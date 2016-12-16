package com.incode_it.spychat.text_effects;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.incode_it.spychat.chat.ChatTextFontDialog;
import com.incode_it.spychat.chat.ChatTextSizeDialog;
import com.incode_it.spychat.chat.FragmentChat;
import com.incode_it.spychat.effects.EffectsSelectorFragment;
import com.incode_it.spychat.utils.FontHelper;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

public class TextEffectsFragment extends Fragment implements View.OnClickListener {

    public static final int REQUEST_TEXT_FONT = 1;
    public static final int REQUEST_TEXT_SIZE = 2;


    private TextStyle textStyle;
    private TextView sampleTextView;
    private AnimatorSet animation;
    private boolean isAnimated;

    public TextEffectsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textStyle = new TextStyle();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_effects, container, false);

        sampleTextView = (TextView) view.findViewById(R.id.sample);
        textStyle.setStyle(sampleTextView;

        view.findViewById(R.id.change_color).setOnClickListener(this);
        view.findViewById(R.id.change_font).setOnClickListener(this);
        view.findViewById(R.id.change_size).setOnClickListener(this);
        view.findViewById(R.id.change_blink).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_color:
                openColorPicker();
                break;
            case R.id.change_font:
                openFontPicker();
                break;
            case R.id.change_size:
                openSizePicker();
                break;
            case R.id.change_blink:
                animate();
                break;
        }
    }

    private void openColorPicker() {
        int[] mColors = getResources().getIntArray(R.array.default_rainbow);

        ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.pick_color_dialog_title,
                mColors,
                textStyle.getColor(),
                5,
                ColorPickerDialog.SIZE_SMALL);

        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                textStyle.setColor(color);
                sampleTextView.setTextColor(color);
            }
        });

        dialog.show(getActivity().getFragmentManager(), "color_dialog_test");
    }

    private void openFontPicker() {
        ChatTextFontDialog dialog = new ChatTextFontDialog();
        dialog.setTargetFragment(TextEffectsFragment.this, REQUEST_TEXT_FONT);
        dialog.show(getActivity().getSupportFragmentManager(), "change_text_font_dialog");
    }

    private void openSizePicker() {
        ChatTextSizeDialog dialog = new ChatTextSizeDialog();
        dialog.setTargetFragment(TextEffectsFragment.this, REQUEST_TEXT_SIZE);
        dialog.show(getActivity().getSupportFragmentManager(), "change_text_size_dialog");
    }

    private void animate() {
        if (animation == null) {
            animation = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.blink);
        }
        animation.setTarget(sampleTextView);

        if (!isAnimated) {
            animation.start();
            isAnimated = true;
        } else {
            animation.cancel();
            sampleTextView.setAlpha(1);
            isAnimated = false;
        }

        textStyle.setAnimated(isAnimated);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEXT_SIZE) {
            if (resultCode == Activity.RESULT_OK) {
                float size = data.getFloatExtra(ChatTextSizeDialog.EXTRA_TEXT_SIZE, 16);
                textStyle.setSize(size);
                sampleTextView.setTextSize(size);
            }
        } else if (requestCode == REQUEST_TEXT_FONT) {
            if (resultCode == Activity.RESULT_OK) {
                String font = data.getStringExtra(ChatTextFontDialog.EXTRA_TEXT_FONT);
                textStyle.setFont(font);
                FontHelper.setCustomFont(getActivity(), sampleTextView, font);
            }
        }
    }
}
