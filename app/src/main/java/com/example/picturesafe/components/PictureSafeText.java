package com.example.picturesafe.components;

import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class PictureSafeText {
    public TextView textView;
    public CardView cardView;

    public PictureSafeText(TextView textView, CardView cardView, boolean visible) {
        this.textView = textView;
        this.cardView = cardView;
        if(visible){
            this.textView.setVisibility(TextView.VISIBLE);
            this.cardView.setVisibility(CardView.VISIBLE);
        }
        else{
            this.textView.setVisibility(TextView.GONE);
            this.cardView.setVisibility(CardView.GONE);
        }
    }
    public PictureSafeText(TextView textView, CardView cardView){
        this(textView, cardView, false);
    }

    public void setText(String text) {
        textView.setVisibility(TextView.VISIBLE);
        textView.setText(text);
        cardView.setVisibility(CardView.VISIBLE);
    }

    public void removeText(){
        textView.setVisibility(TextView.GONE);
        textView.setText("");
        cardView.setVisibility(CardView.VISIBLE);
    }

}
