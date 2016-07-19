package milestone.ewalk.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import milestone.ewalk.R;
import milestone.ewalk.bean.SevenMessageBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.service.StepCounterService;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.ui.MyApplication;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.CircularImage;
import milestone.ewalk.widget.SpringProgressView;

/**
 * Created by ltf on 2016/3/30.
 */
public class ActivityMain extends ActivityBase{
    public static Intent service;
    private Button btn_run;
    private SpringProgressView progress1,progress2,progress3,progress4,progress5,progress6,progress7;
    private TextView tv_day1,tv_day2,tv_day3,tv_day4,tv_day5,tv_day6,tv_day7;
    private TextView tv_rank;
    private TextView tv_feedback,tv_mine;
    private UserBean userBean;
    private SevenMessageBean sevenMessageBean;
    private TextView tv_step,tv_distance,tv_calory,tv_time,tv_average_step;
    private String path = Environment.getExternalStorageDirectory().getPath()+ "/eWalk";
    private File file;
    private String[] steps;
    private int step=0;
    private int averageStep = 0;
    private ImageView iv_walk_line,iv_average_line;
    private Timer timer;
    private CircularImage iv_rank_hint;

    private NewsBrocastReceiver brocastReceiver;

    class NewsBrocastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getMessageTask();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        brocastReceiver = new NewsBrocastReceiver();
        registerReceiver(brocastReceiver, new IntentFilter("StepRefresh"));
        initView();
        initData();
    }

    private void initView() {
        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_calory = (TextView) findViewById(R.id.tv_calory);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_average_step = (TextView) findViewById(R.id.tv_average_step);
        progress1 = (SpringProgressView) findViewById(R.id.progress1);
        progress2 = (SpringProgressView) findViewById(R.id.progress2);
        progress3 = (SpringProgressView) findViewById(R.id.progress3);
        progress4 = (SpringProgressView) findViewById(R.id.progress4);
        progress5 = (SpringProgressView) findViewById(R.id.progress5);
        progress6 = (SpringProgressView) findViewById(R.id.progress6);
        progress7 = (SpringProgressView) findViewById(R.id.progress7);
        tv_day1 = (TextView) findViewById(R.id.tv_day1);
        tv_day2 = (TextView) findViewById(R.id.tv_day2);
        tv_day3 = (TextView) findViewById(R.id.tv_day3);
        tv_day4 = (TextView) findViewById(R.id.tv_day4);
        tv_day5 = (TextView) findViewById(R.id.tv_day5);
        tv_day6 = (TextView) findViewById(R.id.tv_day6);
        tv_day7 = (TextView) findViewById(R.id.tv_day7);
        tv_rank = (TextView) findViewById(R.id.tv_rank);
        tv_rank.setOnClickListener(this);
        btn_run = (Button) findViewById(R.id.btn_run);
        btn_run.setOnClickListener(this);
        service = new Intent(this, StepCounterService.class);
        tv_feedback = (TextView) findViewById(R.id.tv_feedback);
        tv_feedback.setOnClickListener(this);
        tv_mine = (TextView) findViewById(R.id.tv_mine);
        tv_mine.setOnClickListener(this);
        iv_walk_line = (ImageView) findViewById(R.id.iv_walk_line);
        iv_average_line = (ImageView) findViewById(R.id.iv_average_line);
        iv_rank_hint = (CircularImage) findViewById(R.id.iv_rank_hint);
    }

    private void initData() {
        startService(service);
        userBean = mApplication.getUserBean();
        StepCounterService.userBean = userBean;
        Calendar c = Calendar.getInstance(); // 当时的日期和时间
        int day = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, day-1);
        tv_day7.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-2);
        tv_day6.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-3);
        tv_day5.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-4);
        tv_day4.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-5);
        tv_day3.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-6);
        tv_day2.setText(c.get(Calendar.DAY_OF_MONTH)+"日");
        c.set(Calendar.DAY_OF_MONTH, day-7);
        tv_day1.setText(c.get(Calendar.DAY_OF_MONTH)+"日");

        if(StepCounterService.isRankUpdate){
            iv_rank_hint.setImageResource(R.color.my_red);;
            iv_rank_hint.setVisibility(View.VISIBLE);
        }else{
            iv_rank_hint.setVisibility(View.GONE);
        }

        getMessageTask();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initTodayMessage();
                    }
                });

            }
        },new Date(),10*1000);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    private void initTodayMessage() {
        step = (int) StepCounterService.dayDetector;
        if(steps!=null){
            step += Integer.parseInt(steps[steps.length-1]);
        }


        tv_step.setText(step+"");
        double distance  = step * 0.55/1000;
        double kcal = Util.getCalory(userBean.getWeight(),distance);
        distance = BigDecimalUtil.doubleChange(distance,3);
        kcal = BigDecimalUtil.doubleChange(kcal,0);
        tv_distance.setText(distance+"公里");
        tv_calory.setText(kcal+"千卡");
//        if(StepCounterService.startTime!=0){
//            int time = (int) ((System.currentTimeMillis()-StepCounterService.startTime)/1000);
//
//            int h= (int) (time/3600);
//            int m= (int) ((time-h*3600)/60);
//            int s= (int) ((time-h*3600)%60);
//            tv_time.setText(h + "时"+m + "分");
//        }



    }

    /**
     * 新建异步任务获取7天信息
     */
    private void getMessageTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return getMessage();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    if(jsonObject.optInt("retNum")==0){
                        sevenMessageBean = new SevenMessageBean();
                        sevenMessageBean.parseJSON(jsonObject);
                        initMessage();
                    }else if(jsonObject.optInt("retNum")==2016){
                        Util.Tip(mContext,jsonObject.optString("retMsg"));
                        stopService(ActivityMain.service);
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("autoLogin",false);
                        startA(LoginActivity.class,bundle,true,true,true);
                    }else{
                        Util.Tip(mContext,jsonObject.optString("retMsg"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NetRequestException e) {
                    e.printStackTrace();
                }


            }
        }.execute() ;
    }

    /**
     * 显示历史信息
     */
    private void initMessage() {
        String[] titlesArray = sevenMessageBean.getSteps();
        steps = sevenMessageBean.getSteps();
        int dayStep1 = Integer.parseInt(steps[0]);
        int dayStep2 = Integer.parseInt(steps[1]);
        int dayStep3 = Integer.parseInt(steps[2]);
        int dayStep4 = Integer.parseInt(steps[3]);
        int dayStep5 = Integer.parseInt(steps[4]);
        int dayStep6 = Integer.parseInt(steps[5]);
        int dayStep7 = Integer.parseInt(steps[6]);
        averageStep = (dayStep1+dayStep2+dayStep3+dayStep4+dayStep5+dayStep6+dayStep7)/7;
        tv_average_step.setText("平均"+averageStep+"步");
        if(averageStep!=0){
            Arrays.sort(titlesArray);
            int maxStep = Integer.parseInt(steps[6]);
            progress1.setCurrentCount(dayStep1*100/maxStep);
            progress2.setCurrentCount(dayStep2*100/maxStep);
            progress3.setCurrentCount(dayStep3*100/maxStep);
            progress4.setCurrentCount(dayStep4*100/maxStep);
            progress5.setCurrentCount(dayStep5*100/maxStep);
            progress6.setCurrentCount(dayStep6*100/maxStep);
            progress7.setCurrentCount(dayStep7*100/maxStep);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv_average_line.getLayoutParams();
            params.bottomMargin = averageStep*progress1.getHeight()/maxStep;
            iv_average_line.setLayoutParams(params);
        }

        int time = (int) sevenMessageBean.getToday_last();
        int h= (int) (time/3600);
        int m= (int) ((time-h*3600)/60);
        int s= (int) ((time-h*3600)%60);
        tv_time.setText(h + "时"+m + "分");
        initTodayMessage();
    }






    /**
     * 获取7天信息接口调用
     */
    private String getMessage() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.SevenMessage,
                        proInfoList
                );

        return jsonData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_rank:
                iv_rank_hint.setVisibility(View.GONE);
                StepCounterService.isRankUpdate = false;
                float nowStep = StepCounterService.mDetector;
                if(nowStep!=0) {
                    saveDataToCSV(nowStep);
                }else {
                    startA(ActivityRank.class, false, true);
                }
