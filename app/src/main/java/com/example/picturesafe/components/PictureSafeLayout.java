package com.example.picturesafe.components;

import android.widget.LinearLayout;

public class PictureSafeLayout {
    LinearLayout layout;

    public PictureSafeLayout(LinearLayout layout, boolean visible){
        this.layout = layout;

        if(visible) {
            this.layout.setVisibility(LinearLayout.VISIBLE);
        }
        else{
            this.layout.setVisibility(LinearLayout.GONE);
        }
    }
    public PictureSafeLayout(LinearLayout layout){
        this(layout, false);
    }

    public void change_visibility(boolean visible){
        if(visible) {
            this.layout.setVisibility(LinearLayout.VISIBLE);
        }
        else{
            this.layout.setVisibility(LinearLayout.GONE);
        }
    }
}
