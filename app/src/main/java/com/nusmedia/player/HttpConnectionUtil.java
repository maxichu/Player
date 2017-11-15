package com.nusmedia.player;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;


public class HttpConnectionUtil {

    /*
              This is a generic class of downloading tools.
    */

    public static File downloadFile(String urlPath, String downloadDir,String fileName, String segmentName)
    {

        /*
                This function is to download MPD and segment files in VIDEO mode
        */

        File file = null;
        try {

            URL url = new URL(urlPath);

            URLConnection urlConnection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.connect();

            String param="filename="+ URLEncoder.encode(fileName,"UTF-8");
            if(!segmentName.equals(""))  param+="&segmentname="+URLEncoder.encode(segmentName,"UTF-8");
            param+="&mode="+URLEncoder.encode("VIDEO","UTF-8");


            DataOutputStream dos=new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();

            int fileLength = httpURLConnection.getContentLength();

            String filePathUrl = httpURLConnection.getURL().getFile();
            String fileFullName = "";
            if(segmentName.equals(""))
            {
                fileFullName = fileName+".mpd";
            }
            else
            {
                fileFullName = fileName+"/"+segmentName;
            }


            URLConnection con = url.openConnection();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            String path = downloadDir + File.separatorChar + fileFullName;
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);

            }
            bin.close();
            out.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return file;
        }

    }

    public static File downloadLiveMPD(String urlPath, String downloadDir,String fileName,String lastTime)
    {

        /*
                This function is to download MPD file in LIVE mode.
        */

        File file = null;
        try
        {

            URL url = new URL(urlPath);

            URLConnection urlConnection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            httpURLConnection.setRequestMethod("POST");


            httpURLConnection.connect();

            String param = "filename=" + URLEncoder.encode(fileName.substring(7 ), "UTF-8");
            param+="&lasttime="+lastTime;

            DataOutputStream dos=new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();

            int fileLength = httpURLConnection.getContentLength();


            String filePathUrl = httpURLConnection.getURL().getFile();
            String fileFullName = fileName.substring(7)+".mpd";

            URLConnection con = url.openConnection();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            String path = downloadDir + File.separatorChar + fileFullName;
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);

            }
            bin.close();
            out.close();

        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
        return file;
    }



    public static File downloadLiveFile(String urlPath, String downloadDir,String fileName,String segmentName)
    {

        /*
                This function is to download segment files in LIVE mode
        */

        File file = null;
        try {

            URL url = new URL(urlPath);

            URLConnection urlConnection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.connect();

            String param="filename="+ URLEncoder.encode(fileName,"UTF-8");
            param+="&segmentname="+URLEncoder.encode(segmentName,"UTF-8");
            param+="&mode="+URLEncoder.encode("LIVE","UTF-8");


            DataOutputStream dos=new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(param);
            dos.flush();
            dos.close();


            int fileLength = httpURLConnection.getContentLength();


            String filePathUrl = httpURLConnection.getURL().getFile();
            String fileFullName = fileName+"/"+segmentName;



            URLConnection con = url.openConnection();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            String path = downloadDir + File.separatorChar + fileFullName;
            file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);

            }
            bin.close();
            out.close();
        }
        catch (MalformedURLException e)
        {

            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(file.length()==0) return null;
            return file;
        }

    }

}