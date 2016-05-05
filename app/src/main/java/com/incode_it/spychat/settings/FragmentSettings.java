package com.incode_it.spychat.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;

public class FragmentSettings extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "FragmentSettings";

    private Switch switchSound, switchVibration, switchPin;
    private SharedPreferences sharedPreferences;
    private EditText pinInput;
    private Button pinSave;

    public static FragmentSettings newInstance() {
        FragmentSettings f = new FragmentSettings();
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        pinInput = (EditText) view.findViewById(R.id.pin_editText);
        String pin = sharedPreferences.getString(C.PIN, "");
        pinInput.setText(pin);
        pinSave = (Button) view.findViewById(R.id.pin_save);
        pinSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = pinInput.getText().toString();
                if (pin.length() == 4)
                {
                    sharedPreferences.edit().putString(C.PIN, pin).apply();
                    Toast.makeText(getContext(), R.string.pin_saved, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getContext(), R.string.pin_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        switchSound = (Switch) view.findViewById(R.id.sett_sound);
        switchSound.setOnCheckedChangeListener(this);
        switchVibration = (Switch) view.findViewById(R.id.sett_vibrate);
        switchVibration.setOnCheckedChangeListener(this);
        switchPin = (Switch) view.findViewById(R.id.sett_pin);
        switchPin.setOnCheckedChangeListener(this);
        boolean isPinSecured = sharedPreferences.getBoolean(C.SETTING_PIN, false);
        if (!isPinSecured)
        {
            switchPin.setChecked(false);
            pinInput.setEnabled(false);
            pinSave.setEnabled(false);
        }
        else
        {
            switchPin.setChecked(true);
            pinInput.setEnabled(true);
            pinSave.setEnabled(true);
        }


        boolean isSoundOn = sharedPreferences.getBoolean(C.SETTING_SOUND, true);
        boolean isVibrateOn = sharedPreferences.getBoolean(C.SETTING_VIBRATE, true);
        boolean isPinOn = sharedPreferences.getBoolean(C.SETTING_PIN, false);

        switchSound.setChecked(isSoundOn);
        switchVibration.setChecked(isVibrateOn);
        switchPin.setChecked(isPinOn);

        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId())
        {
            case R.id.sett_sound:
                sharedPreferences.edit().putBoolean(C.SETTING_SOUND, isChecked).apply();
                break;
            case R.id.sett_vibrate:
                sharedPreferences.edit().putBoolean(C.SETTING_VIBRATE, isChecked).apply();
                break;
            case R.id.sett_pin:
                sharedPreferences.edit().putBoolean(C.SETTING_PIN, isChecked).apply();
                pinInput.setEnabled(isChecked);
                pinSave.setEnabled(isChecked);
                break;
        }
    }
}
