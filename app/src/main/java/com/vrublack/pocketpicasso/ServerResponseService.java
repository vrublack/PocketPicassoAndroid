package com.vrublack.pocketpicasso;


import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerResponseService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        String fname = remoteMessage.getData().get("result");
        String paintingName = remoteMessage.getData().get("painting-name");
        ServerHandler serverHandler = new ServerHandler(this);
        Bitmap pic = serverHandler.loadPic(fname);
        setBackgroundPicture(pic);
        saveToFile(pic, getFilename(paintingName));
        Util.cancelAllNotif(this);
    }

    private String getFilename(String paintingName)
    {
        String[] comps = paintingName.split("/");
        paintingName = comps[comps.length - 1];
        int random = (int) (Math.random() * 100000);
        return random + "_" + paintingName;
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

    private void saveToFile(Bitmap bitmap, String fname)
    {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PocketPicasso");

        if (!folder.exists())
            folder.mkdirs();

        File f = new File(folder, fname);

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
