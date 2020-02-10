package com.ieltstutorials.mylibrary.Gallery;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.ieltstutorials.mylibrary.Camera.ActivityCamera;
import com.ieltstutorials.mylibrary.PubFun;
import com.ieltstutorials.mylibrary.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityGallery extends AppCompatActivity {

    List<String> alImage = new ArrayList<>();
    boolean boolean_folder;
    RecyclerView rv, rvChild;
    ImageView ivMain;
    public static String cameraID;
    LinearLayout llImagePreview;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        rv = findViewById(R.id.rv);
        rvChild = findViewById(R.id.rvChild);
        llImagePreview = findViewById(R.id.llImagePreview);
        ivMain = findViewById(R.id.ivMain);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable drawable = (BitmapDrawable) ivMain.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                PubFun.click.btnClick(bitmap);
                finish();
            }
        });

        rv.setVisibility(View.VISIBLE);
        llImagePreview.setVisibility(View.GONE);
        rvChild.setVisibility(View.GONE);
        try {
            fn_imagespath();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void OpenImage(String url) {
        llImagePreview.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        rvChild.setVisibility(View.GONE);
        Glide.with(this)
                .load(new File(url)) // Uri of the picture
                .into(ivMain);

    }

    public void fillChildImage(ArrayList<String> al_imagepath) {
        rvChild.setLayoutManager(new GridLayoutManager(this, 4));
        ImageAdapter imageAdapter = new ImageAdapter(al_imagepath, this);
        rv.setVisibility(View.GONE);
        rvChild.setVisibility(View.VISIBLE);
        rvChild.setAdapter(imageAdapter);
    }

    @Override
    public void onBackPressed() {
        if (llImagePreview.getVisibility() == View.VISIBLE) {
            llImagePreview.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            rvChild.setVisibility(View.VISIBLE);
        } else if (rv.getVisibility() == View.VISIBLE) {
            finish();
        } else {
            rv.setVisibility(View.VISIBLE);
            llImagePreview.setVisibility(View.GONE);
            rvChild.setVisibility(View.GONE);
        }
    }

    private void AllImage() {
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            //Stores all the images from the gallery in Cursor
            Cursor cursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                    null, orderBy);
            //Total number of images
            assert cursor != null;
            int count = cursor.getCount();

            //Create an array to store path to all the images

            for (int i = 1; i <= count; i++) {
                cursor.moveToPosition(count - i);
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                //Store the path of the image
                alImage.add(cursor.getString(dataColumnIndex));
            }
            // The cursor should be freed up after use with close()
            cursor.close();
        }

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
//Stores all the images from the gallery in Cursor
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
//Total number of images
        int count = cursor.getCount();

//Create an array to store path to all the images


        for (int i = 1; i <= count; i++) {
            cursor.moveToPosition(count - i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            //Store the path of the image
            alImage.add(cursor.getString(dataColumnIndex));
        }// The cursor should be freed up after use with close()
        cursor.close();

        Log.e("TAG", "onCreate: ");
        ImageAdapter imageAdapter = new ImageAdapter(alImage, this);
        rv.setLayoutManager(new GridLayoutManager(this, 5));
        rv.setAdapter(imageAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = false;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            } else {
                isGranted = true;
            }
        }
        if (isGranted) {
            startActivity(new Intent(ActivityGallery.this, ActivityCamera.class));
        }
    }

    public void fn_imagespath() {
        ArrayList<Model_images> alAlbum = new ArrayList<>();
        alAlbum.clear();

        int int_position = 0;
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver()
                .query(uri, projection, null, null, orderBy + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            Log.e("Column", absolutePathOfImage);
            Log.e("Folder", cursor.getString(column_index_folder_name));

            for (int i = 0; i < alAlbum.size(); i++) {
                if (alAlbum.get(i).getStr_folder().equals(cursor.getString(column_index_folder_name))) {
                    boolean_folder = true;
                    int_position = i;
                    break;
                } else {
                    boolean_folder = false;
                }
            }


            if (boolean_folder) {

                ArrayList<String> al_path = new ArrayList<>();
                al_path.addAll(alAlbum.get(int_position).getAl_imagepath());
                al_path.add(absolutePathOfImage);
                alAlbum.get(int_position).setAl_imagepath(al_path);

            } else {
                ArrayList<String> al_path = new ArrayList<>();
                al_path.add(absolutePathOfImage);
                Model_images obj_model = new Model_images();
                obj_model.setStr_folder(cursor.getString(column_index_folder_name));
                obj_model.setAl_imagepath(al_path);

                alAlbum.add(obj_model);


            }
        }

        Collections.sort(alAlbum, new Comparator<Model_images>() {
            @Override
            public int compare(Model_images model_images, Model_images t1) {
                return model_images.getStr_folder().compareToIgnoreCase(t1.getStr_folder());
            }
        });

        alAlbum.add(0, new Model_images("Camera", new ArrayList<String>()));
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        AlbumAdapter albumAdapter = new AlbumAdapter(alAlbum, this);
        rv.setAdapter(albumAdapter);
    }


    public void openCamera() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_camera, null);
        ImageView ivFront = layout.findViewById(R.id.ivFront);
        ImageView ivBack = layout.findViewById(R.id.ivBack);
        ivFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {


                    cameraID = "1";
                    alertDialog.dismiss();
                    checkCameraPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*check is camera option selected to capture image for permission dialog*/
                try {

                    cameraID = "0";
                    alertDialog.dismiss();
                    checkCameraPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        alertDialog.setView(layout);
        alertDialog.show();
        Window window = alertDialog.getWindow();
        Point size = new Point();
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        window.setLayout((int) (width * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

    }

    private void checkCameraPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(ActivityGallery.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    && (ActivityCompat.shouldShowRequestPermissionRationale(ActivityGallery.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
                startActivity(new Intent(ActivityGallery.this, ActivityCamera.class));

            } else {
                ActivityCompat.requestPermissions(ActivityGallery.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        100);
            }
        } else {
            startActivity(new Intent(ActivityGallery.this, ActivityCamera.class));

            Log.e("Else", "Else");
        }
    }

}
