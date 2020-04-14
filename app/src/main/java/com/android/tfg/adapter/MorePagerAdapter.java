package com.android.tfg.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.android.tfg.R;
import com.android.tfg.view.HumChartFragment;
import com.android.tfg.view.PresChartFragment;
import com.android.tfg.view.TempChartFragment;
import com.android.tfg.view.UVChartFragment;
import com.google.android.gms.maps.SupportMapFragment;

public class MorePagerAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 4;
    private Context context;
    private String device;

    public MorePagerAdapter(@NonNull FragmentManager fm, Context c, String d){
        super(fm);
        context=c;
        device=d;
    }

    public MorePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            default: return new TempChartFragment();
            case 1: return new HumChartFragment();
            case 2: return new PresChartFragment();
            case 3: return new UVChartFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            default: return context.getString(R.string.tempTitle);
            case 1: return context.getString(R.string.humTitle);
            case 2: return context.getString(R.string.presTitle);
            case 3: return context.getString(R.string.uvTitle);
        }
    }
}