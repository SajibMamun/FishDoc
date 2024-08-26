package com.sajib.fishdoc;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int IMAGE_PICK = 100;
    private final int CAMERA_CAPTURE = 101;

    CardView layout1;
    ImageView imageView, camerabtn;

    Bitmap bitmap;
    Button predictbtn, againbtn;

    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageview);
        predictbtn = findViewById(R.id.predictbtn);
        againbtn = findViewById(R.id.againbtn);
        layout1 = findViewById(R.id.layout1);
        camerabtn = findViewById(R.id.camerabtn);


        yolov5TFLiteDetector = new Yolov5TFLiteDetector();
        yolov5TFLiteDetector.setModelFile("best-fp16.tflite");
        yolov5TFLiteDetector.initialModel(this);

        boxPaint.setStrokeWidth(3);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.RED);

        textPain.setTextSize(30);
        textPain.setColor(Color.BLUE);
        textPain.setStyle(Paint.Style.FILL_AND_STROKE);

        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout1.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
                }
            }
        });
    }

    public void selectImage(View view) {
        layout1.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }

    public void predict(View view) {
        againbtn.setVisibility(View.VISIBLE);

        ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(bitmap);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        for (Recognition recognition : recognitions) {
            if (recognition.getConfidence() > 0.4) {
                RectF location = recognition.getLocation();
                canvas.drawRect(location, boxPaint);
                canvas.drawText(recognition.getLabelName(), location.left, location.top, textPain);
            }
        }

        imageView.setImageBitmap(mutableBitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                predictbtn.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            predictbtn.setVisibility(View.VISIBLE);
        }
    }

    public void Againbtnclicked(View view) {
        imageView.setVisibility(View.GONE);
        layout1.setVisibility(View.VISIBLE);
        predictbtn.setVisibility(View.GONE);
        againbtn.setVisibility(View.GONE);
        imageView.setImageBitmap(null);
    }
}