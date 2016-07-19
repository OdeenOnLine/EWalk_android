package milestone.ewalk.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.activity.ActivityRank;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;

/**
 * Created by ltf on 2016/3/30.
 */

public class StepCounterService extends Service implements SensorEventListener{

    public static Boolean FLAG = false;// 服务运行标志
    public static Boolean isRankUpdate = true;// 排行榜更新

    private SensorManager mSensorManager;// 传感器服务
    private PowerManager mPowerManager;// 电源管理服务
    private PowerManager.WakeLock mWakeLock;// 屏幕灯

    private Sensor mStepCount;
    private Sensor mStepDetector;
    public static float mCount;//步行总数
    public static float mDetector;//步行探测器
    private static final int sensorTypeD=Sensor.TYPE_STEP_DETECTOR;
    private static final int sensorTypeC=Sensor.TYPE_STEP_COUNTER;
    private Timer timer;
    private Timer dayTimer;
    public static UserBean userBean;
    private BroadcastReceiver mBR;
    private String path = Environment.getExternalStorageDirectory().getPath()+ "/eWalk";
    private File file;
    public static float dayDetector;//当天步数
    public static float startTime=0;


    @Override
    public IBinder onBind(Intent intent) {
        // TODOAuto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODOAuto-generated method stub
        super.onCreate();


        mBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                Intent a = new Intent(StepCounterService.this, StepCounterService.class);
                startService(a);
            }
        };
        IntentFilter mIF = new IntentFilter();
        mIF.addAction("listener");
        registerReceiver(mBR, mIF);

        FLAG = true;// 标记为服务正在运行
        // 获取传感器的服务，初始化传感器
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mStepCount = mSensorManager.getDefaultSensor(sensorTypeC);
        mStepDetector = mSensorManager.getDefaultSensor(sensorTypeD);
        // 电源管理服务
        mPowerManager = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "S");
        mWakeLock.acquire();
        register();

        Calendar c = Calendar.getInstance();
        final int minute = c.get(Calendar.MINUTE);
        if(minute>=58){
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Util.Log("ltf","user==="+userBean.getToken());
                    if(mDetector!=0) {
                        saveDataToCSV(mDetector);
                    }
                }
            },new Date(), 60*60 * 1000);
        }else {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(mDetector!=0) {
                        saveDataToCSV(mDetector);
                    }
                }
            }, (58 - minute)*60*1000, 60*60 * 1000);
        }

        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay  = getTimeMillis("00:00:00") - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        dayTimer = new Timer();
        dayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isRankUpdate = true;
                dayDetector = 0;
                startTime=0;
            }
        },initDelay,24*60*60*1000);
    }

    /**
     * 获取指定时间对应的毫秒数
     * @param time "HH:mm:ss"
     * @return
     */
    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void register(){

        register(mStepCount, SensorManager.SENSOR_DELAY_FASTEST);
        register(mStepDetector, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unRegister(){
        mSensorManager.unregisterListener(this);
    }

    private void register(Sensor sensor,int rateUs) {
        mSensorManager.registerListener(this, sensor, rateUs);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()==sensorTypeC) {
            mCount = event.values[0];
        }
        if (event.sensor.getType()==sensorTypeD) {
            if (event.values[0]==1.0) {
                mDetector++;
                dayDetector++;
                if(startTime==0){
                    startTime = System.currentTimeMillis();
                }
            }
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
            kcal = BigDecimalUtil.doubleChange(kcal, 0);
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

            }

            @Override
            protected String doInBackground(Void... params) {
                return uploadStep();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
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
    public void onDestroy() {
        // TODOAuto-generated method stub
        super.onDestroy();
        FLAG = false;// 服务停止
        unRegister();
        if(timer!=null){
            timer.cancel();
            timer =null;
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }


        Intent intent = new Intent();
        intent.setAction("listener");
        sendBroadcast(intent);

        unregisterReceiver(mBR);

    }
}
