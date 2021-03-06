package com.incode_it.spychat.effects;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.incode_it.spychat.R;

public class ChatTextAnimationDialog extends DialogFragment implements View.OnClickListener {

    public static final String EXTRA_ANIMATION_TYPE = "EXTRA_ANIMATION_TYPE";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_chat_text_animation, null);

        final TextView blink = (TextView) v.findViewById(R.id.blink);
        blink.setOnClickListener(this);



        final TextView bounce = (TextView)v.findViewById(R.id.bounce);
        bounce.setOnClickListener(this);


        final TextView shake = (TextView)v.findViewById(R.id.shake);
        shake.setOnClickListener(this);

        final TextView rotationX = (TextView)v.findViewById(R.id.rotationX);
        rotationX.setOnClickListener(this);

        final TextView rotationY = (TextView)v.findViewById(R.id.rotationY);
        rotationY.setOnClickListener(this);


        TextView none = (TextView)v.findViewById(R.id.none);
        none.setOnClickListener(this);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                new TextStyle(getContext()).animate(blink, TextStyle.ANIMATION_BLINK);
                new TextStyle(getContext()).animate(bounce, TextStyle.ANIMATION_BOUNCE);
                new TextStyle(getContext()).animate(shake, TextStyle.ANIMATION_SHAKE);
                new TextStyle(getContext()).animate(rotationX, TextStyle.ANIMATION_ROTATION_X);
                new TextStyle(getContext()).animate(rotationY, TextStyle.ANIMATION_ROTATION_Y);
            }
        });

        builder.setView(v);
        return builder.create();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.blink:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_BLINK);
                break;
            case R.id.bounce:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_BOUNCE);
                break;
            case R.id.shake:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_SHAKE);
                break;
            case R.id.rotationX:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_ROTATION_X);
                break;
            case R.id.rotationY:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_ROTATION_Y);
                break;
            case R.id.none:
                intent.putExtra(EXTRA_ANIMATION_TYPE, TextStyle.ANIMATION_NONE);
                break;
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
