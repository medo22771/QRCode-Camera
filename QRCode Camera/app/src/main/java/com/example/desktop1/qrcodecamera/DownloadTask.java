package com.example.desktop1.qrcodecamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadTask
{
    Bitmap Img = null;
    private static final String TAG = "Download Task";
    private Context context;
    private TextView ErrorTextView;
    private String downloadUrl = "";
    private String downloadFileName = "";
    private ProgressDialog pDialog;

    public DownloadTask(Context context, TextView ErrorTV, String downloadUrl)
    {
        this.context = context;
        this.ErrorTextView = ErrorTV;
        this.downloadUrl = downloadUrl;

        //Create file name by picking download file name from URL
        downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf( '=' ) + 1,downloadUrl.length());

        //Start Downloading Task
        new DownloadingTask().execute();
    }

    public boolean isSDCardPresent()
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void>
    {
        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute() {
            pDialog=new ProgressDialog(context);
            pDialog.setMessage("Downloading...");
            pDialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null)
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                            Toast.makeText(context, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }, 400);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            Intent toCmpltnActv = new Intent(context, com.example.desktop1.qrcodecamera.CompletionActivity.class);
                            //toCmpltnActv.putExtra("Downloaded Image", Img);
                            toCmpltnActv.putExtra("ImageCodeName", downloadFileName);
                            context.startActivity(toCmpltnActv);
                        }
                    }, 1200);
                }
                else
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();

            }
            catch (Exception e)
            {
                ErrorTextView.setText("DwnldTask 0001: " + e.toString());
                ErrorTextView.setVisibility(View.VISIBLE);
                Log.i("Error" , "DwnldTask 0001: " + e.toString());
            }
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            Log.i("Passed" , "DownloadTask 00012: ");
            try
            {
                //Create Download URl
                URL url = new URL(downloadUrl);

                //Open Url Connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //Set Request Method to "GET" since we are grtting data
                conn.setRequestMethod("GET");

                //connect the URL Connection
                conn.connect();

                //If Connection response is not OK then show Logs
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Server returned HTTP " + conn.getResponseCode()
                            + " " + conn.getResponseMessage());
                }


                //Get File if SD card is present
                if (isSDCardPresent())
                {
                    apkStorage = new File(Environment.getExternalStorageDirectory() + "/"
                                    + "QRC Downloaded Filesz");
                }
                else
                    Toast.makeText(context, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                //If File is not present create directory
                if (!apkStorage.exists())
                {
                    apkStorage.mkdirs();
                    Log.e(TAG, "Directory Created.");
                }

                //Create Output file in Main File (Location, FileName)
                outputFile = new File(apkStorage, downloadFileName);

                //Create New File if not present
                if (!outputFile.exists())
                {
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                }

                //Get OutputStream for NewFile Location
                FileOutputStream DownloadedFile = new FileOutputStream(outputFile);

                //Get InputStream for connection
                InputStream ReturnedData = conn.getInputStream();

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = ReturnedData.read(buffer)) != -1) {
                    DownloadedFile.write(buffer, 0, len1);//Write new file
                }

                //Close all connection after doing task
                DownloadedFile.close();
                ReturnedData.close();

            }
            catch (Exception e)
            {
                outputFile = null;
                ErrorTextView.setText("DwnldTask 0002: " + e.toString());
                ErrorTextView.setVisibility(View.VISIBLE);
                Log.i("Error" , "DwnldTask 0002: " + e.toString());
            }

            return null;
        }
    }
}
