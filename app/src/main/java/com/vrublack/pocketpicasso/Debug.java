package com.vrublack.pocketpicasso;


import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Debug
{
    public static final boolean DEBUG = true;

    private static final String TAG = "PocketPicasso";

    public static void passDebugNotification(Context c, String message)
    {
        if (!DEBUG)
            return;

        d("Passing debug notification: " + message);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("PocketPicasso DEBUG")
                        .setContentText(message);
        NotificationManager mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        final int id = message.hashCode();
        mNotificationManager.notify(id, mBuilder.build());
    }

    public static void d(String msg)
    {
        Log.d(TAG, msg);
    }
}
