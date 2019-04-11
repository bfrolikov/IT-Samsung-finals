package com.example.bfrol.it_samsung_finals;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

public class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder> {
    private ArrayList<DocumentSnapshot> dataArray;

    public CustomRecyclerViewAdapter(ArrayList<DocumentSnapshot> dataArray) {
        this.dataArray = dataArray;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_with_rating, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {
        User loadedUser = dataArray.get(position).toObject(User.class);
        customViewHolder.usrDemands.setText(loadedUser.getDemands());
        customViewHolder.usrUsername.setText(loadedUser.getFirstName()+" "+loadedUser.getLastName());
        customViewHolder.usrRating.setRating(loadedUser.getRating());
        customViewHolder.usrProfileImage.setImageDrawable(customViewHolder.usrProfileImage.getContext().getDrawable(R.drawable.user_placeholder));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference("images/"+loadedUser.getProfileImageUrl());
        GlideApp.with(customViewHolder.usrProfileImage).load(reference).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(customViewHolder.usrProfileImage);//TODO caching!
    }

    @Override
    public int getItemCount() {
        return dataArray.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView usrUsername, usrDemands;
        RatingBar usrRating;
        ImageView usrProfileImage;

        CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            usrUsername = itemView.findViewById(R.id.usr_username);
            usrDemands = itemView.findViewById(R.id.usr_demands);
            usrRating = itemView.findViewById(R.id.usr_rating);
            usrProfileImage = itemView.findViewById(R.id.usr_profile_image);
        }
    }

    void setDataArray(ArrayList<DocumentSnapshot> dataArray) {
        this.dataArray = dataArray;
    }
}
