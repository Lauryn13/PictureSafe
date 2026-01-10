package com.example.picturesafe.components;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class PictureSafeCheckBox {
    public CheckBox checkBox;
    public TextView text;

    public PictureSafeCheckBox(TextView text, CheckBox checkBox, boolean visible) {
        this.checkBox = checkBox;
        this.text = text;

        change_visibility(visible);
    }

    public void change_visibility(boolean visible) {
        this.checkBox.setVisibility(visible ? View.VISIBLE : View.GONE);
        this.text.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }
}
