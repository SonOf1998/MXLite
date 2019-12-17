package com.matekisdev.weebly.mxlite.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.matekisdev.weebly.mxlite.fragments.AllFilesFragment;
import com.matekisdev.weebly.mxlite.fragments.RecentFilesFragment;

public class FileBrowserAdapter extends FragmentPagerAdapter {

    public FileBrowserAdapter(FragmentManager fm)
    {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                return new RecentFilesFragment();

            case 1:
                return new AllFilesFragment();

            default:
                throw new IllegalArgumentException("invalid position");
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
