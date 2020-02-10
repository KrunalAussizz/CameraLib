package com.ieltstutorials.mylibrary.Camera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ieltstutorials.mylibrary.R;

public class ImagePreviewActivity extends AppCompatActivity {

    ImageView ivPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        ivPreview = findViewById(R.id.ivPreview);
        Bundle bundle = getIntent().getExtras();
        String img = bundle.getString("Image");

        Glide.with(ImagePreviewActivity.this)
                .load(img) // Uri of the picture
                .into(ivPreview);
    }
}
