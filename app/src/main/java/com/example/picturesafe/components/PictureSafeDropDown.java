package com.example.picturesafe.components;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.picturesafe.R;
import com.example.picturesafe.enumerators.CompressionDropdown;

public class PictureSafeDropDown {
    public Spinner spinner;
    Context context;

    public PictureSafeDropDown(Context context, Spinner spinner, boolean visible) {
        this.context = context;
        this.spinner = spinner;

        change_visibility(visible);
        setItems();
    }
    public PictureSafeDropDown(Context context, Spinner spinner) {
        this(context, spinner, false);
    }

    public void setItems() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                CompressionDropdown.get_all_strings()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinner.setAdapter(adapter);
    }

    public void change_visibility(boolean visible) {
        this.spinner.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public CompressionDropdown getSelectedItem() {
        return CompressionDropdown.from_text(this.spinner.getSelectedItem().toString());
    }

    public void reset_selected_item(){
        this.spinner.setSelection(0);
    }
}
