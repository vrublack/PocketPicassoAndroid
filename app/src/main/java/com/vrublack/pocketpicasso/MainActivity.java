package com.vrublack.pocketpicasso;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 1;

    private String userChoosenTask;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        startService(new Intent(this, StyleService.class));

        Button uploadLatest = (Button) findViewById(R.id.upload_latest);
        uploadLatest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String cameraFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
                File latest = Util.getLatestFilefromDir(cameraFolder);
                ServerHandler sh = new ServerHandler(MainActivity.this);
                sh.uploadImage(latest);
            }
        });

        Button chooseImage = (Button) findViewById(R.id.choose_image);
        chooseImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectImage();
            }
        });

        Switch autoUpload = (Switch) findViewById(R.id.auto_upload);
        autoUpload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                Util.setPref(MainActivity.this, "auto-upload", isChecked);
            }
        });
    }

    private void selectImage()
    {
        final CharSequence[] items = {"Take Photo", "Choose from Library"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int item)
            {
                boolean result = checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo"))
                {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library"))
                {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                }
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else
                {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                return false;
            } else
            {
                return true;
            }
        } else
        {
            return true;
        }
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
        {
            File f = null;
            if (requestCode == SELECT_FILE)
                f = onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                f = onCaptureImageResult(data);

            if (f != null)
            {
                ServerHandler sh = new ServerHandler(MainActivity.this);
                sh.uploadImage(f);
            } else
            {
                Debug.d("f is null");
            }
        }
    }

    private File onCaptureImageResult(Intent data)
    {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        return writeToFile(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private File onSelectFromGalleryResult(Intent data)
    {
        Bitmap bm = null;
        if (data != null)
        {
            try
            {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                return writeToFile(bm);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static File writeToFile(Bitmap bmp)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try
        {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return destination;
    }
}
