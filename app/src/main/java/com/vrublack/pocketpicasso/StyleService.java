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
                        onMediaChanged();
                        super.onChange(selfChange);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler())
                {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        Log.d("ScratchService", "Internal Media has been added");
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
            doFileUpload(latest);
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

    private void doFileUpload(File file)
    {
        new UploadTask().execute(file);
    }

    private class UploadTask extends AsyncTask<File, Void, Void>
    {
        @Override
        protected Void doInBackground(File... file)
        {
            System.out.println("Sending file to server: " + file[0]);

            String urlString = "http://52.91.8.206:5000/upload";
            try
            {
                int timeout = 60 * 10 * 1000; // 10 minutes
                HttpClient client = new DefaultHttpClient();
                HttpParams params = client.getParams();
                HttpConnectionParams.setConnectionTimeout(params, timeout);
                HttpConnectionParams.setSoTimeout(params, timeout);
                HttpPost post = new HttpPost(urlString);
                FileBody bin1 = new FileBody(file[0]);
                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("uploadedfile1", bin1);
                reqEntity.addPart("user", new StringBody("User"));
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) response.getEntity().getContent());
                setBackgroundPicture(bitmap);
            } catch (Exception ex)
            {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            }

            return null;
        }
    }

    private void setBackgroundPicture(Bitmap bitmap)
    {
        System.out.println("Setting background picture. " + (System.currentTimeMillis() + takenTime) / 1000 + " seconds elapsed.");

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