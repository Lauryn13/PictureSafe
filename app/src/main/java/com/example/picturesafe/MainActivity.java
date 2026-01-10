package com.example.picturesafe;

import com.example.picturesafe.fragments.AddFragment;
import com.example.picturesafe.fragments.ImageFragment;
import com.example.picturesafe.fragments.SavedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ImageFragment()).commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.nav_image) {
                fragment = new ImageFragment();
            } else if (item.getItemId() == R.id.nav_saved) {
                fragment = new SavedFragment();
            } else {
                fragment = new AddFragment();
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

            return true;
        });

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#2A2A2A"));
        actionBar.setBackgroundDrawable(colorDrawable);
    }
}