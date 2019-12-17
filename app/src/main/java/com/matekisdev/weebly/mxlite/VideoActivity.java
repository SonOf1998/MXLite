package com.matekisdev.weebly.mxlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.matekisdev.weebly.mxlite.database.DatabaseHelper;
import com.matekisdev.weebly.mxlite.listeners.ControlSliderGestureListener;
import com.matekisdev.weebly.mxlite.listeners.DoubleClickListener;

import org.antlr.runtime.UnwantedTokenException;

import java.io.File;


/* Minden működik, amit specifikáltam kivéve a szinkronszöveg állomány kijelzése.
 * ExoPlayer-ben könnyű megoldás ennek megoldására sajnos nincs.
 * Néha működött, néha pedig elszáltt csak piros logcat üzenetekkel, mindenféle iránymutatás nélkül.
 * Az exo playertől független srt feldolgozásba pedig nem szeretnék belekezdeni ídő hiányában.
 *
 *
 *
 */


public class VideoActivity extends AppCompatActivity
{
    private SimpleExoPlayer player;
    private String path;

    private TextView brightnessLabel;
    private TextView brightness;
    private TextView volumeLabel;
    private TextView volume;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AppThemeChanger atc = AppThemeChanger.getInstance();
        atc.applyThemeOnActivity(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_video);

        brightnessLabel = findViewById(R.id.tvBrightnessLabel);
        brightness = findViewById(R.id.tvBrightness);
        volumeLabel = findViewById(R.id.tvVolumeLabel);
        volume = findViewById(R.id.tvVolume);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init()
    {
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        boolean hasSrt = intent.getBooleanExtra("hasSrt", false );
        int progress = intent.getIntExtra("progress", 0);

        if (lastPos == null)
        {
            lastPos = (long) (progress * 1000); // ms-sé alakítás
        }



        PlayerView playerView = findViewById(R.id.pvVideoPlayer);

        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "MXLite"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(new File(path)));
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == ExoPlayer.STATE_ENDED)
                {
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                }
            }
        });

        GestureDetector gd = new GestureDetector(this, new DoubleClickListener(player, playerView));
        playerView.setOnTouchListener((v, event) -> gd.onTouchEvent(event));



        ImageButton scaleTypeButton = playerView.findViewById(R.id.exo_scale);
        scaleTypeButton.setOnClickListener(v -> {

            if (v.getTag() == "fullscreen")
            {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                scaleTypeButton.setTag("default");
                scaleTypeButton.setImageDrawable(getResources().getDrawable(R.drawable.fullscreen_turn_on));
            }
            else
            {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                scaleTypeButton.setTag("fullscreen");
                scaleTypeButton.setImageDrawable(getResources().getDrawable(R.drawable.fullscreen_turn_off));
            }
        });

        if (hasSrt)
        {
            ImageButton srtEnableButton = playerView.findViewById(R.id.exo_srt);
            srtEnableButton.setImageDrawable(getResources().getDrawable(R.drawable.exo_srt_enabled ));
        }


        View brightnessSlider = findViewById(R.id.vBrightnessSlider);



        float currBrightness = 0.0f;

        try
        {
            currBrightness = (float)Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255;
        }
        catch (Settings.SettingNotFoundException ss)
        {
            // unhandled
        }

        brightness.setText(String.valueOf((int)(currBrightness * 100)) + '%');

        GestureDetector gdBrightness = new GestureDetector(this, new ControlSliderGestureListener(floatInput -> {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) (floatInput * 255));
            brightness.setText(String.valueOf((int)(floatInput * 100)) + '%');
            reportBrightnessSliderUsage();
            return null;
        },
        input -> {
            brightness.setVisibility(View.VISIBLE);
            brightnessLabel.setVisibility(View.VISIBLE);

            reportBrightnessSliderUsage();
            return null;
        }
        , currBrightness));
        gdBrightness.setIsLongpressEnabled(false);
        brightnessSlider.setOnTouchListener((v, event) -> gdBrightness.onTouchEvent(event));
        brightnessSlider.setOnClickListener(v -> { });  // kötelez rá valamiért, egymagába a gesture listener nem működik.. =(





        View volumeSlider = findViewById(R.id.vVolumeSlider);

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

        volume.setText(String.valueOf(100 * curVolume / maxVolume) + '%');

        GestureDetector gdVolume = new GestureDetector(this, new ControlSliderGestureListener(floatInput -> {
            int factorizedVol = (int) (maxVolume * floatInput);
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, factorizedVol, 0);
            volume.setText(String.valueOf((int)(floatInput * 100)) + '%');
            reportVolumeSliderUsage();
            return null;
        },

        input -> {
            volume.setVisibility(View.VISIBLE);
            volumeLabel.setVisibility(View.VISIBLE);

            reportVolumeSliderUsage();
            return null;
        },
        (float)curVolume / maxVolume));

        gdVolume.setIsLongpressEnabled(false);
        volumeSlider.setOnTouchListener((v, event) -> gdVolume.onTouchEvent(event));
        volumeSlider.setOnClickListener(v -> {});
    }

    private Runnable brightnessRunnable = null;
    private Runnable volumeRunnable = null;

    private void reportBrightnessSliderUsage()
    {
        if (brightnessRunnable == null)
        {
            brightnessRunnable = () -> {

                Animation fade = AnimationUtils.loadAnimation(VideoActivity.this, R.anim.fade_out_anim);

                if (brightness.getVisibility() == View.VISIBLE && brightnessLabel.getVisibility() == View.VISIBLE)
                {
                    brightness.startAnimation(fade);
                    brightnessLabel.startAnimation(fade);

                    brightness.setVisibility(View.INVISIBLE);
                    brightnessLabel.setVisibility(View.INVISIBLE);
                }

            };

            brightness.postDelayed(brightnessRunnable, 2500);
            brightnessLabel.postDelayed(brightnessRunnable, 2500);
        }
        else
        {
            brightness.removeCallbacks(brightnessRunnable);
            brightnessLabel.removeCallbacks(brightnessRunnable);

            brightness.postDelayed(brightnessRunnable, 2500);
            brightnessLabel.postDelayed(brightnessRunnable, 2500);
        }
    }

    private void reportVolumeSliderUsage()
    {
        if (volumeRunnable == null)
        {
            volumeRunnable = () -> {

                Animation fade = AnimationUtils.loadAnimation(VideoActivity.this, R.anim.fade_out_anim);

                if (volume.getVisibility() == View.VISIBLE && volumeLabel.getVisibility() == View.VISIBLE)
                {
                    volume.startAnimation(fade);
                    volumeLabel.startAnimation(fade);

                    volume.setVisibility(View.INVISIBLE);
                    volumeLabel.setVisibility(View.INVISIBLE);
                }

            };

            volume.postDelayed(volumeRunnable, 2500);
            volumeLabel.postDelayed(volumeRunnable, 2500);
        }
        else
        {
            volume.removeCallbacks(volumeRunnable);
            volumeLabel.removeCallbacks(volumeRunnable);

            volume.postDelayed(volumeRunnable, 2500);
            volumeLabel.postDelayed(volumeRunnable, 2500);
        }
    }

    /*
     TODO:

     Swipe-ok
     Menü megjelenítése swipe mellé kattintással
     Srt lejátszása
     Menün vezérelhető src

     */

    Long lastPos;

    @Override
    protected void onResume()
    {
        super.onResume();

        init();
        player.seekTo(lastPos);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        player.stop();
        lastPos = player.getCurrentPosition();
    }

    protected void onStop()
    {
        super.onStop();

        DatabaseHelper.getInstance().addToRecent(path, (int) (lastPos / 1000));
    }
}
