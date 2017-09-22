package com.abi.smartlogin.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abi.smartlogin.R;
import com.abi.smartlogin.db.DatabaseAccess;
import com.abi.smartlogin.entity.User;
import com.abi.smartlogin.neuralnetwork.FaceRecognizer;
import com.abi.smartlogin.util.ImageProcessor;
import com.abi.smartlogin.util.Utility;

import java.io.File;
import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "UserActivity";
    private static final int REQUEST_TAKE_PHOTO = 1889;
    private static final int REQUEST_PERMISSION_CODE = 1;
    private TextView txtWelcome;
    private ProgressBar progressBar;
    private String username;
    private User user;
    private DatabaseAccess databaseAccess;
    private String currentPhotoPath;
    private FaceRecognizer faceRecognizer = FaceRecognizer.getInstance();
    private ProgressDialog progressDialog;
    private ImageView imgUser;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        this.txtWelcome = (TextView) findViewById(R.id.txtWelcome);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.imgUser = (ImageView) findViewById(R.id.imgUser);

        this.databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        this.username = getIntent().getExtras().getString("username");
        this.txtWelcome.setText("Welcome " + this.username + "!");

        databaseAccess.open();
        this.user = databaseAccess.getUser(username);
        databaseAccess.close();

        this.progressBar.setProgress(this.user.getConfidence());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseAccess.open();
                User user = databaseAccess.getUser(username);
                databaseAccess.close();

                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        this.requestRuntimePermission();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
    }

    public void train(View view) {
        this.startCamera();
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
//        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            processImage(ImageProcessor.loadImage(currentPhotoPath));
//        } else {
//            Toast.makeText(this, "Please take a selfie and click ok", Toast.LENGTH_SHORT).show();
//        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            processImage(photo);
        } else {
            Toast.makeText(this, "Please take a selfie and click ok", Toast.LENGTH_SHORT).show();
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

    private void processImage(Bitmap bitmap) {

        // Detect face
        Bitmap processedImage = ImageProcessor.process(bitmap);

        if (processedImage != null) {

            imgUser.setImageBitmap(processedImage);
            txtWelcome.setText("Successfully detected a face");

            this.wakeLock.acquire();    // keep phone awake
            progressDialog = ProgressDialog.show(this, "Training", "Please wait...");
            new TrainTask().execute(processedImage);
        } else {
            imgUser.setImageResource(R.drawable.user);
            txtWelcome.setText("The photo must contain a single face but found");
        }
    }

    private class TrainTask extends AsyncTask<Bitmap, Integer, Double> {
        protected Double doInBackground(Bitmap... bitmaps) {
            // Train
            double error = 0.0;
            faceRecognizer.open(getApplicationContext());
            try {
                error = faceRecognizer.train(user.getId(), bitmaps[0]);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            } finally {
                try {
                    faceRecognizer.save();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                faceRecognizer.close();
            }
            return error;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Double result) {
            Log.i(TAG, "Error: " + result);
            int confidence = (int) ((1 - result) * 100);
            progressBar.setProgress(confidence);
            user.setConfidence(confidence);
            databaseAccess.open();
            databaseAccess.update(user);
            databaseAccess.close();
            wakeLock.release();
            progressDialog.hide();
            progressDialog.cancel();
        }
    }

}
