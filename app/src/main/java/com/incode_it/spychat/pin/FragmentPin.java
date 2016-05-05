package com.incode_it.spychat.pin;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;

public class FragmentPin extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "FragmentPin";

    private SharedPreferences sharedPreferences;
    private ImageView btnCancel;
    private Button btnClear;
    private TextView pinTextView;
    private String enteredPinCode = "";
    private Button logOutBtn;

    private FragmentPinListener fragmentPinListener;

    public static FragmentPin newInstance() {
        FragmentPin f = new FragmentPin();
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentPinListener = (FragmentPinListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pin, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // remove background dim
        getDialog().getWindow().setDimAmount(0.95f);
        setCancelable(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        pinTextView = (TextView) view.findViewById(R.id.pin_text);
        logOutBtn = (Button) view.findViewById(R.id.security_log_out);
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentPinListener.onSecurityLogOut();
            }
        });

        btnClear = (Button) view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredPinCode = "";
                pinTextView.setTextColor(Color.WHITE);
                pinTextView.setText(enteredPinCode);
            }
        });
        btnCancel = (ImageView) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentPinListener.onSecurityClose();
            }
        });

        view.findViewById(R.id.pin_1).setOnClickListener(this);
        view.findViewById(R.id.pin_2).setOnClickListener(this);
        view.findViewById(R.id.pin_3).setOnClickListener(this);
        view.findViewById(R.id.pin_4).setOnClickListener(this);
        view.findViewById(R.id.pin_5).setOnClickListener(this);
        view.findViewById(R.id.pin_6).setOnClickListener(this);
        view.findViewById(R.id.pin_7).setOnClickListener(this);
        view.findViewById(R.id.pin_8).setOnClickListener(this);
        view.findViewById(R.id.pin_9).setOnClickListener(this);
        view.findViewById(R.id.pin_0).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        String number = "";
        switch (v.getId())
        {
            case R.id.pin_1:
                number = "1";
                break;
            case R.id.pin_2:
                number = "2";
                break;
            case R.id.pin_3:
                number = "3";
                break;
            case R.id.pin_4:
                number = "4";
                break;
            case R.id.pin_5:
                number = "5";
                break;
            case R.id.pin_6:
                number = "6";
                break;
            case R.id.pin_7:
                number = "7";
                break;
            case R.id.pin_8:
                number = "8";
                break;
            case R.id.pin_9:
                number = "9";
                break;
            case R.id.pin_0:
                number = "0";
                break;
        }
        if (enteredPinCode.length() < 4)
        {
            enteredPinCode += number;
            pinTextView.setText(enteredPinCode);
            String pin = sharedPreferences.getString(C.PIN, "1111");
            if (enteredPinCode.equals(pin))
            {
                getDialog().dismiss();
            }
            else if (enteredPinCode.length() == 4)
            {
                pinTextView.setTextColor(Color.RED);
            }

        }


    }

    public interface FragmentPinListener
    {
        void onSecurityClose();

        void onSecurityLogOut();
    }
}