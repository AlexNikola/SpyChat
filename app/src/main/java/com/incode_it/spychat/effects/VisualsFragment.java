package com.incode_it.spychat.effects;

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
import android.widget.RadioGroup;

import com.incode_it.spychat.R;

import java.util.ArrayList;

public class VisualsFragment extends Fragment implements View.OnClickListener {

    private VisualsView visualsView;

    private ArrayList<View> buttons = new ArrayList<>();

    public static final String EXTRA_EFFECT_ID = "EXTRA_EFFECT_ID";

    View balloons;
    View fireworks;
    View love;
    View party;
    View none;

    public VisualsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Log.d("sfsdfd", "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_effects_selector, container, false);
        visualsView = (VisualsView) view.findViewById(R.id.container);
        Log.d("sfsdfd", "onCreateView: " + savedInstanceState);
        if(savedInstanceState == null) {
            int effectId = getActivity().getIntent().getIntExtra(EXTRA_EFFECT_ID, VisualsView.EFFECT_NONE);
            visualsView.setCurrentEffectId(effectId);
        }

        balloons = view.findViewById(R.id.balloons);
        fireworks = view.findViewById(R.id.firework);
        love = view.findViewById(R.id.love);
        party = view.findViewById(R.id.party);
        none = view.findViewById(R.id.none);

        buttons.add(balloons);
        buttons.add(fireworks);
        buttons.add(love);
        buttons.add(party);
        buttons.add(none);

        balloons.setOnClickListener(this);
        fireworks.setOnClickListener(this);
        love.setOnClickListener(this);
        party.setOnClickListener(this);
        none.setOnClickListener(this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (visualsView.currentEffect) {
            case VisualsView.EFFECT_BALLOON:
                balloons.setSelected(true);
                break;
            case VisualsView.EFFECT_FIREWORK:
                fireworks.setSelected(true);
                break;
            case VisualsView.EFFECT_LOVE:
                love.setSelected(true);
                break;
            case VisualsView.EFFECT_PARTY:
                party.setSelected(true);
                break;
            case VisualsView.EFFECT_NONE:
                none.setSelected(true);
                break;
        }
    }

    public void done(Intent intent) {
        intent.putExtra(EXTRA_EFFECT_ID, visualsView.currentEffect);
    }

    @Override
    public void onClick(View v) {
        for (View view: buttons) {
            view.setSelected(false);
        }
        switch (v.getId()) {
            case R.id.balloons: {
                balloons.setSelected(true);
                visualsView.start(VisualsView.EFFECT_BALLOON);
                break;
            }
            case R.id.firework: {
                fireworks.setSelected(true);
                visualsView.start(VisualsView.EFFECT_FIREWORK);
                break;
            }
            case R.id.love: {
                love.setSelected(true);
                visualsView.start(VisualsView.EFFECT_LOVE);
                break;
            }
            case R.id.party: {
                party.setSelected(true);
                visualsView.start(VisualsView.EFFECT_PARTY);
                break;
            }
            case R.id.none: {
                none.setSelected(true);
                visualsView.start(VisualsView.EFFECT_NONE);
                break;
            }
        }
    }
}
