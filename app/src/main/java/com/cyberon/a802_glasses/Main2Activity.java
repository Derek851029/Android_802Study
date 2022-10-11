package com.cyberon.a802_glasses;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberon.engine.LoadLibrary;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterRecog.DSpotterRecogStatusListener;
import com.cyberon.dspotterutility.DSpotterStatus;

public class Main2Activity extends AppCompatActivity {
    private String[] voice = {"播放影片","離開","第一項","第二項","第三項","第四項","第五項","第六項","第七項","第八項","第九項","第十項","上一頁","下一頁","完成","退出軟體","完成操作","離開操作"};

    private TableLayout tableLayout;
    private ImageView imageView;

    private String End = "";
    private AlertDialog dialog = null;
    private AlertDialog dialog2 = null;
    private AlertDialog dialog3 = null;

    private String Study_Data; //response Data
    private String Study_name;
    private String KeyInfo; //左排
    private String KeyValuesInfo; //step
    private String VideoInfo;
    private String Video_have = "";
    private String[] KeyInfo_array;
    private String[] keyword_arr = new String[10];
    List<String> keyword_finish = new ArrayList<String>();
    private String[] num_chin = {"一","二","三","四","五","六","七","八","九","十"};
    private int now_index = 0;
    private String [] video_arr = new String[5];
    private String [] step_arr = new String[20];
    private String [] sopmain_arr = new String[20];
    private String finish_keyword;
    private String FormID;
    private String Agent_ID;

    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog  m_oDSpotterRecog = null;

    //form createLogResultFile function
    // Log file
    private BufferedWriter m_oBufferedWriter = null;
    private String[] m_sTriggerWordArray = null;
    private long m_lStartTime = 0;

    private ArrayAdapter<String> m_oCommandBinAdapter = null;

    //form createLogResultFile function
    // Count recognition result
    private int[] m_naTriggerWordHit = null;
    private int m_nHitCount = 0;
    private String m_sLogFile = null;


