package com.cyberon.a802_glasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;

import org.json.JSONException;

import java.io.File;
import java.util.Arrays;

public class VideoPlayer extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private String[] voice = {"下一頁","離開"};

    private VideoView mVv;
    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;

    private String IP;
    private String[] video_arr;
    private int now_index = 0;

    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new VideoPlayer.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {

        public DSpotterDemoHandler(VideoPlayer mainActivity) {

        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INITIALIZE_SUCCESS:
                    System.out.println("MSG_INITIALIZE_SUCCESS");
                    // Set recognize button enable
                    // Show success message
                    // showToast("Initialize success!");
                    break;
                case DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECORDER_INITIALIZE_FAIL");
                    m_oDSpotterRecog.stop();

                    // Show message
                    // Show message
                    showToast("Fail to initialize recorder!");
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_START:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_START");
                    break;

                case DSpotterStatus.STATUS_RECOGNITION_OK:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_OK");

                    String[] straResult = new String[1];
                    m_oDSpotterRecog.getResult(null, straResult, null, null, null, null, null, null);

                    System.out.println(straResult[0]);
                    String Recog_text = straResult[0].replaceAll("\\s+","");
                    if(Arrays.asList(voice).contains(Recog_text)){
                        showToast(Recog_text);
                    }
                    switch (Recog_text){
                        case "下一頁":
                            if(video_arr[now_index+1] !=null){
                                now_index +=1;
                                play(IP+"/video/"+video_arr[now_index]);
                            }
                            else {
                                showToast("沒有下一部影片。");
                            }

                            break;
                        case "離開":
                            m_oDSpotterRecog.stop();
                            mVv.stopPlayback();
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("New","");
                            intent.putExtras(bundle);
                            intent.setClass(VideoPlayer.this,Main2Activity.class);
                            startActivity(intent);
                            break;
                    }
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_FAIL");
//                    m_oDSpotterRecog.stop();
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_ABORT:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_ABORT");
                    break;
                case DSpotterStatus.STATUS_RECORD_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECORD_FAIL");
                    m_oDSpotterRecog.stop();
                    break;
                case DSpotterStatus.STATUS_RECORD_FINISH:
                    System.out.println("DSpotterStatus.STATUS_RECORD_FINISH");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(R.layout.videoview);

        iniDSpotter();
        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        IP = sharedPreferences.getString("IP","");
        Bundle bundle = getIntent().getExtras();
        video_arr = bundle.getStringArray("video_arr");
        play(IP+"/video/"+video_arr[now_index]);
        System.out.println(IP+"/video/"+video_arr[now_index]);
//        mVv = (VideoView) findViewById(R.id.vv);
//        mVv.setOnPreparedListener(this);
//        mVv.setOnErrorListener(this);
//        mVv.setOnCompletionListener(this);
//
////        mVv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test));
//        mVv.setVideoURI(Uri.parse("http://210.68.227.123:8029/video/BCMA.mp4"));
//        mVv.setMediaController(new MediaController(this));
//        mVv.start();
////        mVv.setVideoPath("android.resource://" + getPackageName() + "/" + R.);
    }

    public void play(String url){
        mVv = (VideoView) findViewById(R.id.vv);
        mVv.setOnPreparedListener(this);
        mVv.setOnErrorListener(this);
        mVv.setOnCompletionListener(this);

        mVv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test));
//        mVv.setVideoURI(Uri.parse(url));
        mVv.setMediaController(new MediaController(this));
        mVv.start();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO
        Toast.makeText(getApplicationContext(), "播放完畢。", Toast.LENGTH_LONG).show();
        if(video_arr[now_index+1] == null){
            m_oDSpotterRecog.stop();
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("New","");
            intent.putExtras(bundle);
            intent.setClass(VideoPlayer.this,Main2Activity.class);
            startActivity(intent);
        }
        else {
            now_index +=1;
            play(IP+"/video/"+video_arr[now_index]);
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "資源有問題", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        Toast.makeText(getApplicationContext(), "準備好了", Toast.LENGTH_LONG).show();
    }

    public void showToast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

    //語音辨識用
    public void iniDSpotter(){

        if (m_oDSpotterRecog == null)
            m_oDSpotterRecog = new DSpotterRecog();

        int nRet;
        int[] naErr = new int[1];

        String strCommandFile;
        if (DSpotterApplication.m_sCmdFilePath == null) {
//            strCommandFile = DSpotterApplication.m_sCmdFileDirectoryPath + "/"
//                    + m_oCommandBinAdapter
//                    .getItem(DSpotterApplication.m_nCmdBinListIndex)
//                    + ".bin";
            strCommandFile = DSpotterApplication.m_sCmdFileDirectoryPath + "/" + "glasses_802_pack_withTxt" + ".bin";
        }
        else {
            strCommandFile = DSpotterApplication.m_sCmdFilePath;
            String strCommandFileName = new File(strCommandFile).getName();
            DSpotterApplication.m_sCmdFilePath = null;
        }

        nRet = m_oDSpotterRecog.initWithFiles(this,strCommandFile,DSpotterApplication.m_sLicenseFile,DSpotterApplication.m_sServerFile,naErr);
        if (nRet != DSpotterRecog.DSPOTTER_RECOG_SUCCESS) {
            Toast.makeText(this,"Fail to initialize DSpotter, " + naErr[0],Toast.LENGTH_LONG).show();
            return;
        }

        m_oDSpotterRecog.setListener(m_oRecogStatusListener);

        m_oDSpotterRecog.getTriggerWord();

        m_oHandler.sendMessage(m_oHandler.obtainMessage(MSG_INITIALIZE_SUCCESS,
                0, 0));
    }
}
