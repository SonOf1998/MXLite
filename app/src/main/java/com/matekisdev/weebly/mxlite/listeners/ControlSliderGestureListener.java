package com.matekisdev.weebly.mxlite.listeners;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.arch.core.util.Function;

public class ControlSliderGestureListener extends GestureDetector.SimpleOnGestureListener
{

    private final float distanceFromZeroToMax = 400.0f;   // mennyit kelljen csúszni fölfelé, hogy nulláról pl max fényerőre érjünk
    private float currentProgress;                 // hol tartunk 0.0-tól 1.0-ig terjedő skálán fényerőben / hangerőben
    private Function<Float, Void> function;               // csúsztatás ezt hívja, ez változtatja ténylegesen pl a fényerőt
    private Function<Void, Void> visibilitySetter;


    public ControlSliderGestureListener(Function<Float, Void> function, Function<Void, Void> visibilitySetter, float currentProgress)
    {
        this.function = function;
        this.currentProgress = currentProgress;
        this.visibilitySetter = visibilitySetter;
    }



    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        currentProgress += distanceY;

        currentProgress = Math.max(currentProgress, 0);
        currentProgress = Math.min(currentProgress, distanceFromZeroToMax);

        function.apply(currentProgress / distanceFromZeroToMax);

        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        visibilitySetter.apply(null);
        return super.onDown(e);
    }
}
