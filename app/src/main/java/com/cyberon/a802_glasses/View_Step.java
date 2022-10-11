package com.cyberon.a802_glasses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class View_Step extends AppCompatActivity {
    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;
    private AlertDialog dialog = null;

    private String FormID;
    private String Agent_ID;

    private String End = "";

    private String keyword;
    private String sopmain_arr[];
    private int sop_len = 0;

    ArrayList<Integer> finish_step = new ArrayList<>();
    JSONArray finish_step_jsonArray = new JSONArray();

    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new View_Step.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {

        public DSpotterDemoHandler(View_Step mainActivity) {

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
                    switch (Recog_text){
                        case "離開":
                            if(sop_len+1 == finish_step_jsonArray.length()){
                                try {
                                    m_oDSpotterRecog.stop();
                                    write_step_data();
                                    Go_Back();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }else {
                                if(End.equals("離開")){
                                    try {
                                        dialog.dismiss();
                                        m_oDSpotterRecog.stop();
                                        write_step_data();
                                        Go_Back();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    dialog.show();
//                                    showToast("尚有未完成步驟，若要離開列表請再說「離開」");
                                    End = "離開";
                                }
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

        setContentView(R.layout.table_view);
        iniDSpotter();
        if(m_oDSpotterRecog.start(false) == 0){
            System.out.println("success");
        }else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        Agent_ID = sharedPreferences.getString("Agent_ID","");
        FormID = sharedPreferences.getString("FormID","");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("尚有未完成步驟，若要離開列表請再說「離開」");
        dialog = builder.create();

//        Thread thread = new Thread(runnable);
//        thread.start();

        Bundle bundle = getIntent().getExtras();
        keyword = bundle.getString("keyword");
        finish_step = bundle.getIntegerArrayList("finish_step");
        sopmain_arr = bundle.getStringArray("sopmain_arr");
        for(int i = 0;i<sopmain_arr.length;i++){
            if(sopmain_arr[i] == null){
                break;
            }
            else {
                sop_len = i;
            }
        }
        list_main();

    }


    public void list_main(){
        TextView textViewtitle = findViewById(R.id.title);
        textViewtitle.setText(keyword);

        TextView textView;
        ImageView imageView;
        try {
            for(int i =0; i<sopmain_arr.length;i++){
                if(sopmain_arr[i] != null){
                    int textID = getResources().getIdentifier("text"+i,"id",getPackageName());
                    int imageID = getResources().getIdentifier("image"+i,"id",getPackageName());
                    textView = findViewById(textID);
                    textView.setText(sopmain_arr[i]);
                    if(finish_step.contains(i)){
                        imageView = findViewById(imageID);
                        imageView.setImageResource(R.drawable.check);
                        finish_step_jsonArray.put(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write_step_data(){
        System.out.println(finish_step_jsonArray.toString());
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyword,finish_step_jsonArray.toString());
        editor.apply();
    }

    public void Go_Back() throws JSONException {
        String In_Out = "";
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        String finish_keyword = sharedPreferences.getString("finish_keyword","");
        if(sop_len+1 == finish_step.size()){
            if(finish_keyword.equals("")){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(keyword);
                editor.putString("finish_keyword",jsonArray.toString());
                editor.apply();
            }
            else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
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
                    editor.apply();
                }
            }
        }
        Intent intent = new Intent();
        intent.setClass(View_Step.this,Main2Activity.class);
        startActivity(intent);
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost("http://210.68.227.123:8029/WebServiceFormID.asmx/FormTime");
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("FormID", FormID));
                nvps.add(new BasicNameValuePair("SOPID", keyword));
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
