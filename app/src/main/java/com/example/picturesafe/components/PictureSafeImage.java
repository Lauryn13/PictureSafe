package com.example.picturesafe.components;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

public class PictureSafeImage {
    ImageView imageView;
    CardView cardView;
    boolean constantVisible;

    public PictureSafeImage(ImageView imageView, CardView cardView, boolean constantVisible){
        this.imageView = imageView;
        this.cardView = cardView;
        this.constantVisible = constantVisible;

        this.changeVisibility(constantVisible);
    }
    public PictureSafeImage(ImageView imageView, CardView cardView){
        this(imageView, cardView, false);
    }

    public void changeVisibility(boolean visible){
        if(visible) {
            this.imageView.setVisibility(ImageView.VISIBLE);
            this.cardView.setVisibility(CardView.VISIBLE);
        }
        else{
            this.imageView.setVisibility(ImageView.GONE);
            this.cardView.setVisibility(CardView.GONE);
        }
    }

    public void setImage(Bitmap bitmap){
        this.changeVisibility(true);
        this.imageView.setImageBitmap(bitmap);
    }

    public void removeImage(){
        if(!this.constantVisible){
            this.changeVisibility(false);
            this.imageView.setImageBitmap(null);
        }
        else{
            this.imageView.setImageBitmap(null);
        }
    }

    public void changeImage(Bitmap bitmap){
        this.imageView.setImageBitmap(bitmap);
    }
}
