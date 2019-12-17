package com.matekisdev.weebly.mxlite.listeners;

import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.arch.core.util.Function;

/* A view szövege érintéskor félkövér lesz. A view területéről kihúzva visszaáll normálisra.
 * Ha az elengedés pillananátban a területén voltunk, akkor meghívódik a példányosításnál
 * beadott function.
 *
 * Csak Button-ökre!
 */
public class HoverTouchListener implements View.OnTouchListener {

    private Rect rect;
    private Function<Void, Void> function;

    public HoverTouchListener(Function<Void, Void> function)
    {
        this.function = function;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Button button = (Button) v;

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            button.setTypeface(null, Typeface.BOLD);
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            if (rect != null)
            {

                if (rect.contains(v.getLeft() + (int)event.getX(), v.getTop() + (int)(event.getY())))
                {
                    button.setTypeface(null, Typeface.BOLD);
                }
                else
                {
                    button.setTypeface(null, Typeface.NORMAL);
                }

                return true;
            }
        }
        else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            if (rect != null && rect.contains(v.getLeft() + (int)event.getX(), v.getTop() + (int)(event.getY())))
            {
                button.setTypeface(null, Typeface.NORMAL);
                function.apply(null);
                return true;
            }
        }

        return false;
    }
}
