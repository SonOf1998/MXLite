package com.matekisdev.weebly.mxlite.listeners;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class DoubleClickListener extends GestureDetector.SimpleOnGestureListener
{
    private SimpleExoPlayer player;
    private PlayerView playerView;

    public DoubleClickListener(SimpleExoPlayer player, PlayerView playerView)
    {
        this.player = player;
        this.playerView = playerView;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        // bal oldali double click
        if (e.getX() < playerView.getMeasuredWidth() / 2)
        {
            player.seekTo(player.getCurrentPosition() - 10000);
        }
        // jobb oldali double click
        else if (e.getX() > playerView.getMeasuredWidth() / 2)
        {
            player.seekTo(player.getCurrentPosition() + 10000);
        }

        return super.onDoubleTap(e);
    }
}
