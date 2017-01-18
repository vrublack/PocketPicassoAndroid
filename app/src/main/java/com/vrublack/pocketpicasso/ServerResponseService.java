package com.vrublack.pocketpicasso;


import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

public class ServerResponseService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        String fname = remoteMessage.getData().get("result");
        Bitmap pic = ServerHandler.loadPic(fname);
        setBackgroundPicture(pic);
    }

    private void setBackgroundPicture(Bitmap bitmap)
    {
        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(getApplicationContext());
        try
        {
            myWallpaperManager.setBitmap(bitmap);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
