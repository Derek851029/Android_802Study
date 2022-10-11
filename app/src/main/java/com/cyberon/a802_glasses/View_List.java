package com.cyberon.a802_glasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class View_List extends AppCompatActivity {
    private String[] voice = {"上一頁","下一頁","離開操作"};
    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;

    private String KeyValuesInfo; //response Data
    private String KeyInfo;

    TextView textView;
    ImageView imageView;

    private int num = 0;

    JSONArray main_arr = new JSONArray();
    JSONArray finish_arr = new JSONArray();

    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new View_List.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {

        public DSpotterDemoHandler(View_List mainActivity) {

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
                        case "上一頁":
                            if(num == 0){
                                showToast("已經在第一總表");
                                break;
                            }
                            else {
                                try {
                                    num-=1;
                                    Parse_JSON();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;
                        case "下一頁":
                            String[] KeyInfo_array = KeyInfo.split(",");
                            if(num+1 == KeyInfo_array.length) {
                                showToast("已經在最後總表");
                            }
                            else {
                                try {
                                    num+=1;
                                    Parse_JSON();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            break;
                        case "離開操作":
                            m_oDSpotterRecog.stop();
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("New","");
                            intent.putExtras(bundle);
                            intent.setClass(View_List.this,Main2Activity.class);
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

        setContentView(R.layout.list_view);
        iniDSpotter();
        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        Bundle bundle = getIntent().getExtras();

        KeyValuesInfo = bundle.getString("KeyValuesInfo");
        KeyInfo = bundle.getString("KeyInfo");
        try {
            Parse_JSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void Parse_JSON() throws JSONException {
        JSONArray jsonArr = new JSONArray(KeyValuesInfo);
        String[] KeyInfo_array = KeyInfo.split(",");
        if(num+1 <= KeyInfo_array.length) {
            try {
                Clear();
                String keyword = "";
                if (num == 0) {
                    //處理第一個會莫名抓到[
                    String sub_str = KeyInfo_array[num].toString().substring(1);
                    JSONObject jsonObject = new JSONObject(sub_str);
                    keyword = jsonObject.getString("keyword");
                } else {
                    JSONObject jsonObject = new JSONObject(KeyInfo_array[num]);
                    keyword = jsonObject.getString("keyword");
                }

                TextView textViewtitle = findViewById(R.id.title);
                textViewtitle.setText(keyword);
                System.out.println(keyword);

                SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                String step_data = sharedPreferences.getString(keyword, "");
                if (step_data.equals("") == false) {
                    finish_arr = new JSONArray(step_data);
                    for (int x = 0; x < jsonArr.length(); x++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(x);
                        String obj_keyword = jsonObj.getString("keyword");
                        if (keyword.equals(obj_keyword)) {
                            String main = jsonObj.getString("main");
                            main_arr.put(main);
                        }
                    }
                } else {
                    for (int x = 0; x < jsonArr.length(); x++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(x);
                        String obj_keyword = jsonObj.getString("keyword");
                        if (keyword.equals(obj_keyword)) {
                            String main = jsonObj.getString("main");
                            main_arr.put(main);
                        }
                    }
                }
//                        init_title();
                init_view();
                System.out.println(main_arr.toString());
                System.out.println(finish_arr.toString());
                main_arr = new JSONArray(new ArrayList<String>());
                finish_arr = new JSONArray(new ArrayList<String>());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



//    public void Parse_JSON() throws JSONException {
//        Handler handler=new Handler();
//        handler.postDelayed(new Runnable() {
//            JSONArray jsonArr = new JSONArray(KeyValuesInfo);
//            String[] KeyInfo_array = KeyInfo.split(",");
//            int num = 0;
//            @Override
//            public void run() {
//                if(num+1 <= KeyInfo_array.length){
//                    try {
//                        Clear();
//                        String keyword = "";
//                        if(num==0){
//                            //處理第一個會莫名抓到[
//                            String sub_str = KeyInfo_array[num].toString().substring(1);
//                            JSONObject jsonObject = new JSONObject(sub_str);
//                            keyword = jsonObject.getString("keyword");
//                        }else {
//                            JSONObject jsonObject = new JSONObject(KeyInfo_array[num]);
//                            keyword = jsonObject.getString("keyword");
//                        }
//
//                        TextView textViewtitle = findViewById(R.id.title);
//                        textViewtitle.setText(keyword);
//                        System.out.println(keyword);
//
//                        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
//                        String step_data = sharedPreferences.getString(keyword,"");
//                        if(step_data.equals("") == false){
//                            finish_arr = new JSONArray(step_data);
//                            for(int x = 0; x<jsonArr.length();x++){
//                                JSONObject jsonObj = jsonArr.getJSONObject(x);
//                                String obj_keyword = jsonObj.getString("keyword");
//                                if(keyword.equals(obj_keyword)){
//                                    String main = jsonObj.getString("main");
//                                    main_arr.put(main);
//                                }
//                            }
//                        }
//                        else {
//                            for(int x = 0; x<jsonArr.length();x++){
//                                JSONObject jsonObj = jsonArr.getJSONObject(x);
//                                String obj_keyword = jsonObj.getString("keyword");
//                                if(keyword.equals(obj_keyword)){
//                                    String main = jsonObj.getString("main");
//                                    main_arr.put(main);
//                                }
//                            }
//                        }
////                        init_title();
//                        init_view();
//                        System.out.println(main_arr.toString());
//                        System.out.println(finish_arr.toString());
//                        main_arr = new JSONArray(new ArrayList<String>());
//                        finish_arr = new JSONArray(new ArrayList<String>());
//                        num += 1;
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    handler.postDelayed(this,5000);
//                }
//                else {
//                    handler.removeCallbacks(this);
//                    Intent intent = new Intent();
//                    intent.setClass(View_List.this,Main2Activity.class);
//                    startActivity(intent);
//                }
//            }
//        },500);
//    }

    public void Clear(){
        for(int i = 0; i<=13; i++){
            int textID = getResources().getIdentifier("text"+i,"id",getPackageName());
            int imageID = getResources().getIdentifier("image"+i,"id",getPackageName());
            textView = findViewById(textID);
            textView.setText("");

            imageView = findViewById(imageID);
            imageView.setImageResource(0);
        }
    }

    public void init_view() throws JSONException {
        ArrayList<Integer> finish_step = new ArrayList<>();
        int finish_len = finish_arr.length();
        if(finish_len != 0){
            for (int i=0;i<finish_len;i++){
                finish_step.add(finish_arr.getInt(i));
            }
        }

        for(int i = 0;i <main_arr.length();i++){
            int textID = getResources().getIdentifier("text"+i,"id",getPackageName());
            int imageID = getResources().getIdentifier("image"+i,"id",getPackageName());
            String main_name = main_arr.getString(i);
            textView = findViewById(textID);
            textView.setText(main_name);

            imageView = findViewById(imageID);
            if(finish_step.contains(i)){
                imageView.setImageResource(R.drawable.check);
            }
//            if(finish_len != 0){
//                for(int x = 0; x<finish_len; x++){
//                    int finish_num = finish_arr.getInt(x);
//                    if(finish_num == i){
//                        imageView.setImageResource(R.drawable.check);
//                    }else {
//                        imageView.setImageResource(0);
//                    }
//                }
//            }
            else {
                imageView.setImageResource(0);
            }
        }
        TextView textView = findViewById(R.id.textview);
        textView.setText("*請說「上一頁」「下一頁」,檢視完成表,「離開操作」回上一步");
    }

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
    public void showToast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }
}
