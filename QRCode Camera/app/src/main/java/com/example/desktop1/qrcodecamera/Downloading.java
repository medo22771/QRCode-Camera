package com.example.desktop1.qrcodecamera;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Downloading
{
    public Downloading(String downloadUrl, String downloadFileName)
    {
        Log.i("HERE" , downloadUrl);
        final String TAG = "wow";
        File apkStorage = null;
        File outputFile = null;

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
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                Log.i(TAG, "Server returned HTTP " + conn.getResponseCode()
                        + " " + conn.getResponseMessage());

            }


            //Get File if SD card is present
            if (isSDCardPresent())
            {

                apkStorage = new File(
                        Environment.getExternalStorageDirectory() + "/"
                                + "QRC Downloaded Files");
            } else
                Log.i(TAG, "Download Failed");

            //If File is not present create directory
            if (!apkStorage.exists())
            {
                apkStorage.mkdir();
                Log.i(TAG, "Directory Created.");
            }

            //Create Output file in Main File (Location, FileName)
            outputFile = new File(apkStorage, downloadFileName);

            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                Log.i(TAG, "File Created");
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

            //Read exception if something went wrong
            e.printStackTrace();
            outputFile = null;
            Log.i(TAG, "Download Error Exception " + e.getMessage());
        }
    }

    public boolean isSDCardPresent()
    {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
}
