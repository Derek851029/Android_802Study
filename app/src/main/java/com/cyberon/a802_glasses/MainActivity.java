package com.cyberon.a802_glasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberon.dspotterutility.DSpotterRecog;
import com.cyberon.dspotterutility.DSpotterStatus;
import com.cyberon.engine.LoadLibrary;
import com.google.zxing.Result;

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

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private String[] voice = {"退出軟體"};

    private ZXingScannerView mScannerView;
    private AlertDialog dialog = null;
    private AlertDialog dialog2 = null;

    private Toast toast = null;
    private String toast_show = "";
    private static long oneTime = 0;
    private static long twoTime = 0;

    private ImageButton imageButton = null;
    private TextView ok;
    private TextView cancel;

    private static final int MSG_INITIALIZE_SUCCESS = 2000;
    private DSpotterRecog m_oDSpotterRecog = null;
    private ArrayAdapter<String> m_oCommandBinAdapter = null;

    private String Post;
    private String IP = "http://192.168.2.143:8080";
//    private String IP = "http://210.68.227.123:8029";
//    private String IP = "https://apex-health-care.com:7024";

    private String Agent_ID = "";
    private String UserID = "";
    private String sub_str;
    private String Study_name;
    private String Message = "";
    private String Agent_Name = "";
    private String FormID;
    private String[] QR_data_array;

    // For status callback
    private DSpotterRecog.DSpotterRecogStatusListener m_oRecogStatusListener = new DSpotterRecog.DSpotterRecogStatusListener() {

        @Override
        public void onDSpotterRecogStatusChanged(int nStatus) {
            m_oHandler.sendMessage(m_oHandler.obtainMessage(nStatus, 0, 0));
        }
    };


    private final Handler m_oHandler = new MainActivity.DSpotterDemoHandler(this);

    @SuppressLint("HandlerLeak")
    private class DSpotterDemoHandler extends Handler {


        public DSpotterDemoHandler(MainActivity mainActivity) {

        }

        @Override
        public void handleMessage(android.os.Message msg) {
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
                    String Recog_text = straResult[0].replaceAll("\\s+", "");
                    if (Arrays.asList(voice).contains(Recog_text)) {
                        showToast(Recog_text);
                    }
                    switch (Recog_text) {
//                        case "繼續操作":
//                            if(dialog.isShowing()){
//                                Gomain2();
//                            }
//                            break;
//                        case "第一項":
//                            if(dialog.isShowing()){
//                                clear();
//                            }
//                            break;
                        case "退出軟體":
                            m_oDSpotterRecog.stop();
                            SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            finishAffinity();
                            System.exit(0);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        setContentView(R.layout.activity_main);
        mScannerView = new ZXingScannerView(this);
        //找到介面
        mScannerView = findViewById(R.id.QRCode);
        mScannerView.setAspectTolerance(0.5f);
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("Agent_ID")) {
                String bundleAgent_ID = bundle.getString("Agent_ID");
                Agent_ID = bundleAgent_ID;
                Post = "Study";
                TextView textView = findViewById(R.id.textView_Result);
                textView.setText("請掃描教案QRCode");
            }
        } else {

            CheckLicenseFile();

            LoadLibrary.loadLibrary(getApplicationContext());
        }

        iniDSpotter();
//        initCmdBinSpinner();

        if (m_oDSpotterRecog.start(false) == 0) {
            System.out.println("success");
        } else {
            showToast("語音辨識開啟失敗，請重新開啟或聯絡管理員。");
        }

        imageButton = findViewById(R.id.img_btn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPset();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
//        builder.setMessage("是否接續前次操作"+ "\n" + "1.若繼續請說「繼續操作」"+ "\n" + "2.若重新開始請說「第一項」");
        builder.setView(inflater.inflate(R.layout.loding, null));
        dialog = builder.create();
    }

    public void CheckLicenseFile() {
        //            String fileNames[] = getAssets().list("");
//            System.out.println(Arrays.toString(fileNames));
        InputStream in = null;
        OutputStream out = null;
        File outFile = null;
        try {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            String address = info.getMacAddress().replace(':','_');
            System.out.println("address:"+address);

            String filestr = "CybLicense_" + address + ".bin";
            in = getAssets().open(filestr);
            String outDir = Environment.getExternalStorageDirectory()
                    .getPath() + "/DCIM/";
            outFile = new File(outDir, "CybLicense.bin");
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            outFile = null;

//            in = getAssets().open("CybLicense.bin");
//            String outDir = Environment.getExternalStorageDirectory()
//                    .getPath() + "/DCIM/";
//            outFile = new File(outDir, "CybLicense.bin");
//            out = new FileOutputStream(outFile);
//            copyFile(in, out);
//            in.close();
//            in = null;
//            out.flush();
//            out.close();
//            out = null;
//            outFile = null;

            in = getAssets().open("glasses_802_pack_withTxt.bin");
            outFile = new File(outDir, "glasses_802_pack_withTxt.bin");
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public void IPset(){
        final EditText input = new EditText(this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle("請輸入IP");
        builder2.setView(input);
        builder2.setPositiveButton("確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IP = "http://"+input.getText().toString();
                showToast(IP);
            }
        });
        builder2.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog2.dismiss();
            }
        });
        dialog2 = builder2.create();
        dialog2.show();
    }

    public void Check(){
        m_oDSpotterRecog.stop();
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
//        String finish_keyword = sharedPreferences.getString("finish_keyword","");
//        String FormID_Data = sharedPreferences.getString("FormID","");
//        String Agent_ID_Data = sharedPreferences.getString("Agent_ID","");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Study_Data",sub_str);
        editor.putString("Study_name",Study_name);
        editor.putString("Agent_ID",Agent_ID);
        editor.putString("FormID",FormID);
        editor.putString("IP",IP);
        editor.apply();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("New","New");
        intent.putExtras(bundle);
        intent.setClass(MainActivity.this,Main2Activity.class);
        startActivity(intent);
    }

    public void Gomain2(){
        m_oDSpotterRecog.stop();

        Intent intent = new Intent();
        intent.setClass(MainActivity.this,Main2Activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("New","New");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void clear(){
        m_oDSpotterRecog.stop();
        SharedPreferences sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        editor.putString("Study_Data",sub_str);
        editor.putString("Study_name",Study_name);
        editor.putString("Agent_ID",Agent_ID);
        editor.putString("FormID",FormID);
        editor.putString("IP",IP);
        editor.commit();

        Intent intent = new Intent();
        intent.setClass(MainActivity.this,Main2Activity.class);
        Bundle bundle = new Bundle();
        bundle.putString("New","New");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void Record(){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(IP+"/WebServiceFormID.asmx/FormTime");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("FormID", FormID));
        nvps.add(new BasicNameValuePair("AgentID", Agent_ID));
        nvps.add(new BasicNameValuePair("Status", "start"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
            System.out.println(response.getCode() + " " + response.getReasonPhrase());
            HttpEntity entity = response.getEntity();
            String res_data  = EntityUtils.toString(entity); //response
            System.out.println(res_data);
        }catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
        mScannerView.setAutoFocus(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            Looper.prepare();
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
//                Agent_ID = "000000";
//                UserID = "users";
//                System.out.println(Agent_ID);
//                System.out.println(UserID);
                switch (Post){
                    case "login":

                        HttpPost httpPost = new HttpPost(IP+"/WebServiceLogin.asmx/GlassLogin");
                        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                        nvps.add(new BasicNameValuePair("Agent_ID", Agent_ID));
                        nvps.add(new BasicNameValuePair("UserID", UserID));
                        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                        try(CloseableHttpResponse response = httpclient.execute(httpPost)) {
                            System.out.println(response.getCode() + " " + response.getReasonPhrase());
                            HttpEntity entity = response.getEntity();
                            String res_data  = EntityUtils.toString(entity); //response
                            System.out.println(res_data);
                            if(res_data.indexOf("MSG") > -1){
                                Message = "NoAgent";
                            }
                            else {
                                int index = res_data.indexOf("{");
                                int index2 = res_data.lastIndexOf("}") +1; //要取到}要+1
                                sub_str = res_data.substring(index,index2);

                                JSONObject jsonObject = new JSONObject(sub_str);
                                Agent_Name = jsonObject.getString("Agent_Name");
                                Message = "success";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "study":
                        HttpPost httpPost2 = new HttpPost(IP+"/WebServiceFormID.asmx/GetFormData");
                        List<NameValuePair> nvps2 = new ArrayList<NameValuePair>();
                        nvps2.add(new BasicNameValuePair("FormID", FormID));
                        httpPost2.setEntity(new UrlEncodedFormEntity(nvps2));
                        try(CloseableHttpResponse response = httpclient.execute(httpPost2)) {
                            System.out.println(response.getCode() + " " + response.getReasonPhrase());
                            HttpEntity entity = response.getEntity();
                            String res_data  = EntityUtils.toString(entity); //response
                            System.out.println(res_data);
                            if(res_data.indexOf("MSG") > -1){
                                Message = "NoData";
                            }
                            else {
                                int index = res_data.indexOf("{");
                                int index2 = res_data.lastIndexOf("}") +1; //要取到}要+1
                                sub_str = res_data.substring(index,index2);
                                Message = "success";
                            }

                        }  catch (ParseException e) {
                            Message = "fail";
                            e.printStackTrace();
                        }
                        break;
                }

            } catch(MalformedURLException e){
                Message = "fail";
                e.printStackTrace();
            } catch (IOException e) {
                Message = "fail";
                e.printStackTrace();
            }
        }
    };

    @Override
    public void handleResult(Result rawResult) {

        System.out.println("qrcode:"+rawResult.getText());
        String QR_data = rawResult.getText();
        Boolean comma = QR_data.contains(",");
        if(comma){
            QR_data_array = QR_data.split(",");
            String data2 = QR_data_array[1];
            int data2_len = data2.length();
//            System.out.println(String.valueOf(data2.getBytes().length) +":::::" +String.valueOf(data2_len));
            // 判斷陣列1 是否是中文
            if(data2.getBytes().length == data2_len) {
                Post = "login";

                if(Agent_ID.equals("")){
                    try {
                        Agent_ID = QR_data_array[0];
                        UserID = QR_data_array[1];

                        Thread thread = new Thread(runnable);
                        thread.start();
                        thread.join();
                        if(Message.equals("success")){
                            Message = "";
                            showShortMsg_Time("登入成功，登入人員:"+Agent_Name+"");
                            mScannerView.resumeCameraPreview(this);

                            TextView textView = findViewById(R.id.textView_Result);
                            textView.setText("請掃描教案QRCode");
                        }
                        else if(Message.equals("NoAgent")){
                            Message = "";
                            showShortMsg_Time("登入失敗，請確認QRCode。");
                            mScannerView.resumeCameraPreview(this);
                        }
                        else if(Message.equals("fail")){
                            Message = "";
                            showShortMsg_Time("網路異常，請稍後嘗試。");
                            mScannerView.resumeCameraPreview(this);
                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    showShortMsg_Time("已登入成功，請掃描教案QRCode");
                    mScannerView.resumeCameraPreview(this);
                }
            }
            else {
                if(Agent_ID.equals("")){
                    showShortMsg_Time("請先登入。");
                    mScannerView.resumeCameraPreview(this);
                }
                else {
                    if(Post.equals("study") == false){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });

                        Post = "study";
                        FormID = QR_data_array[0];
                        Study_name = QR_data_array[1];
                        try {
                            Thread thread2 = new Thread(runnable);
                            thread2.start();
                            thread2.join();

                            if (Message.equals("NoData")) {
                                Message = "";
                                showShortMsg_Time("教案不存在，請檢查QRCode內容。");
                                dialog.dismiss();
                                mScannerView.resumeCameraPreview(this);
                            } else if (Message.equals("fail")) {
                                Message = "";
                                showShortMsg_Time("網路異常，請稍後嘗試。");
                                dialog.dismiss();
                                mScannerView.resumeCameraPreview(this);
                            }
                            else {
                                Check();
                                mScannerView.resumeCameraPreview(this);
                            }
                        } catch (InterruptedException e) {
                            mScannerView.resumeCameraPreview(this);
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        else {
            showShortMsg_Time("QRCode錯誤, 請重新掃描。");
            mScannerView.resumeCameraPreview(this);
        }

    }

    //下面全都是語音辨識用
    private void initCmdBinSpinner() {
        File oFile = new File(DSpotterApplication.m_sCmdFileDirectoryPath);
        String[] strBinFileArray = oFile.list(new MainActivity.CmdBinFilter());

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

    public void showToast(String text){
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }

    private void showShortMsg_Time(String msg) {
        if(toast_show.equals("")){
            toast =  Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();
            toast_show = "show";
        }else {
            twoTime = System.currentTimeMillis();
            if(twoTime - oneTime > 3000){
                toast_show = "";
            }
        }
    }

}
