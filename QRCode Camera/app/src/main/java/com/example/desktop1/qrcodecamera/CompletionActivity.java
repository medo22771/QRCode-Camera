package com.example.desktop1.qrcodecamera;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class CompletionActivity extends Activity
{
    Context context;
    TextView ErrorTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completion);

        context = getApplicationContext();
        ErrorTextView = (TextView)findViewById(R.id.ErrorTextView);

       // Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra("Downloaded Image");


//        File imgFile = new  File(Environment.getExternalStorageDirectory() + "/"
//                + "QRC Downloaded Filesz/gohary.png");
//
//        if(imgFile.exists())
//        {
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            sodmsd.setImageBitmap(myBitmap);
//        }

        try
        {
            String ImageCodeName = getIntent().getStringExtra("ImageCodeName");
            Bitmap bmp = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().toString() + "/"
                    + "QRC Downloaded Filesz/" + ImageCodeName);
            ImageView img = (ImageView)findViewById(R.id.Receipt);
            Log.i("HAMADA", "" + Environment.getExternalStorageDirectory().toString() + "/"
                    + "QRC Downloaded Filesz/" + ImageCodeName);
            img.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true));
        }
        catch(Exception e)
        {
            ErrorTextView.setText("CmpltnActv 0001: " + e.toString());
            ErrorTextView.setVisibility(View.VISIBLE);
            Log.i("HAMADA", "CmpltnActv 0001: " + e.toString());
        }
    }

    public void ReScan(View v)
    {
        Intent toMainActv= new Intent(context, com.example.desktop1.qrcodecamera.MainActivity.class);
        //toCmpltnActv.putExtra("Downloaded Image", Img);
        //toNewActivity.putExtra("GenerateDownloadURL", GenerateDownloadURL);
        context.startActivity(toMainActv);
    }
}
