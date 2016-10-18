package com.incode_it.spychat.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.incode_it.spychat.R;
import com.incode_it.spychat.utils.FontHelper;

public class ChatTextFontDialog extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TEXT_FONT = "choosen_text_font";

    private static final String FIRST_FONT = "fonts/Alabama.ttf";
    private static final String SECOND_FONT = "fonts/Fat.otf";
    private static final String THIRD_FONT = "fonts/Keetano.ttf";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_text_font, null);

        TextView guilty = (TextView) v.findViewById(R.id.guilty);
        TextView innocent = (TextView) v.findViewById(R.id.innocent);
        TextView tusi = (TextView) v.findViewById(R.id.tusi);
        TextView def = (TextView) v.findViewById(R.id.default_font);

        FontHelper.setCustomFont(getActivity(), guilty, FIRST_FONT);
        FontHelper.setCustomFont(getActivity(), innocent, SECOND_FONT);
        FontHelper.setCustomFont(getActivity(), tusi, THIRD_FONT);

        guilty.setOnClickListener(this);
        innocent.setOnClickListener(this);
        tusi.setOnClickListener(this);
        def.setOnClickListener(this);

        builder.setView(v);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        String font;
        switch (v.getId()) {
            case R.id.guilty:
                font = FIRST_FONT;
                break;
            case R.id.innocent:
                font = SECOND_FONT;
                break;
            case R.id.tusi:
                font = THIRD_FONT;
                break;
            default:
                font = null;
                break;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT_FONT, font);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
