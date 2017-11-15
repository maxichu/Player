package com.nusmedia.player;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;


public class MPDDownloadThread extends Thread
{

    private String filename;
    private Handler handler;
    private String mode;
    private HashMap<String,List<String>> segmentHashMap;

    MPDDownloadThread(String filename,Handler handler, HashMap<String,List<String>> segmentHashMap)
    {
        this.filename=filename;
        this.handler=handler;
        this.segmentHashMap=segmentHashMap;
        if(filename.startsWith("[Live]"))
        {
            filename.substring(6);
            this.mode="LIVE";
        }
        else
        {
            this.mode="VIDEO";
        }
    }

    @Override
    public void run()
    {
        if(mode.equals("LIVE"))
        {
            try
            {
                LiveMPDDownload();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        else if(mode.equals("VIDEO"))
        {
            VideoMPDDownload();
        }
    }

    public void VideoMPDDownload()
    {
        while(HttpConnectionUtil.downloadFile("http://monterosa.d2.comp.nus.edu.sg/~team02/FileSendServer.php", "/storage/emulated/0/Android/data/com.nusmedia.player/downloads/",filename,"")==null)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Message msg=new Message();
        msg.what=1;
        msg.obj=filename;
        handler.sendMessage(msg);
    }

    public void LiveMPDDownload() throws InterruptedException
    {
        String lasttime=new SimpleDateFormat("yyyyMMddHHmmss").format(0);

        while(true)
        {
            File file = HttpConnectionUtil.downloadLiveMPD("http://monterosa.d2.comp.nus.edu.sg/~team02/LiveFileSendServer.php", "/storage/emulated/0/Android/data/com.nusmedia.player/downloads/",filename,lasttime);
            //lasttime=new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            //lasttime=new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            if(file==null)
                Thread.sleep(300);
            else
            {
                Message msg=new Message();
                msg.what=2;
                msg.obj=file;
                handler.sendMessage(msg);
                Thread.sleep(2500);
            }
        }
    }
}
