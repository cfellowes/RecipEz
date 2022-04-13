package com.gocery.recipez.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gocery.recipez.R;
import com.gocery.recipez.activities.ScanResultsActivity;
import com.gocery.recipez.data.ScanResult;
import com.gocery.recipez.http.RequestCallback;
import com.gocery.recipez.http.ScanApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class ScanFragment extends Fragment implements View.OnClickListener, RequestCallback<ScanResult[]> {

    private static final int REQUEST_TAKE_PICTURE = 0;
    private static final int REQUEST_UPLOAD_PICTURE = 1;

    private View rootView;
    private Uri imageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        setOnClickListeners();

        return rootView;
    }

    private void setOnClickListeners() {
        rootView.findViewById(R.id.button_take_picture).setOnClickListener(this);
        rootView.findViewById(R.id.button_upload_picture).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.button_take_picture):
                onClickTakePicture();
                break;
            case (R.id.button_upload_picture):
                onClickUploadPicture();
                break;
        }
    }

    private void onClickTakePicture() {
        if (hasCameraPermissions()) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File imageFile = createImageFile(getContext());
                imageUri = FileProvider.getUriForFile(getContext(), "com.gocery.recipez.file_provider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            requestCameraPermissions();
        }
    }

    private boolean hasCameraPermissions() {
        if (getContext() != null) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void requestCameraPermissions() {
        if (getActivity() != null) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_TAKE_PICTURE);
        }
    }

    private File createImageFile(@Nullable Context context) throws IOException {
        if (context == null) {
            throw new IOException("Cannot create file from null context");
        }

        String fileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", directory);
    }

    private void onClickUploadPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_UPLOAD_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_UPLOAD_PICTURE) {
                imageUri = data.getData();
            }
            startReceiptScan();
        }
    }

    private void startReceiptScan() {
        if (getActivity() != null) {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                rootView.findViewById(R.id.layout_progress_bar).setVisibility(View.VISIBLE);
                ScanApi.getInstance().scanReceipt(inputStream, this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCompleteRequest(ScanResult[] data) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ScanResultsActivity.class);
            intent.putExtra("scanResults", data);
            startActivity(intent);

            rootView.findViewById(R.id.layout_progress_bar).setVisibility(View.GONE);
        }
        // TODO: delete the image file if created
    }
}