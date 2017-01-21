package com.vrublack.pocketpicasso;


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.io.File;

public class Util
{
    // convenience method
    public static String getPrefS(Context c, String key)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        return s.getString(key, "");
    }

    // convenience method
    public static boolean getPrefB(Context c, String key)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        return s.getBoolean(key, false);
    }

    public static void setPref(Context c, String key, String val)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor e = s.edit();
        e.putString(key, val);
        e.apply();
    }

    public static void setPref(Context c, String key, boolean val)
    {
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor e = s.edit();
        e.putBoolean(key, val);
        e.apply();
    }

    public static File getLatestFilefromDir(String dirPath)
    {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
        {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++)
        {
            if (lastModifiedFile.lastModified() < files[i].lastModified())
            {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public static int notifyOngoing(Context c, String title, String message)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c)
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setOngoing(true);
        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        final int id = message.hashCode();
        mNotificationManager.notify(id, mBuilder.build());
        return id;
    }

    public static void cancelNotif(Context c, int id)
    {
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static void cancelAllNotif(Context c)
    {
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
