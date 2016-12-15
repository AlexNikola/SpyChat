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
import android.widget.Toast;

import com.incode_it.spychat.R;

import java.util.ArrayList;

public class EffectsSelectorFragment extends Fragment {

    private EffectsView effectsView;
    private RadioGroup radioGroup;

    public static final String EXTRA_EFFECT_ID = "EXTRA_EFFECT_ID";

    public EffectsSelectorFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_effects_selector, container, false);
        effectsView = (EffectsView) view.findViewById(R.id.container);

        radioGroup = (RadioGroup) view.findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.balloons:
                        effectsView.startBalloons();
                        break;
                    case R.id.firework:
                        effectsView.startFirework();
                        break;
                    case R.id.love:
                        effectsView.startLove();
                        break;
                    case R.id.party:
                        effectsView.startParty();
                        break;
                    case R.id.none:
                        effectsView.cancel();
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_effects_selector, menu);
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
        int id = radioGroup.getCheckedRadioButtonId();
        if (id == -1) return;

        Intent intent = new Intent();
        switch (id) {
            case R.id.balloons:
                intent.putExtra(EXTRA_EFFECT_ID, EffectsView.EFFECT_BALLOON);
                break;
            case R.id.firework:
                intent.putExtra(EXTRA_EFFECT_ID, EffectsView.EFFECT_FIREWORK);
                break;
            case R.id.love:
                intent.putExtra(EXTRA_EFFECT_ID, EffectsView.EFFECT_LOVE);
                break;
            case R.id.party:
                intent.putExtra(EXTRA_EFFECT_ID, EffectsView.EFFECT_PARTY);
                break;
            case R.id.none:
                intent.putExtra(EXTRA_EFFECT_ID, EffectsView.EFFECT_NONE);
                break;
        }
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
