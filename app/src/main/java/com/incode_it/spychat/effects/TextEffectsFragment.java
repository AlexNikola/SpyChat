package com.incode_it.spychat.effects;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.incode_it.spychat.R;
import com.incode_it.spychat.chat.ChatTextFontDialog;
import com.incode_it.spychat.chat.ChatTextSizeDialog;
import com.incode_it.spychat.utils.FontHelper;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

public class TextEffectsFragment extends Fragment implements View.OnClickListener {

    public static final int REQUEST_TEXT_FONT = 1;
    public static final int REQUEST_TEXT_SIZE = 2;
    public static final int REQUEST_ANIMATION = 3;

    private static final String SAVE_STATE_TEXT_STYLE = "SAVE_STATE_TEXT_STYLE";
    public static final String EXTRA_TEXT_STYLE = "EXTRA_TEXT_STYLE";


    private TextStyle textStyle;
    private TextView sampleTextView;

    public TextEffectsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            textStyle = (TextStyle) savedInstanceState.getSerializable(SAVE_STATE_TEXT_STYLE);
        } else {
            textStyle = (TextStyle) getActivity().getIntent().getSerializableExtra(EXTRA_TEXT_STYLE);
        }
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_STATE_TEXT_STYLE, textStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_effects, container, false);

        sampleTextView = (TextView) view.findViewById(R.id.sample);
        setStyle();


        view.findViewById(R.id.defaultStyle).setOnClickListener(this);
        view.findViewById(R.id.change_color).setOnClickListener(this);
        view.findViewById(R.id.change_font).setOnClickListener(this);
        view.findViewById(R.id.change_size).setOnClickListener(this);
        view.findViewById(R.id.change_animation).setOnClickListener(this);

        return view;
    }

    private void setStyle() {
        sampleTextView.setTextColor(textStyle.getColor());
        sampleTextView.setTextSize(textStyle.getSize());
        FontHelper.setCustomFont(getActivity(), sampleTextView, textStyle.getFont());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textStyle.animate(sampleTextView);
            }
        }, 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.defaultStyle:
                int animation = textStyle.getAnimationType();
                textStyle.refresh(getContext(), sampleTextView);
                textStyle.setAnimationType(animation);
                setStyle();
                break;
            case R.id.change_color:
                openColorPicker();
                break;
            case R.id.change_font:
                openFontPicker();
                break;
            case R.id.change_size:
                openSizePicker();
                break;
            case R.id.change_animation:
                openAnimationPicker();
                break;
        }
    }

    private void openAnimationPicker() {
        ChatTextAnimationDialog dialog = new ChatTextAnimationDialog();
        dialog.setTargetFragment(TextEffectsFragment.this, REQUEST_ANIMATION);
        dialog.show(getActivity().getSupportFragmentManager(), "ChatTextAnimationDialog");
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
                setStyle();
            }
        });

        dialog.show(getActivity().getFragmentManager(), "color_dialog_test");
    }

    private void openFontPicker() {
        ChatTextFontDialog dialog = new ChatTextFontDialog();
        dialog.setTargetFragment(TextEffectsFragment.this, REQUEST_TEXT_FONT);
        dialog.show(getActivity().getSupportFragmentManager(), "ChatTextFontDialog");
    }

    private void openSizePicker() {
        ChatTextSizeDialog dialog = new ChatTextSizeDialog();
        dialog.setTargetFragment(TextEffectsFragment.this, REQUEST_TEXT_SIZE);
        dialog.show(getActivity().getSupportFragmentManager(), "ChatTextSizeDialog");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TEXT_SIZE) {
            if (resultCode == Activity.RESULT_OK) {
                float size = data.getFloatExtra(ChatTextSizeDialog.EXTRA_TEXT_SIZE, 16);
                textStyle.setSize(size);
                setStyle();
            }
        } else if (requestCode == REQUEST_TEXT_FONT) {
            if (resultCode == Activity.RESULT_OK) {
                String font = data.getStringExtra(ChatTextFontDialog.EXTRA_TEXT_FONT);
                textStyle.setFont(font);
                setStyle();
            }
        } else if (requestCode == REQUEST_ANIMATION) {
            if (resultCode == Activity.RESULT_OK) {
                int animationType = data.getIntExtra(ChatTextAnimationDialog.EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_NONE);
                textStyle.animate(sampleTextView, animationType);
            }
        }
    }


    public void done(Intent intent) {
        intent.putExtra(EXTRA_TEXT_STYLE, textStyle);
    }
}