//                file = new File(path + "/steps.csv");
//                uploadStepTask();
                break;
            case R.id.btn_run:
                startA(ActivityMyRun.class,false,true);
                break;
            case R.id.tv_mine:
                Bundle bundle = new Bundle();
                bundle.putInt("step",step);
                startA(ActivityMine.class,bundle,false,true,false);
                break;
            case R.id.tv_feedback:
                startA(ActivityFeedBack.class,false,true);
                break;
        }
    }

    /**
     * 将步数存入csv文件
     */
    private void saveDataToCSV(float nowStep) {
        try {
            File csv = new File(path); // CSV数据文件
            if(!csv.exists()){
                csv.mkdir();
            }
            file = new File(path + "/steps.csv");
            if(!file.exists()){//判断文件是否存在（不存在则创建这个文件）
                file.createNewFile();//创建文件
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true)); // 附加
            // 添加新的数据行
//            bw.write("\"李四\"" + "," + "\"1988\"" + "," + "\"1992\"");
            double distance  = nowStep * 0.55/1000;
            double kcal = Util.getCalory(userBean.getWeight(),distance);
            kcal = BigDecimalUtil.doubleChange(kcal,0);
            bw.write(new Date().getTime()/1000+","+(int)nowStep+","+distance+","+kcal);
            bw.newLine();
            bw.close();
            StepCounterService.mDetector -= nowStep;

            uploadStepTask();
        } catch (FileNotFoundException e) {
            // File对象的创建过程中的异常捕获
            e.printStackTrace();
        } catch (IOException e) {
            // BufferedWriter在关闭对象捕捉异常
            e.printStackTrace();
        }
    }

    /**
     * 新建异步任务上传步数
     */
    private void uploadStepTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return uploadStep();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData!=null){
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optString("retNum").equals("0")) {
                            file.delete();
                            sendBroadcast(new Intent("StepRefresh"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                startA(ActivityRank.class, false, true);

            }
        }.execute() ;
    }

    /**
     * 上传步数接口调用
     */
    private String uploadStep() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        String strBuffer=null;
        try {
            strBuffer = encodeBase64File(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("ltf","strBuffer=========="+strBuffer);
        proInfo = new PropertyInfo();
        proInfo.setName("steps");
        proInfo.setValue(strBuffer);
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.UploadSteps,
                        proInfoList
                );

        return jsonData;
    }

    private  String encodeBase64File(File file) throws Exception {
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(service);
        if(timer!=null){
            timer.cancel();
        }
        unregisterReceiver(brocastReceiver);
    }

    @Override
    public void onBackPressed() {
        MyApplication.getInstance().shutDown();
    }
}