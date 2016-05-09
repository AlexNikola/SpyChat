package com.incode_it.spychat.country_selection;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.util.ArrayList;

public class ActivitySelectCountry extends AppCompatActivity
{
    private ArrayList<Country> countryArrayList;
    private String selectedISO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country);

        selectedISO = getIntent().getStringExtra(C.EXTRA_COUNTRY_ISO);
        countryArrayList = MyDbHelper.readCountries(new MyDbHelper(this).getReadableDatabase());

        int position = 0;
        for (int i = 0; i < countryArrayList.size(); i++)
        {
            if (countryArrayList.get(i).codeISO.equalsIgnoreCase(selectedISO))
            {
                position = i;
            }
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        CountryAdapter adapter = new CountryAdapter();
        assert recyclerView != null;
        recyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.country_list_divider));
        recyclerView.scrollToPosition(position);
        recyclerView.setAdapter(adapter);


        Toolbar toolbar  = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(R.string.select_country);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder>
    {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.country_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String nameEnglish = countryArrayList.get(position).nameEnglish;
            String nameNative = countryArrayList.get(position).nameNative;
            String code = countryArrayList.get(position).codePhone;
            String iso = countryArrayList.get(position).codeISO;

            holder.nameNative.setText(nameEnglish);
            holder.nameEnglish.setText(nameNative);

            if (nameEnglish.equals(nameNative))
            {
                holder.nameEnglish.setVisibility(View.GONE);
            }
            else holder.nameEnglish.setVisibility(View.VISIBLE);

            if (iso.equalsIgnoreCase(selectedISO))
            {
                int color = getResources().getColor(R.color.colorPrimary);
                holder.nameNative.setTextColor(color);
                holder.code.setTextColor(color);
            }
            else
            {
                holder.nameNative.setTextColor(Color.DKGRAY);
                holder.code.setTextColor(Color.BLACK);
            }
            holder.code.setText(code);
        }

        @Override
        public int getItemCount() {
            return countryArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView nameNative, nameEnglish, code;

            public ViewHolder(View itemView) {
                super(itemView);
                nameNative = (TextView) itemView.findViewById(R.id.name_native);
                nameEnglish = (TextView) itemView.findViewById(R.id.name_english);
                code = (TextView) itemView.findViewById(R.id.code);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra(C.EXTRA_COUNTRY_CODE, countryArrayList.get(getAdapterPosition()).codePhone);
                        intent.putExtra(C.EXTRA_COUNTRY_ISO, countryArrayList.get(getAdapterPosition()).codeISO);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }
    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};

        private Drawable mDivider;

        /**
         * Default divider will be used
         */
        public DividerItemDecoration(Context context) {
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            mDivider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        /**
         * Custom divider will be used
         */
        public DividerItemDecoration(Context context, int resId) {
            mDivider = ContextCompat.getDrawable(context, resId);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }


}
