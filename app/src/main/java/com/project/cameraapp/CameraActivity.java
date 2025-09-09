package com.project.cameraapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.net.Uri; // Needed for Uri.fromFile
import android.os.Bundle;
import android.os.Environment; // Needed for Environment.DIRECTORY_PICTURES
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button captureButton;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.btn_capture); // Get reference to capture button

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (permission.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                continue; // Skip WRITE_EXTERNAL_STORAGE check for Android 10+
            }

            if (ContextCompat.checkSelfPermission(
                    this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user. Closing camera.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider); // Updated method name to reflect multiple use cases
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                Toast.makeText(this, "Error starting camera.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .build();

        try {
            cameraProvider.unbindAll();

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            Log.d(TAG, "Camera preview and ImageCapture bound successfully.");
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
            Toast.makeText(this, "Failed to bind camera use cases.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void takePhoto() {
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture not initialized.");
            Toast.makeText(this, "Camera not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg";


        File outputDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraApp");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        File photoFile = new File(outputDirectory, fileName);


        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = outputFileResults.getSavedUri() != null ? outputFileResults.getSavedUri() : Uri.fromFile(photoFile);
                        String msg = "Photo capture succeeded: " + savedUri;
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        // TODO: Update recent capture thumbnail in Stage 5
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        String msg = "Photo capture failed: " + exc.getMessage();
                        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, msg, exc);
                    }
                }
        );
    }
}