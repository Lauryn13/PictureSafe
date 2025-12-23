package com.example.picturesafe.components;

import android.content.Context;
import android.widget.Button;

import com.example.picturesafe.R;

public class PictureSafeButton{
    public Button button;
    Context context;

    public PictureSafeButton(Context context, Button button, boolean visible){
        super();
        this.button = button;
        this.context = context;

        if(visible){
            this.button.setVisibility(Button.VISIBLE);
        }
        else{
            this.button.setVisibility(Button.GONE);
        }
    }
    public PictureSafeButton(Context context, Button button){
        this(context, button, false);
    }


    public void change_visibility(boolean visible){
        if(visible){
            this.button.setVisibility(Button.VISIBLE);
        }
        else{
            this.button.setVisibility(Button.GONE);
        }
    }

    public void set_highlight(boolean is_highlighted){
        if(is_highlighted) {
            this.button.setBackgroundColor(context.getColor(R.color.secondary));
        }
        else{
            this.button.setBackgroundColor(context.getColor(R.color.primary));
        }
    }
}