    // For status callback
    private DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {


        public DSpotterDemoHandler(Main2Activity mainActivity) {

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
                        case "播放影片":
                            if(video_arr[0] == null){
                                showToast("此教案沒有影片。");
                            }
                            else {
                                m_oDSpotterRecog.stop();
                                Intent intent3 = new Intent();
                                intent3.setClass(Main2Activity.this,VideoPlayer.class);
                                Bundle bundle2 = new Bundle();
                                bundle2.putStringArray("video_arr",video_arr);
                                intent3.putExtras(bundle2);   // 記得put進去，不然資料不會帶過去
                                startActivity(intent3);
                            }

                            break;
                        case "第一項":
                            if(End.equals("End")){
                                try{
                                    dialog2.dismiss();
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,0);
                                    Clear_finish();
                                    Go_Step(0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                if(KeyInfo_array.length >= 1){
                                    try{
                                        m_oDSpotterRecog.stop();
                                        Parse_JSON(KeyValuesInfo,0);
                                        Go_Step(0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            break;
                        case "第二項":
                            if(KeyInfo_array.length >= 2){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,1);
                                    Go_Step(1);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                        case "第三項":
                            if(KeyInfo_array.length >= 3){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,2);
                                    Go_Step(2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        case "第四項":
                            if(KeyInfo_array.length >= 4){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,3);
                                    Go_Step(3);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        case "第五項":
                            if(KeyInfo_array.length >= 5){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,4);
                                    Go_Step(4);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        case "第六項":
                            if(KeyInfo_array.length >= 6){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,5);
                                    Go_Step(5);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        case "第七項":
                            if(KeyInfo_array.length >= 7){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,6);
                                    Go_Step(6);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                        case "第八項":
                            if(KeyInfo_array.length >= 8){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,7);
                                    Go_Step(7);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                        case "第九項":
                            if(KeyInfo_array.length >= 9){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,4);
                                    Go_Step(8);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                        case "第十項":
                            if(KeyInfo_array.length >= 10){
                                try{
                                    m_oDSpotterRecog.stop();
                                    Parse_JSON(KeyValuesInfo,4);
                                    Go_Step(9);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        case "下一頁":
                            if(KeyInfo_array.length >= 6){
                                tableLayout.removeAllViews();
                                titleData();
                                fillData(5);
                            }
                            break;
                        case "上一頁":
                            tableLayout.removeAllViews();
                            titleData();
                            fillData(0);
                            break;
                        case "完成":
                            m_oDSpotterRecog.stop();
                            Intent intent = new Intent();
                            intent.setClass(Main2Activity.this,View_List.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("KeyValuesInfo",KeyValuesInfo);
                            bundle.putString("KeyInfo",KeyInfo);
                            intent.putExtras(bundle);   // 記得put進去，不然資料不會帶過去
                            startActivity(intent);
                            break;
                        case "退出軟體":
                            m_oDSpotterRecog.stop();
                            SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            finishAffinity();
                            System.exit(0);
                            break;
                        case "完成操作":
                            if(dialog.isShowing()){
                                End = "";
                                dialog.dismiss();
                            }
                            break;
                        case "離開操作":
                            if(End.equals("")){
                                End = "End";
                                check_step();
                            }
                            else {
                                dialog.dismiss();
                                m_oDSpotterRecog.stop();
                                Intent intent2 = new Intent();
                                Bundle bundle2 = new Bundle();
                                bundle2.putString("Agent_ID",Agent_ID);
                                intent2.setClass(Main2Activity.this,MainActivity.class);
                                intent2.putExtras(bundle2);

                                startActivity(intent2);
                                break;
                            }
                            break;
                    }
                    break;
                case DSpotterStatus.STATUS_RECOGNITION_FAIL:
                    System.out.println("DSpotterStatus.STATUS_RECOGNITION_FAIL");
                    m_oDSpotterRecog.stop();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("In");
        Bitmap photo = null;
        Uri uri = data.getData();
        photo = BitmapFactory.decodeFile(uri.getPath());
        System.out.println(photo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        setContentView(R.layout.activity_main2);

        tableLayout = findViewById(R.id.tableLayoutProduct);
        imageView = findViewById(R.id.video_image);
//        LoadLibrary.loadLibrary(getApplicationContext());

        iniDSpotter();
//        initCmdBinSpinner();

        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        Study_Data = sharedPreferences.getString("Study_Data","");
        Study_name = sharedPreferences.getString("Study_name","");
        finish_keyword = sharedPreferences.getString("finish_keyword","");
        Agent_ID = sharedPreferences.getString("Agent_ID","");
        FormID = sharedPreferences.getString("FormID","");
        ParseData(Study_Data);

        dialogCreate();

        if(Video_have.equals("yes")){
            imageView.setVisibility(View.VISIBLE);
        }else {
            imageView.setVisibility(View.INVISIBLE);
        }

        Bundle bundle = getIntent().getExtras();
        String status = bundle.getString("New");
        if(status.equals("New")){
            if(Video_have.equals("yes")){
                dialog2.show();
                return;
            }
        }

        try {
            Check_All();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_oDSpotterRecog == null) {

        }
    }

    public void dialogCreate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("1.若要離開此教案, 請說「離開操作」回上一步掃描其他教案"+"\n"+"2.若要結束練習, 請說「退出軟體」"+"\n"+"3.若要繼續,請說「繼續操作」接續前次未完成的步驟"+"\n"+"4.若要重新練習,請說「第一項」");
//        builder.setPositiveButton("離開操作", (dialog, id) -> {
//        });
//        builder.setNegativeButton("完成操作", (dialogInterface, id) -> {
//        });
        dialog = builder.create();

        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setMessage("此教案有影片, 請說「播放影片」; 若不觀看請說「第一項」進入練習步驟");
        dialog2 = builder2.create();

        AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
        builder3.setMessage("已完成此教案所有項目,"+"\n"+"1.若要掃描其它請說「離開操作」"+"\n"+"2.若要重新練習,請說「第一項」\n3.若要結束練習, 請說「退出軟體」\n4.若要檢視完成表請說「完成」");
        dialog3 = builder3.create();
    }

    public void check_step(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        finish_keyword = sharedPreferences.getString("finish_keyword","");
        if(finish_keyword.equals("")){
            m_oDSpotterRecog.stop();
            Intent intent2 = new Intent();
            Bundle bundle2 = new Bundle();
            bundle2.putString("Agent_ID",Agent_ID);
            intent2.setClass(Main2Activity.this,MainActivity.class);
            intent2.putExtras(bundle2);

            startActivity(intent2);
        }
        else {
            dialog.show();
        }
    }

    public void showToast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

    public void ParseData(String Study_Data) {
        try {
            JSONObject jsonObject = new JSONObject(Study_Data);
            KeyInfo = jsonObject.getString("KeyInfo");
            KeyValuesInfo = jsonObject.getString("KeyValuesInfo");
            VideoInfo = jsonObject.getString("VideoInfo");
//            System.out.println("KeyInfo:"+KeyInfo);
//            System.out.println("KeyValuesInfo:"+KeyValuesInfo);
            System.out.println(VideoInfo);
            ParseVideo();

            titleData();
            fillData(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void ParseVideo() throws JSONException {
        JSONArray jsonArr = new JSONArray(VideoInfo);
        if (jsonArr.length() != 0) {
            Video_have = "yes";
            for(int i = 0; i<jsonArr.length();i++){
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                String VideoName = jsonObj.getString("VideoName");
                video_arr[i] = VideoName;
                System.out.println(VideoName);
            }
        }
    }

    public void titleData(){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(30, 20, 30, 0);
//        StudyName();
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams layoutParams1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.MATCH_PARENT);
        layoutParams1.setMargins(10,10,10,10);
//        tableRow.setLayoutParams (new TableRow.LayoutParams(
//                TableRow.LayoutParams.WRAP_CONTENT,
//                TableRow.LayoutParams.WRAP_CONTENT));
//            tableRow.setBackgroundColor(Color.rgb(255,255,255));

        TextView text_study = findViewById(R.id.study_name);
        text_study.setText("教案名稱:"+Study_name);

        // Number
        TextView textViewNumber = new TextView(this);
        textViewNumber.setText("大項");
        textViewNumber.setTextSize(24);
        textViewNumber.setTextColor(Color.rgb(255,255,255));
        textViewNumber.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewNumber.setPadding(5, 5, 120, 20);
        tableRow.addView(textViewNumber);

        // keyword
        TextView textViewkeyword = new TextView(this);
        textViewkeyword.setText("步驟");
        textViewkeyword.setTextSize(24);
        textViewkeyword.setTextColor(Color.rgb(255,255,255));
        textViewkeyword.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewkeyword.setPadding(5, 5, 120, 20);
        tableRow.addView(textViewkeyword);

        // Name Column
        TextView textViewName = new TextView(this);
        textViewName.setText("狀態");
        textViewName.setTextSize(24);
        textViewName.setTextColor(Color.rgb(255,255,255));
        textViewName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewName.setPadding(5, 5, 5, 20);
        tableRow.addView(textViewName);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }

    public void fillData(int index){
        ImageView imageView = findViewById(R.id.page1);
        ImageView imageView1 = findViewById(R.id.page2);
        try {
            KeyInfo_array = KeyInfo.split(",");
            JSONArray jsonArray = new JSONArray();
            if(KeyInfo_array.length >= 6){
                if(index == 5){
                    System.out.println("In");
                    imageView.setVisibility(View.VISIBLE);
//                    imageView.clearColorFilter();
                    imageView.setColorFilter(Color.rgb(255,255,255));
                    imageView1.setVisibility(View.VISIBLE);
                    imageView1.setColorFilter(Color.rgb(255,0,0));
                }
                else {
                    System.out.println("Out");
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setColorFilter(Color.rgb(255,0,0));
                    imageView1.setVisibility(View.VISIBLE);
                    imageView1.clearColorFilter();
                }
            }
            else {
                imageView.setVisibility(View.INVISIBLE);
                imageView1.setVisibility(View.INVISIBLE);
            }

            if(finish_keyword.equals("") == false){
                jsonArray = new JSONArray(finish_keyword);
                System.out.println(jsonArray.toString());
            }

            for (int i=index; i<KeyInfo_array.length;i++){
                String keyword = "";
                if(i==0){
                    //處理第一個會莫名抓到[
                    String sub_str = KeyInfo_array[i].toString().substring(1);
                    JSONObject jsonObject = new JSONObject(sub_str);
                    keyword = jsonObject.getString("keyword");
                }else {
                    JSONObject jsonObject = new JSONObject(KeyInfo_array[i]);
                    keyword = jsonObject.getString("keyword");
                }
                keyword_arr[i] = keyword;


                TableRow tableRow = new TableRow(this);
                tableRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                TextView textViewId = new TextView(this);
                textViewId.setText(num_chin[i]);
                textViewId.setTextSize(24);
                textViewId.setTextColor(Color.rgb(255,255,255));
                textViewId.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                textViewId.setPadding(5, 5, 5, 10);
                tableRow.addView(textViewId);

                TextView textViewStep = new TextView(this);
                textViewStep.setText(keyword);
                textViewStep.setTextSize(24);
                textViewStep.setTextColor(Color.rgb(255,255,255));
                textViewStep.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                textViewStep.setPadding(5, 5, 150, 10);
                tableRow.addView(textViewStep);

                if(finish_keyword.equals("") == false){
                    for(int x = 0; x<jsonArray.length();x++){
                        if(jsonArray.getString(x).equals(keyword)){
                            ImageView imageView2 = new ImageView(getApplicationContext());
                            imageView2.setImageResource(R.drawable.check);
                            tableRow.addView(imageView2);
                        }
                    }
                }

                tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                if((i+1)%5 == 0){
                    break;
                }

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void Parse_JSON(String data,int index) throws JSONException {
        String keyword = keyword_arr[index];

        String info_keyword = "";
        JSONArray jsonArr = new JSONArray(data);
        int count = 0;

        Arrays.fill(step_arr,null);
        for(int i = 0;i<jsonArr.length();i++){
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            info_keyword = jsonObj.getString("keyword");

            if(info_keyword.equals(keyword)){
//                if(i == step_arr.length){
//                    System.out.println("In");
//                    step_arr = Arrays.copyOf(step_arr,step_arr.length+1);
//                }
                step_arr[count] = jsonObj.getString("values");
                sopmain_arr[count] = jsonObj.getString("main");
                count += 1;
//                System.out.println(Arrays.toString(sopmain_arr));
            }else {
                continue;
            }
        }
    }

    public void Clear_finish(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("finish_keyword");
        for(int i = 0; i<keyword_arr.length; i++){
            editor.remove(keyword_arr[i]);
        }

        editor.commit();
    }

    public void Go_Step(int index){
        Intent intent = new Intent();
        intent.setClass(Main2Activity.this,KeyStep.class);
        Bundle bundle = new Bundle();
//        bundle.putStringArray("step_arr",step_arr);
//        bundle.putStringArray("sopmain_arr",sopmain_arr);
        bundle.putInt("index",index);
        bundle.putString("KeyValuesInfo",KeyValuesInfo);
        bundle.putString("KeyInfo",KeyInfo);
        bundle.putString("keyword",keyword_arr[index]);
        intent.putExtras(bundle);   // 記得put進去，不然資料不會帶過去
        startActivity(intent);
    }

    public void Check_All() throws JSONException {
        JSONArray jsonArray = new JSONArray();

        if(finish_keyword.equals("") == false){
            jsonArray = new JSONArray(finish_keyword);

            int finish_len = jsonArray.length();
            int keyinfo_len = KeyInfo_array.length;
            if(finish_len == keyinfo_len){
                End = "End";
                dialog3.show();
            }
        }
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost("http://210.68.227.123:8029/WebServiceFormID.asmx/FormTime");
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("FormID", FormID));
                nvps.add(new BasicNameValuePair("AgentID", Agent_ID));
                nvps.add(new BasicNameValuePair("Status", "end"));
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
                    System.out.println(response.getCode() + " " + response.getReasonPhrase());
                    HttpEntity entity = response.getEntity();
                    String res_data  = EntityUtils.toString(entity); //response
                    System.out.println(res_data);
                }catch (ParseException | IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //下面全都是語音辨識用
    private void initCmdBinSpinner() {
        File oFile = new File(DSpotterApplication.m_sCmdFileDirectoryPath);
        String[] strBinFileArray = oFile.list(new CmdBinFilter());

        if (strBinFileArray == null || strBinFileArray.length == 0) {
            showToast("Found no command file.");
            return;
        }

        for (int i = 0; i < strBinFileArray.length; i++)
            strBinFileArray[i] = strBinFileArray[i].substring(0,
                    strBinFileArray[i].length() - 4); // skip .bin

        Arrays.sort(strBinFileArray);
        m_oCommandBinAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, strBinFileArray);
        m_oCommandBinAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private class CmdBinFilter implements FilenameFilter {

        @SuppressLint("DefaultLocale")
        private boolean isBinFile(String file) {
            return file.endsWith(".bin");
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (filename.equals("DSpotter_CMS.bin"))
                return false;
            return isBinFile(filename);
        }
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

//    private void createLogResultFile() {
//        Date oCurrDateTime = new Date();
//        SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss",
//                Locale.TAIWAN);
//
//        // Log recognition result
//        m_sLogFile = DSpotterApplication.m_sOnlineTestLogDirectoryPath + "/"
//                + String.format("Result_%s.log",
//                oDateFormat.format(oCurrDateTime));
//
//        // =====================================
//        // Check folder, if not exist to create folder
//        // =====================================
//        File oWaveDataPath = new File(m_sLogFile);
//        File oWaveDirPath = oWaveDataPath.getParentFile();
//        if (false == oWaveDirPath.exists())
//            oWaveDirPath.mkdir();
//
//        try {
//            m_oBufferedWriter = new BufferedWriter(new FileWriter(m_sLogFile,
//                    false));
//
//            m_sTriggerWordArray = m_oDSpotterRecog.getTriggerWord();
//            m_naTriggerWordHit = new int[m_sTriggerWordArray.length];
//            for (int i = 0; i < m_sTriggerWordArray.length; i++)
//                m_naTriggerWordHit[i] = 0;
//
//            m_nHitCount = 0;
//        } catch (IOException e) {
//            showToast("Create log file failed.");
//            e.printStackTrace();
//        } finally {
//
//        }
//
//        m_lStartTime = SystemClock.elapsedRealtime();
//    }
}