package com.example.galleryapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(v -> {
            if (checkPermission()) {
                uploadImages();
            } else {
                requestPermission();
            }
        });
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            uploadImages();
        } else {
            Toast.makeText(this, "الصلاحية مطلوبة", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImages() {
        List<Uri> imageUris = getImagesFromGallery();
        if (imageUris.isEmpty()) {
            Toast.makeText(this, "مفيش صور في الاستوديو", Toast.LENGTH_SHORT).show();
            return;
        }
        UploadService uploadService = new UploadService(this);
        uploadService.uploadImages(imageUris);
        Toast.makeText(this, "جاري رفع " + imageUris.size() + " صورة", Toast.LENGTH_SHORT).show();
    }

    private List<Uri> getImagesFromGallery() {
        List<Uri> uris = new ArrayList<>();
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        String.valueOf(id));
                uris.add(uri);
            }
            cursor.close();
        }
        return uris;
    }
}
