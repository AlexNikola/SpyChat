package com.incode_it.spychat.effects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.eftimoff.viewpagertransformers.AccordionTransformer;
import com.eftimoff.viewpagertransformers.BackgroundToForegroundTransformer;
import com.eftimoff.viewpagertransformers.CubeInTransformer;
import com.eftimoff.viewpagertransformers.CubeOutTransformer;
import com.eftimoff.viewpagertransformers.DepthPageTransformer;
import com.eftimoff.viewpagertransformers.FlipHorizontalTransformer;
import com.eftimoff.viewpagertransformers.FlipVerticalTransformer;
import com.eftimoff.viewpagertransformers.RotateDownTransformer;
import com.eftimoff.viewpagertransformers.StackTransformer;
import com.eftimoff.viewpagertransformers.TabletTransformer;
import com.eftimoff.viewpagertransformers.ZoomInTransformer;
import com.incode_it.spychat.R;

import java.util.Arrays;
import java.util.List;

public class EffectsSelectorActivity extends AppCompatActivity implements View.OnClickListener {

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
        //setupResideMenu();
    }



    private void setupResideMenu() {
        /*resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.spylinklogo);
        resideMenu.attachToActivity(this);

        // create menu items;
        String titles[] = { "Home", "Profile", "Calendar", "Settings" };
        int icon[] = { R.drawable.clear_24dp, R.drawable.attachments, R.drawable.home_24dp, R.drawable.file_download };

        for (int i = 0; i < titles.length; i++){
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            item.setOnClickListener(this);
            resideMenu.addMenuItem(item,  ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT
        }*/
    }

    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new EffectsViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                page.setPivotX(position < 0.0F?(float)page.getWidth():0.0F);
                page.setPivotY((float)page.getHeight() * 0.5F);
                page.setRotationY(45.0F * position);
            }
        });

        int position = getIntent().getIntExtra(EXTRA_EFFECT_TYPE, 0);
        viewPager.setCurrentItem(position);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onClick(View v) {

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
