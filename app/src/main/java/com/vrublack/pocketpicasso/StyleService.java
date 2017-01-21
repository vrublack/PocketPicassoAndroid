package com.vrublack.pocketpicasso;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;


public class StyleService extends Service
{
    private String lastUploaded = null;
    private ServerHandler serverHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Util.getPrefB(this, "auto-upload"))
            registerContentObserver();

        serverHandler = new ServerHandler(this);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void registerContentObserver()
    {
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler())
                {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        Log.d("ScratchService", "External Media has been added");
                        Debug.passDebugNotification(StyleService.this, "External media added");
                        onMediaChanged();
                        super.onChange(selfChange);
                    }
                }
        );
    }

    private void onMediaChanged()
    {
        // find out what changed (if at all)
        final String cameraFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
        File latest = Util.getLatestFilefromDir(cameraFolder);
        if (!latest.toString().equals(lastUploaded))
        {
            // picture was added
            serverHandler.uploadImage(latest);
        } else
        {
            Debug.passDebugNotification(StyleService.this, "No new image to be added");
        }
    }
}