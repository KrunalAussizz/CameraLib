package com.ieltstutorials.mylibrary.Gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.ieltstutorials.mylibrary.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {

    List<String> alImage;
    ActivityGallery activityGallery;

    public ImageAdapter(List<String> alImage, ActivityGallery activityGallery) {
        this.alImage = alImage;
        this.activityGallery = activityGallery;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, final int position) {
//        Uri myUri = Uri.parse(alImage.get(position));
        Glide.with(activityGallery)
                .load(new File(alImage.get(position))) // Uri of the picture
                .into(holder.iv);

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityGallery.OpenImage(alImage.get(position));
            }
        });

//        holder.iv.setImageURI(myUri);
    }

    @Override
    public int getItemCount() {
        return alImage.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView iv;

        Holder(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
        }
    }
}
