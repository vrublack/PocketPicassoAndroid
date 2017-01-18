package com.vrublack.pocketpicasso;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

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
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerHandler
{
    private static final String SERVER_IP = "52.90.143.95";

    public static Bitmap loadPic(String filename)
    {
        try
        {
            String url = "http://" + SERVER_IP + ":5000/static/tmp-images/" + filename;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e)
        {
            // Log exception
            return null;
        }
    }

    public static void uploadImage(File file, String fcmToken)
    {
        new UploadTask().execute(new Pair<File, String>(file, fcmToken));
    }

    private static HttpClient buildClient()
    {
        int timeout = 60 * 10 * 1000; // 10 minutes
        HttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
        return client;
    }

    /*
    public static void sendRegistrationToServer(String token)
    {
        HttpClient client = buildClient();
        String urlString = "http://" + SERVER_IP + ":5000/register_fcm_token";
        HttpPost post = new HttpPost(urlString);
        post.setHeader("fcm-token", token);
        try
        {
            client.execute(post);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }*/

    private static class UploadTask extends AsyncTask<Pair<File, String>, Void, Void>
    {
        @Override
        protected Void doInBackground(Pair<File, String>... p)
        {
            File file = p[0].first;
            String fcmToken = p[0].second;

            System.out.println("Sending file to server: " + file);

            String urlString = "http://" + SERVER_IP + ":5000/upload";
            try
            {
                HttpClient client = buildClient();
                HttpPost post = new HttpPost(urlString);
                FileBody bin1 = new FileBody(file);
                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("uploadedfile1", bin1);
                reqEntity.addPart("user", new StringBody("User"));
                post.setEntity(reqEntity);
                // also include fcm token so this device can receive a push notification when the server
                // has rendered the image
                post.setHeader(Const.FCM_TOKEN, fcmToken);
                client.execute(post);
            } catch (Exception ex)
            {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            }

            return null;
        }
    }

}
