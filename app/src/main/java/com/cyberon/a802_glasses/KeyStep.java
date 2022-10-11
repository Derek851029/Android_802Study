package com.cyberon.a802_glasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyStep extends AppCompatActivity {
    private String[] voice = {"上一頁","下一頁","完成"};

    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog  m_oDSpotterRecog = null;
    private TableLayout tableLayout;

    private String Now;

    private String KeyValuesInfo;
    private String KeyInfo;
    private String[] KeyInfo_array;
    private String [] keyword_arr = new String[10];
    private String [] step_arr = new String[20];
    private String [] sopmain_arr = new String[20];
    private int keyword_index;
    private String[] num_chin = {"一、","二、","三、","四、","五、","六、","七、","八、","九、","十、"};

    private String keyword;
//    private String step_arr[];
//    private String sopmain_arr[];
    private int sop_len = 0;
    JSONArray finish_step_jsonArray = new JSONArray();
    ArrayList<Integer> finish_step = new ArrayList<>();
    private int now_index = 0;

    private String Agent_ID;
    private String FormID;
    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new KeyStep.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {

        public DSpotterDemoHandler(KeyStep mainActivity) {

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
                    tableLayout = findViewById(R.id.tableLayoutProduct);
                    switch (Recog_text){
                        case "完成":
                            try {
                                if(finish_step.contains(now_index) == false){
                                    finish_step_jsonArray.put(now_index);
                                    finish_step.add(now_index);
                                }

                                if(step_arr[now_index+1] != null){
                                    tableLayout.removeAllViews();
                                    Steptitle();
                                    step_main();
                                    initStep();
                                    same_step_sleep();
                                    break;
                                }
                                else {
                                    try {
                                        tableLayout.removeAllViews();
                                        if(keyword_arr[keyword_index+1] != null){
                                            tableLayout.removeAllViews();
                                            Steptitle();
                                            step_main();
                                            initStep();
                                            change_step_sleep("new");
                                        }
                                        else {
                                            m_oDSpotterRecog.stop();
                                            write_step_data();
                                            Intent intent = new Intent();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("New","");
                                            intent.putExtras(bundle);
                                            intent.setClass(KeyStep.this,Main2Activity.class);
                                            startActivity(intent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                        case "上一頁":
                            if(now_index == 0){
                                if(keyword_index == 0){
                                    showToast("已經在第一步。");
                                }
                                else {
                                    tableLayout.removeAllViews();
                                    change_step_sleep("back");
                                }
                            }else {
                                tableLayout.removeAllViews();
                                now_index -=1;
                                Steptitle();
                                step_main();
                                initStep();
                            }
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setContentView(R.layout.keystep);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30, 20, 30, 0);

        tableLayout = findViewById(R.id.tableLayoutProduct);

        iniDSpotter();
        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        Bundle bundle = getIntent().getExtras();
        keyword_index = bundle.getInt("index");
        KeyValuesInfo = bundle.getString("KeyValuesInfo");
        KeyInfo = bundle.getString("KeyInfo");

        try {
            ParseData();
            keyword = keyword_arr[keyword_index];
            Parse_step("new");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        String step_data = sharedPreferences.getString(keyword,"");
        Agent_ID = sharedPreferences.getString("Agent_ID","");
        FormID = sharedPreferences.getString("FormID","");

//        Thread thread = new Thread(runnable);
//        thread.start();

        if(step_data.equals("") == false){
            try {
                JSONArray jsonArray = new JSONArray(step_data);
                for(int i = 0;i<jsonArray.length();i++){
                    int data = jsonArray.getInt(i);
                    finish_step.add(data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Steptitle();
        step_main();
        initStep();
//        test();
    }

    public void same_step_sleep(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tableLayout.removeAllViews();
                now_index +=1;
                Steptitle();
                step_main();
                initStep();
            }
        },1500);
    }

    public void change_step_sleep(String type){
        Handler handler=new Handler();
        if(type.equals("new")){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tableLayout.removeAllViews();
                    try {
                        write_step_data();
                        keyword_index +=1;
                        Parse_step(type);
                        Steptitle();
                        step_main();
                        initStep();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            },1500);
        }
        else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tableLayout.removeAllViews();
                    try {
                        write_step_data();
                        keyword_index -=1;
                        Parse_step(type);
                        Steptitle();
                        step_main();
                        initStep();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            },1500);
        }

    }


    public void ParseData() throws JSONException {
        KeyInfo_array = KeyInfo.split(",");
        for (int i=0; i<KeyInfo_array.length;i++){
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
        }


    }

    public void Parse_step(String type) throws JSONException {
        keyword = keyword_arr[keyword_index];

        String info_keyword = "";
        JSONArray jsonArr = new JSONArray(KeyValuesInfo);
        int count = 0;

        Arrays.fill(step_arr,null);
        Arrays.fill(sopmain_arr,null);

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

        for(int i = 0;i<sopmain_arr.length;i++){
            if(sopmain_arr[i] == null){
                break;
            }
            else {
                sop_len = i;
            }
        }

        if(type.equals("new")){
            now_index = 0;
        }
        else {
            now_index = count -1;
            SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
            String finishStep = sharedPreferences.getString(keyword,"");
            System.out.println("Data:" + keyword + "::" +finishStep);
            finish_step_jsonArray = new JSONArray(finishStep);
            for(int i = 0; i<finish_step_jsonArray.length(); i++){
                finish_step.add(finish_step_jsonArray.getInt(i));
            }
        }


//        System.out.println(keyword);
//        System.out.println("keyword_arr:"+ Arrays.toString(keyword_arr));
//        System.out.println("step_arr:"+ Arrays.toString(step_arr));
//        System.out.println("sopmain_arr:"+ Arrays.toString(sopmain_arr));
    }


    public void sleep() throws InterruptedException {
        Thread.sleep(2000);
    }


    public void Steptitle(){
        Now = "step";
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams (new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        int num = 0;
        for(int i =0; i<step_arr.length; i++){
            String contain = step_arr[i];
            if(contain == null){
                break;
            }else {
                num = i;
            }
        }

        num +=1;

        TextView textViewtitle = new TextView(this);
        textViewtitle.setText(num_chin[keyword_index]+keyword+": "+"   "+"("+String.valueOf(now_index+1) +"/"+String.valueOf(num) + ")");
        textViewtitle.setTextSize(24);
        textViewtitle.setTextColor(Color.parseColor("#FFFF37"));
        textViewtitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewtitle.setPadding(5, 5, 300, 30);
        tableRow.addView(textViewtitle);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }

    public void step_main(){
        tableLayout = findViewById(R.id.tableLayoutProduct);

        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams (new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView textViewmain = new TextView(this);
        textViewmain.setText(String.valueOf(now_index+1)+". "+sopmain_arr[now_index]);
        textViewmain.setTextSize(24);
        textViewmain.setTextColor(Color.parseColor("#FFFF37"));
        textViewmain.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewmain.setPadding(20, 0, 250, 10);
        tableRow.addView(textViewmain);

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }

    public void initStep(){

        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams (new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView textViewStep = new TextView(this);
        textViewStep.setText(step_arr[now_index]);
        textViewStep.setTextSize(24);
        textViewStep.setTextColor(Color.rgb(255,255,255));
        textViewStep.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textViewStep.setPadding(5, 50, 0, 50);
        textViewStep.setWidth(500);
        tableRow.addView(textViewStep);


        if(finish_step.contains(now_index)){
            System.out.println("image in");
            ImageView imageView2 = new ImageView(getApplicationContext());
            imageView2.setImageResource(R.drawable.check);
            imageView2.setMaxWidth(30);
            imageView2.setMaxHeight(30);
            imageView2.setPadding(10,70,0,0);
            tableRow.addView(imageView2);
        }

        tableLayout.addView(tableRow, new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    public void write_step_data() throws JSONException {
        System.out.println("finish_step_jsonArray:"+finish_step_jsonArray.toString());
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyword,finish_step_jsonArray.toString());

        String In_Out = "";
        String finish_keyword = sharedPreferences.getString("finish_keyword","");
        if(sop_len+1 == finish_step.size()){
            if(finish_keyword.equals("")){
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(keyword);
                editor.putString("finish_keyword",jsonArray.toString());
            }
            else {
                JSONArray jsonArray = new JSONArray(finish_keyword);

                for(int i = 0;i<jsonArray.length();i++){
                    if(jsonArray.getString(i).equals(keyword)){
                        In_Out = "In";
                        break;
                    }
                }

                if(In_Out.equals("In") == false){
                    jsonArray.put(keyword);
                    editor.putString("finish_keyword",jsonArray.toString());
                }
            }
        }
        editor.apply();

        finish_step_jsonArray = new JSONArray();
        finish_step.clear();
        System.out.println("finish_keyword:"+finish_keyword);
    }

//    public void Go_Back() throws JSONException {
//        String In_Out = "";
//        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
//        String finish_keyword = sharedPreferences.getString("finish_keyword","");
//        if(sop_len+1 == finish_step.size()){
//            if(finish_keyword.equals("")){
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                JSONArray jsonArray = new JSONArray();
//                jsonArray.put(keyword);
//                editor.putString("finish_keyword",jsonArray.toString());
//                editor.apply();
//            }
//            else {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                JSONArray jsonArray = new JSONArray(finish_keyword);
//
//                for(int i = 0;i<jsonArray.length();i++){
//                    if(jsonArray.getString(i).equals(keyword)){
//                        In_Out = "In";
//                        break;
//                    }
//                }
//
//                if(In_Out.equals("In") == false){
//                    jsonArray.put(keyword);
//                    editor.putString("finish_keyword",jsonArray.toString());
//                    editor.apply();
//                }
//            }
//        }
//
//        Intent intent = new Intent();
//        intent.setClass(KeyStep.this,Main2Activity.class);
//        Bundle bundle = new Bundle();
//        intent.putExtras(bundle);   // 記得put進去，不然資料不會帶過去
//        startActivity(intent);
//    }

//    Runnable runnable = new Runnable(){
//        @Override
//        public void run() {
//            try {
//                CloseableHttpClient httpclient = HttpClients.createDefault();
//                HttpPost httpPost = new HttpPost("http://210.68.227.123:8029/WebServiceFormID.asmx/FormTime");
//                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//                nvps.add(new BasicNameValuePair("FormID", FormID));
//                nvps.add(new BasicNameValuePair("SOPID", keyword));
//                nvps.add(new BasicNameValuePair("AgentID", Agent_ID));
//                nvps.add(new BasicNameValuePair("Status", "start"));
//                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
//                try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
//                    System.out.println(response.getCode() + " " + response.getReasonPhrase());
//                    HttpEntity entity = response.getEntity();
//                    String res_data  = EntityUtils.toString(entity); //response
//                    System.out.println(res_data);
//                }catch (ParseException | IOException e) {
//                    e.printStackTrace();
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };

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
