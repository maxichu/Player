package com.nusmedia.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.ListViewCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements SurfaceHolder.Callback
{

    private MediaPlayer
            nextMediaPlayer,        // player right after this segment finished
            cacheMediaPlayer,      // temp player when adding segment to cache
            currentMediaPlayer;  // the media player  playing now or is about to play

    private String mode;

    private ListView listView;
    private List<String> videoNamesList=new ArrayList<String>();
    private List<String> segmentList=new ArrayList<String>();
    private HashMap<String,List<String>> segmentHashMap=new HashMap<String,List<String>>();
    private int SegmentReceived=0;
    private String videoQuality="low";

    private PopupWindow window;
    private MyAdapter adapter;

    private String mNextVideoAbsolutePath;

    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;

    private ArrayList<String> VideoListQueue=new ArrayList<String>();  //video Urls to be played
    private HashMap<String,MediaPlayer> playersCache=new HashMap<String,MediaPlayer>();  // video cache list
    private int currentVideoIndex=0;
    private int currentPosition;

    private SegmentGetThread segmentGetThread=null;
    private LiveSegmentGetThread liveSegmentGetThread=null;

    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    String videoNames=(String)msg.obj;
                    videoNamesList=getListString(videoNames);

                    break;
                case 1: // have gotten mpd file
                    try
                    {
                        String fileName=(String)msg.obj;
                        Toast.makeText(MainActivity.this, "Start to play: "+fileName, Toast.LENGTH_SHORT)
                                .show();
                        segmentHashMap=DomMPDService.readXML(fileName);
                        segmentGetThread=new SegmentGetThread();
                        segmentGetThread.start();
                    }
                    catch (Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }
                    break;

                case 2: // have gotten latest MPD of Live
                    try
                    {
                        File MPD_file=(File)msg.obj;
                        segmentHashMap=DomMPDService.UpdateXML(segmentHashMap, MPD_file);
                        Toast.makeText(MainActivity.this, String.valueOf(segmentHashMap.get("high").size()), Toast.LENGTH_SHORT)
                                .show();
                        if(liveSegmentGetThread==null)
                            liveSegmentGetThread=new LiveSegmentGetThread();
                        liveSegmentGetThread.start();
                    }
                    catch (Throwable throwable)
                    {
                        throwable.printStackTrace();
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new GetPlayListThread(handler).start();
        ListView();
        initView();
    }


    private void ListView()
    {
        listView = new android.widget.ListView(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ResetParams();

                String filename= (String) ((TextView) view.findViewById(R.id.textView)).getText();

                if(filename.indexOf("[Live]")!=-1)
                {
                    mode="LIVE";
                }
                else
                {
                    mode="VIDEO";
                }

                mNextVideoAbsolutePath = getVideoFilePath(getApplicationContext(),filename);
                new MPDDownloadThread(filename,handler,segmentHashMap).start();
                window.dismiss();
            }
        });

        //隐藏滚动条
        //listView.setVerticalScrollBarEnabled(false);
        adapter=new MyAdapter();
        listView.setAdapter(adapter);
    }

    private void ResetParams()
    {
        if(segmentGetThread!=null)
            segmentGetThread.InterruptThread();
            segmentGetThread=null;

        if(currentMediaPlayer!=null)
        {
            if(currentMediaPlayer.isPlaying()) currentMediaPlayer.stop();
        }
        for(MediaPlayer media:playersCache.values())
        {
            media.release();
        }

        nextMediaPlayer=null;
        cacheMediaPlayer=null;
        currentMediaPlayer=null;

        VideoListQueue=new ArrayList<String>();
        playersCache=new HashMap<String,MediaPlayer>();
        segmentList=new ArrayList<String>();
        segmentHashMap=new HashMap<String,List<String>>();
        segmentHashMap.put("high",new ArrayList<String>());
        segmentHashMap.put("medium",new ArrayList<String>());
        segmentHashMap.put("low",new ArrayList<String>());
        videoQuality="high";

        SegmentReceived=0;
        currentVideoIndex=0;

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (currentMediaPlayer != null && currentMediaPlayer.isPlaying())
        {
            currentMediaPlayer.pause();
            currentPosition = currentMediaPlayer.getCurrentPosition();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(currentMediaPlayer!=null&&!currentMediaPlayer.isPlaying()) currentMediaPlayer.seekTo(Math.max(currentPosition-1000,10));
    }

    public void ClickShowPopup(View v)
    {
        if(window==null)
        {
            //创建PopupWindow
            window = new PopupWindow(listView,v.getWidth()-50, 600);
            window.setOnDismissListener(new PopupWindow.OnDismissListener()
            {
                @Override
                public void onDismiss()
                {
                    new GetPlayListThread(handler).start();
                }
            });
        }

        window.setFocusable(true);
        //设置背景图片
        window.setBackgroundDrawable(new BitmapDrawable());
        //设置外部点击消失
        window.setOutsideTouchable(true);
        window.showAsDropDown(v , Gravity.CENTER,0,0);
    }


    ///storage/emulated/0/Android/data/com.example.nusmedia.seamlessvideo/files/1507862285520.mp4
    private void initView()
    {
        surface=(SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder=surface.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
    }

    private String getURL(String segmentName)
    {
        return mNextVideoAbsolutePath+segmentName;
    }

    private void initNextPlayer(final int segmentNumber, final Handler handler)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //for(int i=1;i<VideoListQueue.size();i++)
                // skip first video because it has been loaded
                //{

                nextMediaPlayer = new MediaPlayer();
                nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                nextMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer)
                    {
                        onVideoPlayCompleted(mediaPlayer);
                    }
                });
                try
                {
                    nextMediaPlayer.setDataSource(VideoListQueue.get(segmentNumber));
                    nextMediaPlayer.prepare();


                    if (segmentNumber != 0) cacheMediaPlayer.setNextMediaPlayer(nextMediaPlayer);
                    // method setNextMediaPlayer(...) allow the palyer x next segment seamlessly
                    // call before the current playing segment finishes
                    cacheMediaPlayer = nextMediaPlayer; // one by one, link the player
                    playersCache.put(String.valueOf(segmentNumber), nextMediaPlayer);  // add to cache

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if(handler!=null)
                    {
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                }
            }

            //}
        }).start();
    }

    public static List getListString(String s)
    {
        String subS[] = s.split(",");
        List list = Arrays.asList(subS);
        for(int i=0;i<list.size();i++)
        {
            list.set(i,list.get(i).toString().replace("\"",""));
        }
        return list;
    }

    private void onVideoPlayCompleted(MediaPlayer mediaPlayer)
    {
        mediaPlayer.setDisplay(null);
        currentMediaPlayer=playersCache.get(String.valueOf(currentVideoIndex));

        if(currentMediaPlayer!=null)
        {
            currentMediaPlayer.setDisplay(surfaceHolder);
            currentMediaPlayer.start();
            currentVideoIndex++;
        }
        else
        {
            if(currentVideoIndex==segmentList.size())
                Toast.makeText(MainActivity.this, "Video Finished..", Toast.LENGTH_SHORT)
                    .show();
            else
                Toast.makeText(MainActivity.this, "Video Stalls..", Toast.LENGTH_SHORT)
                        .show();

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {

    }

    private String getVideoFilePath(Context context, String filename) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + (filename.replace("[Live] ",""))+"/";
    }

    class MyAdapter extends BaseAdapter
    {

        @Override
        public int getCount()
        {
            return videoNamesList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return videoNamesList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup)
        {
            final View itemView=View.inflate(MainActivity.this,R.layout.adapter,null);
            TextView videoName=(TextView) itemView.findViewById(R.id.textView);
            videoName.setText(videoNamesList.get(position));
            return itemView;
        }
    }

    class SegmentGetThread extends Thread
    {

        boolean interrupted=false;
        private int buffer=6;

        public void InterruptThread()
        {
            interrupted=true;
        }


        public void QualitySwitch(String mode)
        {
            videoQuality=mode;
        }

        @Override
        public void run()
        //get the next segment from server and link it to the mediaplayer list
        {
            long EB=2500;
            double alpha=0.2;

            segmentList=segmentHashMap.get(videoQuality);
            while(SegmentReceived< segmentList.size()&&!interrupted)
            {
                segmentList=segmentHashMap.get(videoQuality);
                if (SegmentReceived - currentVideoIndex <= (int)(buffer*0.75))
                {
                    String URL =  segmentList.get(SegmentReceived);
                    String subS[] = URL.split("/");
                    List PathComponentsList = Arrays.asList(subS);
                    String fileName = PathComponentsList.get(5).toString();
                    String segmentName = PathComponentsList.get(6).toString();

                    long startTime = System.currentTimeMillis();

                    File file=HttpConnectionUtil.downloadFile("http://monterosa.d2.comp.nus.edu.sg/~team02/FileSendServer.php", "/storage/emulated/0/Android/data/com.nusmedia.player/files/",
                            fileName, segmentName);

                    if (file!= null)
                    {

                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;
                        long rate=file.length()*8/duration*100;

                        EB=(long)((1.0-alpha)*EB+alpha*rate);

                        if(EB>2500) QualitySwitch("high");
                        else if(EB>1300) QualitySwitch("low");
                        else QualitySwitch("low");

                        String url=getURL(segmentName);
                        VideoListQueue.add(url);

                        initNextPlayer(SegmentReceived,null);
                        if(SegmentReceived-currentVideoIndex>=buffer/2 || segmentList.size()-(SegmentReceived+1)<2)
                        {
                            //check play normal
                            if(currentMediaPlayer==null) //reconnect
                            {
                                currentMediaPlayer=playersCache.get(String.valueOf(currentVideoIndex));
                                if(currentMediaPlayer!=null)
                                {
                                    currentMediaPlayer.setDisplay(surfaceHolder);
                                    currentMediaPlayer.start();
                                    currentVideoIndex++;
                                }
                            }
                        }

                        SegmentReceived++;
                    }
                    else
                    {
                        try
                        {
                            Thread.sleep(500);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
                else
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
            }

        }
    }

    class LiveSegmentGetThread extends Thread
    {
        private boolean ini_finished=true;
        private boolean ini_buffer=true;

        private Handler handler_init=new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                ini_finished=true;
            }
        };

        public void QualitySwitch(String mode)
        {
            videoQuality=mode;
        }

        boolean interrupted=false;


        @Override
        public void run()
        //get the next segment from server and link it to the mediaplayer list
        {

            QualitySwitch("low");

            while(!interrupted)
            {
                segmentList=segmentHashMap.get(videoQuality);

                if (SegmentReceived < segmentList.size())
                {

                    String URL = segmentList.get(SegmentReceived);
                    String subS[] = URL.split("/");
                    List PathComponentsList = Arrays.asList(subS);
                    String fileName = PathComponentsList.get(5).toString();
                    String segmentName = PathComponentsList.get(6).toString();

                    File file=HttpConnectionUtil.downloadLiveFile("http://monterosa.d2.comp.nus.edu.sg/~team02/FileSendServer.php", "/storage/emulated/0/Android/data/com.nusmedia.player/files/",
                            fileName, segmentName);

                    if (file!=null&&file.length()>200)
                    {
                        String url = getURL(segmentName);
                        VideoListQueue.add(url);

                        ini_finished=false;
                        initNextPlayer(SegmentReceived,handler_init);

                        while(!ini_finished) {}

                        if(playersCache.get(String.valueOf(SegmentReceived))!=null)
                        {
                            SegmentReceived++;
                        }


                        if(!ini_buffer||SegmentReceived-currentVideoIndex>=2)
                        {
                            ini_buffer=false;
                            if (currentMediaPlayer == null) //reconnect
                            {
                                currentMediaPlayer = playersCache.get(String.valueOf(currentVideoIndex));
                                if (currentMediaPlayer != null) {
                                    currentMediaPlayer.setDisplay(surfaceHolder);
                                    currentMediaPlayer.start();
                                    currentVideoIndex++;
                                }
                            }
                        }

                    }
                    else
                    {

                        try
                        {
                            Thread.sleep(200);
                        }
                        catch (InterruptedException e)
                        {
                            //e.printStackTrace();
                        }
                    }

                }
                else
                {

                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        //e.printStackTrace();
                    }
                }

            }
        }


    }

}
