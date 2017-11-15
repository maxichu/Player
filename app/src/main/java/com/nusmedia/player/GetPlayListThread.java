package com.nusmedia.player;

import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class GetPlayListThread extends Thread
{

    /*
            This thread class is to get playlist from the server
    */

//    private String targetURL="http://192.168.1.102:88/listServer.php";
    private String targetURL="http://monterosa.d2.comp.nus.edu.sg/~team02/listServer.php";
    private Handler handler;

    public GetPlayListThread(Handler handler)
    {
        this.handler=handler;
    }

    @Override
    public void run()
    {
        String videoNames=getJsonPlayList(targetURL);

        Message msg=new Message();
        msg.what=0;
        msg.obj=videoNames;
        handler.sendMessage(msg);

    }

    public static String getJsonPlayList(String path)
    {

        /*
                Get playlist data from server and return in JSON format
        */

        try {
            URL url = new URL(path.trim());

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            if(200 == urlConnection.getResponseCode())
            {

                InputStream is =urlConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while(-1 != (len = is.read(buffer))){
                    baos.write(buffer,0,len);
                    baos.flush();
                }

                String jsonString=baos.toString("utf-8");
                return jsonString.substring(4,jsonString.length()-1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


}
