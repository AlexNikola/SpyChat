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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private static final String SAVE_STATE_TEXT_STYLE = "SAVE_STATE_TEXT_STYLE";
    public static final String EXTRA_TEXT_STYLE = "EXTRA_TEXT_STYLE";


    private TextStyle textStyle;
    private TextView sampleTextView;
    private AnimatorSet animation;

    public TextEffectsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            textStyle = (TextStyle) savedInstanceState.getSerializable(SAVE_STATE_TEXT_STYLE);
        } else {
            textStyle = new TextStyle(getContext());
        }
        setHasOptionsMenu(true);
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

        view.findViewById(R.id.change_color).setOnClickListener(this);
        view.findViewById(R.id.change_font).setOnClickListener(this);
        view.findViewById(R.id.change_size).setOnClickListener(this);
        view.findViewById(R.id.change_blink).setOnClickListener(this);

        return view;
    }

    private void setStyle() {
        sampleTextView.setTextColor(textStyle.getColor());
        sampleTextView.setTextSize(textStyle.getSize());
        FontHelper.setCustomFont(getActivity(), sampleTextView, textStyle.getFont());
        animate(textStyle.isAnimated());
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
                animate(!textStyle.isAnimated());
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

    private void animate(boolean isAnimated) {
        if (animation == null) {
            animation = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.blink);

        }

        animation.setTarget(sampleTextView);

        if (isAnimated) {
            animation.start();
        } else {
            animation.cancel();
            sampleTextView.setAlpha(1);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_text_art_selector, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.action_done:
                done();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void done() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT_STYLE, textStyle);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
