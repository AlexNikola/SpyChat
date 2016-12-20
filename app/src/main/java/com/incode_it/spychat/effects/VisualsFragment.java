package com.incode_it.spychat.effects;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.incode_it.spychat.R;

public class VisualsFragment extends Fragment {

    private VisualsView visualsView;
    private RadioGroup radioGroup;

    public static final String EXTRA_EFFECT_ID = "EXTRA_EFFECT_ID";

    public VisualsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_effects_selector, container, false);
        visualsView = (VisualsView) view.findViewById(R.id.container);

        radioGroup = (RadioGroup) view.findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.balloons:
                        visualsView.startBalloons();
                        break;
                    case R.id.firework:
                        visualsView.startFirework();
                        break;
                    case R.id.love:
                        visualsView.startLove();
                        break;
                    case R.id.party:
                        visualsView.startParty();
                        break;
                    case R.id.none:
                        visualsView.cancel();
                        break;
                }
            }
        });

        return view;
    }

    public void done(Intent intent) {
        int id = radioGroup.getCheckedRadioButtonId();
        if (id == -1) return;

        switch (id) {
            case R.id.balloons:
                intent.putExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_BALLOON);
                break;
            case R.id.firework:
                intent.putExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_FIREWORK);
                break;
            case R.id.love:
                intent.putExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_LOVE);
                break;
            case R.id.party:
                intent.putExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_PARTY);
                break;
            case R.id.none:
                intent.putExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_NONE);
                break;
        }
    }
}
