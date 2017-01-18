package com.vrublack.pocketpicasso;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Util
{
    public static String getPref(Context c, String key)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        return s.getString(key, "");
    }

    public static void setPref(Context c, String key, String val)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor e = s.edit();
        e.putString(key, val);
        e.apply();
    }
}
