package com.incode_it.spychat.effects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.incode_it.spychat.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EffectsSelectorActivity extends AppCompatActivity {

    public static final String EXTRA_EFFECT_TYPE = "EXTRA_EFFECT_TYPE";
    public static final int TEXT_EFFECTS = 0;
    public static final int VISUALS = 1;

    private List<Object> fragmentArrayList = Arrays.asList(null, null);
    private ViewPager viewPager;
    private EffectsViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effects_selector);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewPager();
    }

    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new EffectsViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        int position = getIntent().getIntExtra(EXTRA_EFFECT_TYPE, 0);
        viewPager.setCurrentItem(position);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private class EffectsViewPagerAdapter extends FragmentPagerAdapter
    {

        EffectsViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TEXT_EFFECTS:
                    return new TextEffectsFragment();
                case VISUALS:
                    return new VisualsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object fragment = super.instantiateItem(container, position);
            fragmentArrayList.set(position, fragment);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TEXT_EFFECTS:
                    return "Text art";
                case VISUALS:
                    return "Visuals";
                default:
                    return "";
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_effects_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
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

        TextEffectsFragment textEffectsFragment = (TextEffectsFragment) fragmentArrayList.get(0);
        textEffectsFragment.done(intent);

        VisualsFragment visualsFragment = (VisualsFragment) fragmentArrayList.get(1);
        visualsFragment.done(intent);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
