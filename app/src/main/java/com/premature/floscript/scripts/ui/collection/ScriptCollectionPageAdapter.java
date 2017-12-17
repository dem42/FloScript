package com.premature.floscript.scripts.ui.collection;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.premature.floscript.R;

/**
 * Created by Martin on 12/16/2017.
 */

public class ScriptCollectionPageAdapter extends FragmentPagerAdapter {

    private static final int NUM_ITEMS = 2;
    private final String[] pageTitles;

    public ScriptCollectionPageAdapter(FragmentManager fm, String[] pageTitles) {
        super(fm);
        this.pageTitles = pageTitles;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ScriptCollectionPageFragment.newInstance(ScriptCollectionPageType.BASIC);
            case 1:
                return ScriptCollectionPageFragment.newInstance(ScriptCollectionPageType.ADVANCED);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}
