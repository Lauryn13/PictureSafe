package com.example.picturesafe.components;

import android.widget.EditText;

import androidx.cardview.widget.CardView;

public class PictureSafeEditText {
    EditText editText;
    CardView cardView;
    boolean constantVisible;

    public PictureSafeEditText(EditText editText, CardView cardView, boolean constantVisible) {
        this.editText = editText;
        this.cardView = cardView;
        this.constantVisible = constantVisible;

        this.change_visibility(constantVisible);
    }
    public PictureSafeEditText(EditText editText, CardView cardView){
        this(editText, cardView, false);
    }

    public void change_visibility(boolean visible){
        if(visible) {
            this.editText.setVisibility(EditText.VISIBLE);
            this.cardView.setVisibility(CardView.VISIBLE);
        }
        else{
            this.editText.setVisibility(EditText.GONE);
            this.cardView.setVisibility(EditText.GONE);
        }
    }

    public void showEdit(String text) {
        this.change_visibility(true);
        this.editText.setText(text);
    }
    public void showEdit(){
        this.showEdit("");
    }

   public String readText() {
       return this.editText.getText().toString();
   }
}
