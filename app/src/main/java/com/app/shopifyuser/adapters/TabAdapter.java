package com.app.shopifyuser.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabAdapter extends FragmentStatePagerAdapter {

    private final Fragment[] fragments;
    private final String[] pageTitles;

    public TabAdapter(FragmentManager fm, int behavior, Fragment[] fragments, String[] pageTitles) {
        super(fm, behavior);
        this.fragments = fragments;
        this.pageTitles = pageTitles;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }


    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    }
}
