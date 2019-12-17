package com.matekisdev.weebly.mxlite.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.matekisdev.weebly.mxlite.R;
import com.matekisdev.weebly.mxlite.adapter.FileBrowserAdapter;


public class FileBrowserDialog extends DialogFragment
{
    public static boolean isOpen = false;

    public FileBrowserDialog()
    {
        isOpen = true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null)
        {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button bRecentFiles = view.findViewById(R.id.bRecentFiles);
        final Button bAllFiles = view.findViewById(R.id.bAllFiles);
        bRecentFiles.setTypeface(null, Typeface.BOLD);

        // alapből legyen az Recent fül kiválasztva
        bRecentFiles.setBackgroundResource(R.drawable.recent_button_selected);
        bAllFiles.setBackgroundResource(R.drawable.all_button_unselected);

        final ViewPager vp = view.findViewById(R.id.vpFileBrowser);
        vp.setAdapter(new FileBrowserAdapter(getChildFragmentManager()));
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position)
                {
                    case 0:
                        bRecentFiles.setBackgroundResource(R.drawable.recent_button_selected);
                        bAllFiles.setBackgroundResource(R.drawable.all_button_unselected);
                        bRecentFiles.setTypeface(null, Typeface.BOLD);
                        bAllFiles.setTypeface(null, Typeface.NORMAL);
                        break;

                    case 1:
                        bAllFiles.setBackgroundResource(R.drawable.all_button_selected);
                        bRecentFiles.setBackgroundResource(R.drawable.recent_button_unselected);
                        bRecentFiles.setTypeface(null, Typeface.NORMAL);
                        bAllFiles.setTypeface(null, Typeface.BOLD);
                        break;

                    default:

                        throw new IllegalArgumentException("invalid position");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        bRecentFiles.setOnClickListener(v -> vp.setCurrentItem(0, true));

        bAllFiles.setOnClickListener(v -> vp.setCurrentItem(1, true));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog)
    {
        super.onDismiss(dialog);
        isOpen = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_browser, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        final int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.9);
        final int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.9);

        if (getDialog() != null && getDialog().getWindow() != null)
        {
            getDialog().getWindow().setLayout(width, height);
        }
    }
}
