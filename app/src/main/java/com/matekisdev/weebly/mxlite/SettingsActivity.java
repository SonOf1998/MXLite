package com.matekisdev.weebly.mxlite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SettingsActivity extends AppCompatActivity {

    private Theme originalTheme;
    private Theme newTheme;

    private Button restartButton;
    private TextView restartWarning;

    private boolean restartNeeded()
    {
        if (newTheme != null && (newTheme != originalTheme))
        {
            return true;
        }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppThemeChanger atc = AppThemeChanger.getInstance();
        atc.applyThemeOnActivity(this);



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.activity_settings);

        originalTheme = AppThemeChanger.getInstance().getTheme();
        setUpThemeSwitching();
        setUpRestartButton();
    }

    private void setUpThemeSwitching()
    {
        Switch themeSwitcher = findViewById(R.id.swThemeChanger);
        themeSwitcher.setChecked(AppThemeChanger.getInstance().getTheme() != Theme.THEME_LIGHT);

        themeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppThemeChanger.getInstance().switchTheme();
                newTheme = AppThemeChanger.getInstance().getTheme();

                restartButton.setVisibility(restartNeeded() ? View.VISIBLE : View.INVISIBLE);
                restartWarning.setVisibility(restartNeeded() ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void setUpRestartButton()
    {
        restartButton = findViewById(R.id.bRestart);
        restartWarning = findViewById(R.id.tvRestart);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_MAIN);
                SettingsActivity.this.startActivity(intent);
                SettingsActivity.this.finish();
                Runtime.getRuntime().exit(0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (restartNeeded())
        {

        }
        else
        {
            super.onBackPressed();
        }
    }
}
