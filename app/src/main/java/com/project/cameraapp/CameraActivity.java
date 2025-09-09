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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button captureButton;
    private ImageView recentThumbnailImageView;

    private File outputDirectory;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.btn_capture);
        recentThumbnailImageView = findViewById(R.id.iv_recent_thumbnail);

        outputDirectory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CameraApp");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }


        if (allPermissionsGranted()) {
            startCamera();
            loadRecentThumbnail();
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

        recentThumbnailImageView.setOnClickListener(v -> {
            Toast.makeText(this, "Recent thumbnail clicked (Stage 5 functionality)", Toast.LENGTH_SHORT).show();
            // TODO: Implement viewing full image in a later stage if desired
        });
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {

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
                loadRecentThumbnail(); // New: Load thumbnail after permissions
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
                bindCameraUseCases(cameraProvider);
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
                        updateRecentThumbnail(photoFile);
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

    private void loadRecentThumbnail() {
        File mostRecentFile = findMostRecentImage(outputDirectory);
        if (mostRecentFile != null) {
            updateRecentThumbnail(mostRecentFile);
        } else {
            recentThumbnailImageView.setImageDrawable(null);
        }
    }

    private void updateRecentThumbnail(File imageFile) {
        int targetWidth = recentThumbnailImageView.getWidth();
        int targetHeight = recentThumbnailImageView.getHeight();

        if (targetWidth <= 0 || targetHeight <= 0) {
            targetWidth = 64; // Default thumbnail size
            targetHeight = 64;
        }

        Bitmap thumbnail = decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), targetWidth, targetHeight);
        if (thumbnail != null) {
            recentThumbnailImageView.setImageBitmap(thumbnail);
        } else {
            recentThumbnailImageView.setImageDrawable(null);
            Log.e(TAG, "Failed to create thumbnail for: " + imageFile.getAbsolutePath());
        }
    }

    private File findMostRecentImage(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase(Locale.US).endsWith(".jpg");
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        return files[0];
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;


            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}