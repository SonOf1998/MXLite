package com.matekisdev.weebly.mxlite;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import static com.matekisdev.weebly.mxlite.Theme.*;

enum Theme
{
    THEME_LIGHT,
    THEME_DARK
}

class AppThemeChanger
{
    private static Theme theme;

    private static AppThemeChanger instance;
    private static SharedPreferences sp;

    static AppThemeChanger makeInstance(Context context)
    {
        if (instance == null)
        {
            instance = new AppThemeChanger();
            sp = ((AppCompatActivity)context).getPreferences(Context.MODE_PRIVATE);
        }

        return instance;
    }

    static AppThemeChanger getInstance()
    {
        if (instance == null)
        {
            Log.e("App theme changer err:", "Uninitialized changer!");
        }

        return instance;
    }

    private AppThemeChanger()
    {

    }

    Theme getTheme()
    {
        return theme;
    }

    /* Futásidejű témaváltás lehetetlen.
     * A switchTheme után azonnali újraindítás jön, ennek köszönhető, hogy a
     * témaváltás ténleg bekövetkezik.
     */
    void switchTheme()
    {
        switch (theme)
        {
            case THEME_DARK:
                theme = THEME_LIGHT;
                break;

            case THEME_LIGHT:
                theme = THEME_DARK;
                break;
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("theme", theme.ordinal());
        editor.commit();    // Nem aszinkron, hiszen esélyes lenne, hogy az app előbb indulna újra.
    }

    void applyThemeOnActivity(Context context)
    {
        int themeId = sp.getInt("theme", 0);

        switch (themeId)
        {
            case 0:
                theme = Theme.THEME_LIGHT;
                context.setTheme(R.style.LightTheme);
                break;

            case 1:
                theme = THEME_DARK;
                context.setTheme(R.style.DarkTheme);
                break;
        }
    }
}
