package com.vrublack.pocketpicasso;

import android.app.Activity;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class StyleService extends Service
{
    private String lastUploaded = null;

    private long takenTime;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        registerContentObserver();

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
        File latest = getLatestFilefromDir(cameraFolder);
        if (!latest.toString().equals(lastUploaded))
        {
            takenTime = System.currentTimeMillis();

            // picture was added
            ServerHandler.uploadImage(latest, Util.getPref(this, Const.FCM_TOKEN));
        }
    }

    private File getLatestFilefromDir(String dirPath)
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
}