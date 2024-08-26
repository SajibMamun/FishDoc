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
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final int IMAGE_PICK = 100;
    private final int CAMERA_CAPTURE = 101;


    ImageView imageView, camerabtn;


    Bitmap bitmap;
    Button predictbtn, againbtn;
    LinearLayout layout1card, layout2img, layout3scroll;
    TextView detecclasstv, suggestiontv;

    Yolov5TFLiteDetector yolov5TFLiteDetector;
    Paint boxPaint = new Paint();
    Paint textPain = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageview);
        againbtn = findViewById(R.id.againbtn);
        layout1card = findViewById(R.id.layout1);
        layout2img = findViewById(R.id.layout2image);
        layout3scroll = findViewById(R.id.layout3scroll);
        camerabtn = findViewById(R.id.camerabtn);

        detecclasstv = findViewById(R.id.classnametv);
        suggestiontv = findViewById(R.id.suggestionstv);


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
                layout1card.setVisibility(View.GONE);
                layout2img.setVisibility(View.VISIBLE);
                layout3scroll.setVisibility(View.GONE);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
                }
            }
        });


    }

    public void selectImage(View view) {
        layout1card.setVisibility(View.GONE);
        layout2img.setVisibility(View.VISIBLE);
        layout3scroll.setVisibility(View.GONE);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageView.setImageBitmap(bitmap);
                //predictbtn.setVisibility(View.VISIBLE);

                ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(bitmap);
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);

                StringBuilder detectedClasses = new StringBuilder();
                StringBuilder suggestions = new StringBuilder();

                for (Recognition recognition : recognitions) {
                    if (recognition.getConfidence() > 0.4) {
                        RectF location = recognition.getLocation();
                        canvas.drawRect(location, boxPaint);
                        canvas.drawText(recognition.getLabelName(), location.left, location.top, textPain);
                        String className = recognition.getLabelName();
                        detectedClasses.append(recognition.getLabelName()).append("\n");
                        // Get suggestion for each detected class and append it
                        suggestions.append(getSuggestionForClass(className)).append("\n");
                    }
                }

                imageView.setImageBitmap(mutableBitmap);
                layout3scroll.setVisibility(View.VISIBLE);

                // Set the detected class names to the TextView
                detecclasstv.setText(detectedClasses.toString().trim());
                // Set the suggestions to the suggestion TextView
                suggestiontv.setText(suggestions.toString().trim());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        } else if (requestCode == CAMERA_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            //predictbtn.setVisibility(View.VISIBLE);
            ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(bitmap);
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);


            StringBuilder detectedClasses = new StringBuilder();
            StringBuilder suggestions = new StringBuilder();

            for (Recognition recognition : recognitions) {
                if (recognition.getConfidence() > 0.4) {
                    RectF location = recognition.getLocation();
                    canvas.drawRect(location, boxPaint);
                    canvas.drawText(recognition.getLabelName(), location.left, location.top, textPain);
                    String className = recognition.getLabelName();
                    detectedClasses.append(recognition.getLabelName()).append("\n");
                    // Get suggestion for each detected class and append it
                    suggestions.append(getSuggestionForClass(className)).append("\n");
                }
            }

            imageView.setImageBitmap(mutableBitmap);
            layout3scroll.setVisibility(View.VISIBLE);

            // Set the detected class names to the TextView
            detecclasstv.setText(detectedClasses.toString().trim());
            // Set the suggestions to the suggestion TextView
            suggestiontv.setText(suggestions.toString().trim());


        }
    }


    private String getSuggestionForClass(String className) {
        switch (className.toLowerCase()) {
            case "cloudy eye":
                return "Cloudy Eye: Make sure that water changes are made with water at about the same temperature as your tank. Additionally, check your pH, especially if your water is soft. Other possible contributing factors are: stress, malnutrition (lack of vitamin A,) injury to the eye, old age (cataracts).\n";
            case "eus":
                return "EUS: Control of EUS in natural waters is probably impossible. In outbreaks occurring in small, closed water- bodies or fish ponds, liming water with agricultural limes and improving water quality, together with removal of infected fish, is often effective in reducing mortalities and controlling the disease.\n";
            case "fin rot":
                return "Fin Rot: Fin rot can be prevented with good water quality, feeding fresh food in small portions and maintaining constant water temperature. Keeping the tank from becoming cluttered (for domestic fish) will also help prevent fin rot.\n";
            case "gill flukes":
                return "Gill Flukes: Provide your fish with a low-stress environment. This includes good water quality and maintenance practices, feeding a proper, nutritious diet, and adhering to strict quarantine protocols for all new additions to the tank.\n";
            case "hexamita":
                return "Hexamita: Taking care that fish will dispose of and keep sufficient resistance. It is absolutely necessary to feed the fish in your pond on high grade fish feed.\n";
            default:
                return "Disease Information Not Available. Stay Connect With Us For Future Update.\n";
        }
    }


    public void Againbtnclicked(View view) {
        layout2img.setVisibility(View.GONE);
        layout1card.setVisibility(View.VISIBLE);
        layout3scroll.setVisibility(View.GONE);
        imageView.setImageBitmap(null);
    }
}