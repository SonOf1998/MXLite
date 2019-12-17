package com.matekisdev.weebly.mxlite;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.matekisdev.weebly.mxlite.database.DatabaseHelper;
import com.matekisdev.weebly.mxlite.fragments.FileBrowserDialog;
import com.matekisdev.weebly.mxlite.listeners.HoverTouchListener;

import java.io.File;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppThemeChanger atc = AppThemeChanger.makeInstance(this);
        atc.applyThemeOnActivity(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(R.layout.activity_main);
        checkRuntimePermissions();

        final Button bOpenFile = findViewById(R.id.bOpenFile);
        final Button bSettings = findViewById(R.id.bSettings);

        bOpenFile.setOnTouchListener(new HoverTouchListener(input -> {

            // ha akad a UI akkor ezzel megfékezhető a dialog fragment sokszori megnyitása
            if (!FileBrowserDialog.isOpen)
            {
                DialogFragment fileBrowserDialog = new FileBrowserDialog();
                fileBrowserDialog.show(getSupportFragmentManager(), "file_browser_dialog");
            }

            return input;
        }));
        
        bSettings.setOnTouchListener(new HoverTouchListener(input -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return input;
        }));
    }

    private boolean hasMissingPermission()
    {
        return  ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void checkRuntimePermissions()
    {
        if(hasMissingPermission())
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        else
        {
            // későbbre előkészítjük az adatbázist
            DatabaseHelper.makeInstance(this);

            handleIntent(getIntent());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == 0) {
            if (!hasMissingPermission())
            {
                // későbbre előkészítjük az adatbázist
                DatabaseHelper.makeInstance(this);

                handleIntent(getIntent());
            }
            else
            {
                String instructionText = "Please accept every permission request in order to continue!\n\n" +
                        "WRITE_PERMISSION: Allows you to browse, delete and edit audiovisuals. Without this one, " +
                        "the app cannot functionate as expected. \n\n\n" +
                        "Thank you for your understanding!";

                new AlertDialog.Builder(this).setMessage(instructionText)
                        .setCancelable(false)
                        .setPositiveButton(Html.fromHtml("<font color=\"#D81B60\">EXIT</font>"), (dialog, which) -> finish()).create().show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    /* Ez az amikor kívülről nyitunk meg agy videót és nem az alkalmazásból tallózunk,
     *
     */
    private void handleIntent(Intent intent)
    {
        if (Objects.equals(intent.getAction(), Intent.ACTION_VIEW))
        {
            String filePath = Objects.requireNonNull(intent.getData()).getPath();
            boolean hasSrt = false;


            if (new File(filePath.replace(".mp4", ".srt")).exists())
            {
                hasSrt = true;
            }



            Intent activityStartIntent = new Intent(this, VideoActivity.class);
            activityStartIntent.putExtra("path", filePath);
            activityStartIntent.putExtra("hasSrt", hasSrt);
            startActivity(activityStartIntent);
        }
    }
}
