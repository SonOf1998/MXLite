package com.matekisdev.weebly.mxlite.misc;

import android.annotation.SuppressLint;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class FormatterFunctions
{
    @SuppressLint("DefaultLocale")
    public static String to_hhmmss(int time)
    {
        int hour = time / 3600;
        int min = (time - (hour * 3600)) / 60;
        int sec = time % 60;

        if (hour != 0)
        {
            return String.format("%02d:%02d:%02d", hour, min, sec);
        }
        else
        {
            return String.format("%02d:%02d", min, sec);
        }
    }

    public static String toSizeFormat(long bytes)
    {
        double kB = bytes / 1024.0;

        DecimalFormat decimalFormat = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);

        String sizeStr;

        if (kB < 1024)
        {
            sizeStr = decimalFormat.format(kB) + " kB";
        }
        else if (kB < 1024 * 1024)
        {
            sizeStr = decimalFormat.format(kB / 1024) + " MB";
        }
        else
        {
            sizeStr = decimalFormat.format(kB / 1024 / 1024) + " GB";
        }

        return sizeStr;
    }

    public static long sizeFormatToLong(String sizeStr)
    {
        String split[] = sizeStr.split(" ");
        double num = Double.parseDouble(split[0]);
        long factor;

        if (split[1].equals("kB"))
        {
            factor = 1024;
        }
        else if (split[1].equals("MB"))
        {
            factor = 1024 * 1024;
        }
        else
        {
            factor = 1024 * 1024 * 1024;
        }

        return (long)num * factor;
    }

    public static int dateFormatToInt(String dateStr)
    {
        String split[] = dateStr.split(":");
        int res = 0;

        for (String s : split)
        {
            res = res * 60 + Integer.parseInt(s);
        }

        return res;
    }
}
