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
import com.incode_it.spychat.utils.Metric;

public class ChatTextSizeDialog extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_TEXT_SIZE = "choosen_text_size";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_text_size, null);

        TextView small = (TextView) v.findViewById(R.id.small);
        TextView normal = (TextView) v.findViewById(R.id.normal);
        TextView large = (TextView) v.findViewById(R.id.large);
        TextView huge = (TextView) v.findViewById(R.id.huge);

        small.setOnClickListener(this);
        normal.setOnClickListener(this);
        large.setOnClickListener(this);
        huge.setOnClickListener(this);

        builder.setView(v);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        TextView textView = (TextView) v;
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT_SIZE, Metric.pixelsToSp(getActivity(), textView.getTextSize()));
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
