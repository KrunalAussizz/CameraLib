package com.ieltstutorials.mylibrary.Gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ieltstutorials.mylibrary.R;

import java.io.File;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.Holder> {

    private List<Model_images> alImage;
    private ActivityGallery activityGallery;

    public AlbumAdapter(List<Model_images> alImage, ActivityGallery activityGallery) {
        this.alImage = alImage;
        this.activityGallery = activityGallery;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_album, parent, false);

        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
//        Uri myUri = Uri.parse(alImage.get(position));
        final Model_images images = alImage.get(position);
        if (images.getAl_imagepath().size() > 0) {
            Glide.with(activityGallery)
                    .load(new File(images.getAl_imagepath().get(0))) // Uri of the picture
                    .into(holder.ivAlbum);
            holder.tvAlbumName.setText(images.getStr_folder() + "(" + images.getAl_imagepath().size() + ")");
        } else {
            holder.ivAlbum.setImageDrawable(ContextCompat.getDrawable(activityGallery,R.drawable.ic_camera));
            holder.tvAlbumName.setText(images.getStr_folder());
        }

        holder.ivAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (images.getAl_imagepath().size() > 0) {
                    activityGallery.fillChildImage(images.getAl_imagepath());
                } else {
                    activityGallery.openCamera();
                }

            }
        });
//        holder.iv.setImageURI(myUri);
    }

    @Override
    public int getItemCount() {
        return alImage.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView ivAlbum;
        TextView tvAlbumName;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);
            tvAlbumName = itemView.findViewById(R.id.tvAlbumName);
        }
    }
}
