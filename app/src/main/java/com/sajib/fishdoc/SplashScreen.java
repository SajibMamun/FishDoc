package com.sajib.fishdoc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {


    public ImageView nextbtnid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);




    }

    public void Nextbtnclicked(View view) {
        Intent intent=new Intent(SplashScreen.this,Tips.class);
        startActivity(intent);
    }
}