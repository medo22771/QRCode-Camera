package com.example.desktop1.qrcodecamera;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
{
    SurfaceView cameraPreview;
    TextView txtResult;
    TextView ErrorTextView;
    BarcodeDetector QRCodeDetector;
    CameraSource cameraSRC;
    final int ReqCamPermission = 1001;

    String GenerateFIleUrl = "http://41.38.100.253:9090/TMS0000707/myresource/receipt?ch1=1&ch2=1&p1=";
    String GenerateDownloadURL = "http://41.38.100.253:8080/Upload_Download_Service/services/download1?p3=";
    String DownloadURL = null;
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This Is Used For API 23 +
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                        {
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 100);
        //This Is Used For API 23 +

        cameraPreview = (SurfaceView)findViewById(R.id.cameraPreview);
        txtResult = (TextView)findViewById(R.id.txtResult);
        ErrorTextView = (TextView)findViewById(R.id.ErrorTextView);

        QRCodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();

        if(!QRCodeDetector.isOperational())
        {
            Toast.makeText(getApplicationContext(), "Please Connect To Internet To Download necessary Files", Toast.LENGTH_LONG).show();
            ErrorTextView.setText("MainAct 0001: QRC Detector Not Operational");
            ErrorTextView.setVisibility(View.VISIBLE);
            Log.i("Error" , "MainAct 0001: QRC Detector Not Operational");
        }

        try
        {
            cameraSRC = new CameraSource.Builder(this,QRCodeDetector)
                    .setRequestedPreviewSize(640, 480).build();

            cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder)
                {
                    if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        //Request permission
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, ReqCamPermission);
                    }
                    try
                    {
                        cameraSRC.start(cameraPreview.getHolder());
                    }
                    catch(Exception e)
                    {   e.printStackTrace();    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder)
                {   cameraSRC.stop();   }
            });
        }
        catch(Exception e)
        {
            ErrorTextView.setText("MainAct 0002: " + e.toString());
            ErrorTextView.setVisibility(View.VISIBLE);
            Log.i("Error" , "MainAct 0002: " + e.toString());
        }

        try
        {
            QRCodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections)
                {
                    final SparseArray<Barcode> QRCodes = detections.getDetectedItems();
                    if(QRCodes.size() != 0)
                    {
                        txtResult.post(new Runnable() {
                            @Override
                            public void run()
                            {
                                String ImageCodeName = QRCodes.valueAt(0).displayValue;
                                //Create Vibration
                                Vibrator devVibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                devVibrator.vibrate(600);
                                //devVibrator.cancel();
                                //cameraSRC.start(cameraPreview.getHolder());

                                txtResult.setText(ImageCodeName);
                                QRCodeDetector.release();
                                GenerateFIleUrl = GenerateFIleUrl + ImageCodeName;
                                DownloadFun(ImageCodeName);
                            }
                        });
                    }
                }
            });
        }
        catch(Exception e)
        {
            ErrorTextView.setText("MainAct 0003: " + e.toString());
            ErrorTextView.setVisibility(View.VISIBLE);
            Log.i("Error" , "MainAct 0003: " + e.toString());
        }
    }

    public void DownloadFun(String ImageCodeName)
    {
        //Output = (TextView)findViewById(R.id.helloWorld);
        final String finalImgCodeName = ImageCodeName;
        try
        {
            final RequestQueue ReqQ = Volley.newRequestQueue(MainActivity.this);
            StringRequest StrReq = new StringRequest(Request.Method.GET, GenerateFIleUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            //Output.setText(response);
                            ReqQ.stop();
                            Log.i("Error","Here");
                            new DownloadTask(MainActivity.this, ErrorTextView,GenerateDownloadURL + response + "&p4=" + finalImgCodeName +".png");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //Output.setText(error.toString());
                    Log.i("Error","ssssssssssss");
                    ReqQ.stop();
                    String message = "Unknown Error";
                    if (error instanceof NetworkError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ServerError) {
                        message = "The server could not be found. Please try again after some time!!";
                    } else if (error instanceof AuthFailureError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof ParseError) {
                        message = "Parsing error! Please try again after some time!!";
                    } else if (error instanceof NoConnectionError) {
                        message = "Cannot connect to Internet...Please check your connection!";
                    } else if (error instanceof TimeoutError) {
                        message = "Connection TimeOut! Please check your internet connection.";
                    }
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                }
            });
            ReqQ.add(StrReq);
        }
        catch(Exception e)
        {
            ErrorTextView.setText("MainAct 0004: " + e.toString());
            ErrorTextView.setVisibility(View.VISIBLE);
            Log.i("Error" , "MainAct 0004: " + e.toString());
        }
    }

    

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case ReqCamPermission:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        //Request permission
                        return;
                    try
                    {
                        cameraSRC.start(cameraPreview.getHolder());
                    }
                    catch(Exception e)
                    {
                        ErrorTextView.setText("MainAct 0005: " + e.toString());
                        ErrorTextView.setVisibility(View.VISIBLE);
                        Log.i("Error" , "MainAct 0005: " + e.toString());
                        return;
                    }
                }
            }
            break;
        }
    }
}
