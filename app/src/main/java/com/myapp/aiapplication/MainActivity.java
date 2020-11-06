package com.myapp.aiapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import android.text.method.ScrollingMovementMethod;

import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class MainActivity extends AppCompatActivity {





    Button GalleryButton,CameraButton;


    com.libRG.CustomTextView TextFound;
    ImageView ImageHolder;
    TextRecognizer recognizer;
    TextToSpeech ttobj;

    ImageButton SpeakNow;

    String TextRetrieved="";
    File pictureCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextFound=findViewById(R.id.TextFound);
        ImageHolder=findViewById(R.id.ImageHolder);
        recognizer = TextRecognition.getClient();
        SpeakNow=findViewById(R.id.SpeakButton);
//        TextFound.paddingTop
//        TextFound.setMovementMethod(new ScrollingMovementMethod()   );

         pictureCaptured= (File) getIntent().getExtras().get("Picture");


//        ImageHolder.setImageURI( Uri.fromFile(pictureCaptured));


        try {
            InputImage image =
                    InputImage.fromFilePath(getApplicationContext(), Uri.fromFile(pictureCaptured));

            Task<Text> result=ProcessImage(image);



        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("NOT FOUND FILE");
        }

        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        ttobj.setLanguage(Locale.US);



        SpeakNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttobj.speak(TextRetrieved, TextToSpeech.QUEUE_FLUSH, null);
            }
        });




    }


    ////////////////////Photo Returned //////////////////////////




    public Task<Text> ProcessImage(InputImage image){




        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                System.out.println("Testing :  "+visionText.getText());
                                String Results=visionText.getText();
                                Results=Results.replaceAll("\n", "\t"); // will not change text
                                Results=Results.replaceAll("\\n", "\\t"); // will not change text
                                System.out.println(Results);
                                TextFound.setText(Results);
                                TextRetrieved=visionText.getText();
                                DrawRectangles(pictureCaptured,visionText);



                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        System.out.println("Crash");
                                        e.printStackTrace();
                                    }
                                });

        return result;
    }




    //////////////////ON BACKPRESEDD ////
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (getApplicationContext(), CameraActivity.class);

        startActivity(intent);
        finish();
    }



    /////////////////////DRAW Rechtangles ///////////////
    public void DrawRectangles(File ImageURL,Text visionText){
        Bitmap bitmap = BitmapFactory.decodeFile(ImageURL.getAbsolutePath());


        try {
            ExifInterface exif = new ExifInterface(ImageURL.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
        }
        catch (Exception e) {
            System.out.println("ERRORR");
            e.printStackTrace();

        }


        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas cnvs=new Canvas(mutableBitmap);
        Paint paint=new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        for (int i=0;i<visionText.getTextBlocks().size();i++)
        {
            Rect Rectangle = visionText.getTextBlocks()  .get(i).getBoundingBox();
            cnvs.drawRect(Rectangle.left, Rectangle.top,Rectangle.right,Rectangle.bottom , paint);

        }


        ImageHolder.setImageBitmap(mutableBitmap);
    }






}