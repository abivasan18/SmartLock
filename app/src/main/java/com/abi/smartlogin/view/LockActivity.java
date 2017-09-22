package com.abi.smartlogin.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abi.smartlogin.R;
import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.neuralnetwork.FaceRecognizer;
import com.abi.smartlogin.util.ImageProcessor;
import com.abi.smartlogin.util.Utility;

import java.io.File;
import java.io.IOException;

public class LockActivity extends AppCompatActivity {

    private static final String TAG = "LockActivity";
    private static final int REQUEST_TAKE_PHOTO = 1888;
    private static final int REQUEST_PERMISSION_CODE = 1;
    private TextView txtInfo;
    private ImageView imgUser;
    private String currentPhotoPath;
    private FaceRecognizer faceRecognizer = FaceRecognizer.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lock);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        this.makeFullScreen();

        this.txtInfo = (TextView) findViewById(R.id.txtInfo);
        this.imgUser = (ImageView) findViewById(R.id.imgUser);
        this.requestRuntimePermission();
    }

    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    public void login(View view) {
        this.startCamera();
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    private void startCamera() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = Utility.createImageFile(this);
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//                Log.e(TAG, "Error occurred while creating the File", ex);
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                this.currentPhotoPath = photoFile.getAbsolutePath();
//                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//        }
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            processImage(ImageProcessor.loadImage(currentPhotoPath));
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            processImage(photo);
        } else {
            Toast.makeText(LockActivity.this, "Please try again!", Toast.LENGTH_SHORT).show();
            imgUser.setImageResource(R.drawable.user);
            txtInfo.setText("Please try again");
        }
    }

    private void processImage(Bitmap bitmap) {

        // Detect face
        Bitmap processedImage = ImageProcessor.process(bitmap);

        if (processedImage != null) {

            imgUser.setImageBitmap(processedImage);
            txtInfo.setText("Successfully detected a face");
            progressDialog = ProgressDialog.show(this, "Searching", "Please wait...");
            new DetectTask().execute(processedImage);

        } else {
            imgUser.setImageResource(R.drawable.user);
            Toast.makeText(this, "The photo must contain a single face", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do nothing
            } else {
                Toast.makeText(this, "Please provide the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class DetectTask extends AsyncTask<Bitmap, Integer, User> {
        protected User doInBackground(Bitmap... bitmaps) {
            // Train
            User user = null;
            faceRecognizer.open(getApplicationContext());
            try {
                user = faceRecognizer.find(bitmaps[0]);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                faceRecognizer.close();
            }
            return user;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(User result) {
            progressDialog.hide();
            progressDialog.cancel();
            if (result == null) {
                Toast.makeText(LockActivity.this, "Failed to recognize you. Please try again!", Toast.LENGTH_SHORT).show();
                imgUser.setImageResource(R.drawable.user);
                txtInfo.setText("Please try again");
            } else {
                Utility.setWallpaper(LockActivity.this, result.getWallpaperId());
                Toast.makeText(LockActivity.this, "Welcome " + result.getUsername() + "!", Toast.LENGTH_SHORT).show();
                ExitActivity.exitApplication(getApplicationContext());
            }
        }
    }
}
