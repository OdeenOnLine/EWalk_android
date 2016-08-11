package milestone.ewalk.ui.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.Trace;
import com.baidu.trace.TraceLocation;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import milestone.ewalk.R;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.dialog.InfoMsgHint;
import milestone.ewalk.widget.dialog.InfoSimpleMsgHint;

/**
 * 跑步地图
 * Created by ltf on 2016/6/1.
 */
public class ActivityRunMap extends ActivityBase{
    private UserBean userBean;
    protected static MapView bmapView = null;
    protected static BaiduMap mBaiduMap = null;
    /**
     * 开启轨迹服务监听器
     */
    protected static OnStartTraceListener startTraceListener = null;

    /**
     * 停止轨迹服务监听器
     */
    protected static OnStopTraceListener stopTraceListener = null;
    /**
     * 轨迹服务
     */
    protected static Trace trace = null;
    /**
     * entity标识
     */
    protected static String entityName = null;

    /**
     * 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
     */
    protected static long serviceId = 115120;

    /**
     * 轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
     */
    private int traceType = 2;
    /**
     * 采集周期（单位 : 秒）
     */
    private int gatherInterval = 5;

    /**
     * 打包周期（单位 : 秒）
     */
    private int packInterval = 30;
    /**
     * 轨迹服务客户端
     */
    protected static LBSTraceClient client = null;
    /**
     * Entity监听器
     */
    protected static OnEntityListener entityListener = null;
    private static List<LatLng> pointList = new ArrayList<LatLng>();
    // 路线覆盖物
    private static PolylineOptions polyline = null;
    protected RefreshThread refreshThread = null;
    protected static MapStatusUpdate msUpdate = null;

    private ImageView iv_operate;
    private LinearLayout ll_no_full,ll_full,ll_operate;
    private boolean isFullScreen = false;//是否全屏显示地图
    private double distance = 0;
    private TextView tv_distance1,tv_distance2;
    private Button btn_stop,btn_continue,btn_end;
    private Chronometer chronometer1,chronometer2;
    private long recordingTime = 0;// 记录下来的总时间

    private InfoSimpleMsgHint infoSimpleMsgHint;
    private boolean isRecord = false;
    private long startTime = 0;
    private long endTime = 0;
    private InfoMsgHint msgHint;
    private int temp=0;
    private double calory=0;
    private TextView tv_kcal,tv_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_run_map);

        userBean = mApplication.getUserBean();
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(getApplicationContext());
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
        // 初始化entity标识
        entityName = userBean.getUserId()+"";
        // 初始化轨迹服务
        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);


        initView();
        // 初始化OnEntityListener
        initOnEntityListener();
        initOnStartTraceListener();
        initOnStopTraceListener();
        startTrace();

    }


    /**
     * 初始化组件
     */
    private void initView() {

        // 初始化控件
        bmapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = bmapView.getMap();
        bmapView.showZoomControls(false);

        iv_operate = (ImageView) findViewById(R.id.iv_operate);
        iv_operate.setOnClickListener(this);
        ll_no_full = (LinearLayout) findViewById(R.id.ll_no_full);
        ll_full = (LinearLayout) findViewById(R.id.ll_full);
        ll_operate = (LinearLayout) findViewById(R.id.ll_operate);
        tv_distance1 = (TextView) findViewById(R.id.tv_distance1);
        tv_distance2 = (TextView) findViewById(R.id.tv_distance2);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_end = (Button) findViewById(R.id.btn_end);
        btn_stop.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        btn_end.setOnClickListener(this);
        chronometer1 = (Chronometer) findViewById(R.id.chronometer1);
        chronometer1.setText(FormatMiss(0));
        chronometer1.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer ch) {
                long time = SystemClock.elapsedRealtime()- ch.getBase();
                temp = (int) (time/1000);
                ch.setText(FormatMiss(temp));
            }
        });
        chronometer2 = (Chronometer) findViewById(R.id.chronometer2);
        chronometer2.setText(FormatMiss(0));
        chronometer2.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer ch) {
                long time = SystemClock.elapsedRealtime()- ch.getBase();
                ch.setText(FormatMiss((int) (time/1000)));
            }
        });
        tv_kcal = (TextView) findViewById(R.id.tv_kcal);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
//        onRecordStart(chronometer1);
//        onRecordStart(chronometer2);
    }


    // 将秒转化成小时分钟秒
    public String FormatMiss(int miss){
        String hh=miss/3600>9?miss/3600+"":"0"+miss/3600;
        String  mm=(miss % 3600)/60>9?(miss % 3600)/60+"":"0"+(miss % 3600)/60;
        String ss=(miss % 3600) % 60>9?(miss % 3600) % 60+"":"0"+(miss % 3600) % 60;
        return hh+":"+mm+":"+ss;
    }

    /**
     * 开启轨迹服务
     *
     */
    private void startTrace() {
        // 通过轨迹服务客户端client开启轨迹服务
        client.startTrace(trace, startTraceListener);

    }

    /**
     * 初始化OnStartTraceListener
     */
    private void initOnStartTraceListener() {
        // 初始化startTraceListener
        startTraceListener = new OnStartTraceListener() {

            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            public void onTraceCallback(int arg0, String arg1) {
                // TODO Auto-generated method stub
                if (0 == arg0 || 10006 == arg0 || 10008 == arg0 || 10009 == arg0) {
//                    isTraceStart = true;
                    startRefreshThread(true);
                }
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            public void onTracePushCallback(byte arg0, String arg1) {
                // TODO Auto-generated method stub
                if (0x03 == arg0) {
//                    try {
//                        JSONObject dataJson = new JSONObject(arg1);
//                        if (null != dataJson) {
//                            String mPerson = dataJson.getString("monitored_person");
//                            String action = dataJson.getInt("action") == 1 ? "进入" : "离开";
//                            String date = DateUtils.getDate(dataJson.getInt("time"));
//                            showMessage("监控对象[" + mPerson + "]于" + date + " 【" + action + "】围栏", null);
//                        }
//
//                    } catch (JSONException e) {
//                        // TODO Auto-generated catch block
//                        showMessage("轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]", null);
//                    }
                } else {
//                    showMessage("轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]", null);
                }
            }

        };
    }

    /**
     * 初始化OnStopTraceListener
     */
    private void initOnStopTraceListener() {
        // 初始化stopTraceListener
        stopTraceListener = new OnStopTraceListener() {

            // 轨迹服务停止成功
            public void onStopTraceSuccess() {
                // TODO Auto-generated method stub
                startRefreshThread(false);
            }

            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            public void onStopTraceFailed(int arg0, String arg1) {
                // TODO Auto-generated method stub
                startRefreshThread(false);
            }
        };
    }

    protected void startRefreshThread(boolean isStart) {
        if (null == refreshThread) {
            refreshThread = new RefreshThread();
        }
        refreshThread.refresh = isStart;
        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }
    }

    /**
     * 初始化OnEntityListener
     */
    private void initOnEntityListener() {
        entityListener = new OnEntityListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                // TrackApplication.showMessage("entity请求失败回调接口消息 : " + arg0);
                System.out.println("entity请求失败回调接口消息 : " + arg0);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isRecord) {
                            if (infoSimpleMsgHint == null) {
                                infoSimpleMsgHint = new InfoSimpleMsgHint(mContext, R.style.MyDialog1);
                                infoSimpleMsgHint.setContent("定位失败,无法记录轨迹");
                                infoSimpleMsgHint.setBtnContent("确定");
                                infoSimpleMsgHint.setOKListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        infoSimpleMsgHint.dismiss();
                                        onBackPressed();
                                    }
                                });
                            }
                            if (!infoSimpleMsgHint.isShowing()) {
                                infoSimpleMsgHint.show();
                            }
                        }
                    }
                });
            }



            // 添加entity回调接口
            public void onAddEntityCallback(String arg0) {
                // TODO Auto-generated method stub
//                TrackApplication.showMessage("添加entity回调接口消息 : " + arg0);

            }

            // 查询entity列表回调接口
            @Override
            public void onQueryEntityListCallback(String message) {
                // TODO Auto-generated method stub
                System.out.println("entityList回调消息 : " + message);
            }

            @Override
            public void onReceiveLocation(TraceLocation location) {
                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isRecord){
                            isRecord = true;
                            onRecordStart(chronometer1);
                            onRecordStart(chronometer2);
                            startTime = new Date().getTime()/1000;
                        }
                    }
                });


                showRealtimeTrack(location);
                 System.out.println("获取到实时位置:" + location.toString());

            }

        };
    }

    protected class RefreshThread extends Thread {

        protected boolean refresh = true;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Looper.prepare();
            while (refresh) {
                // 查询实时位置
                queryRealtimeLoc();
                try {
                    Thread.sleep(gatherInterval * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    System.out.println("线程休眠失败");
                }
            }
            Looper.loop();
        }
    }

    /**
     * 查询实时轨迹
     *
     * @param
     */
    private void queryRealtimeLoc() {
        if(client!=null) {
            client.queryRealtimeLoc(serviceId, entityListener);
        }
    }

    /**
     * 显示实时轨迹
     * @param location
     */
    protected void showRealtimeTrack(TraceLocation location) {
        if (null == refreshThread || !refreshThread.refresh) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // showMessage("当前经纬度 : [" + latitude + "," + longitude + "]", null);
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
//            showMessage("当前查询无轨迹点",
//                    null);

        } else {

            LatLng latLng = new LatLng(latitude, longitude);

            if (1 == location.getCoordType()) {
                LatLng sourceLatLng = latLng;
                CoordinateConverter converter = new
                        CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(sourceLatLng);
                latLng =
                        converter.convert();
            }

            pointList.add(latLng);
            if(pointList.size()>=2){
                distance += DistanceUtil. getDistance(pointList.get(pointList.size()-2), latLng);
                //取2位小数四舍五入
                distance = BigDecimalUtil.doubleChange(distance/1000, 3);

                calory = Util.getCalory(userBean.getWeight(),distance);
                calory = BigDecimalUtil.doubleChange(calory, 0);
            }


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_distance1.setText(distance+"");
                    tv_distance2.setText(distance+"");
                    tv_kcal.setText(calory+"");
                    if(distance!=0) {
                        int time = (int) (temp / distance);
                        tv_speed.setText(time/60+"'"+time%60+"\"");
                    }

                }
            });

            // 绘制实时点
            drawRealtimePoint(latLng);

        }

    }

    /**
     * 绘制实时点
     * @param point
     */
    private void drawRealtimePoint(LatLng point) {
        mBaiduMap.clear();

        MapStatus mMapStatus = new MapStatus.Builder().target(point).zoom(19).build();

        msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

        if (pointList.size() >= 2 && pointList.size() <= 10000) {
            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(pointList);
        }

        addMarker();

    }

    /**
     * 添加地图覆盖物
     */
    protected static void addMarker() {

        if (null != msUpdate) {
            mBaiduMap.setMapStatus(msUpdate);
        }

        // 路线覆盖物
        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_operate:
                if(isFullScreen){
                    isFullScreen = false;
                    iv_operate.setImageResource(R.drawable.icon_enlarge);
                    ll_no_full.setVisibility(View.VISIBLE);
                    ll_full.setVisibility(View.INVISIBLE);

                }else{
                    isFullScreen = true;
                    iv_operate.setImageResource(R.drawable.icon_narrow);
                    ll_no_full.setVisibility(View.GONE);
                    ll_full.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_stop:
                btn_stop.setVisibility(View.GONE);
                ll_operate.setVisibility(View.VISIBLE);
                onRecordPause(chronometer1);
                onRecordPause(chronometer2);
                break;
            case R.id.btn_continue:
                btn_stop.setVisibility(View.VISIBLE);
                ll_operate.setVisibility(View.GONE);
                onRecordStart(chronometer1);
                onRecordStart(chronometer2);
                break;
            case R.id.btn_end:
                onBackPressed();
                break;
        }
    }

    public void onRecordStart(Chronometer recordChronometer) {
        recordChronometer.setBase(SystemClock.elapsedRealtime() - recordingTime);// 跳过已经记录了的时间，起到继续计时的作用
        recordChronometer.start();
    }

    public void onRecordPause(Chronometer recordChronometer) {
        recordChronometer.stop();
        recordingTime = SystemClock.elapsedRealtime()- recordChronometer.getBase();// 保存这次记录了的时间
    }

    @Override
    public void onBackPressed() {
        if(isRecord){
            msgHint = new InfoMsgHint(mContext,R.style.MyDialog1);
            msgHint.setContent("确定结束本次跑步吗?");
            msgHint.setCancelListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    msgHint.dismiss();
                }
            });
            msgHint.setOKListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    msgHint.dismiss();
                    client.stopTrace(trace, stopTraceListener);
                    client.onDestroy();
                    client = null;
                    pointList.clear();
                    finishA(true);
                }
            });
            msgHint.setupLoadListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    endTime = new Date().getTime() / 1000;
                    uploadRecordTask();
                }
            });
            msgHint.show();
        }else{
            client.stopTrace(trace, stopTraceListener);
            client.onDestroy();
            client = null;
            pointList.clear();
            finishA(true);
        }
    }

    /**
     * 新建异步任务上传跑步记录
     */
    private void uploadRecordTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return uploadRecord();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData!=null){
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optString("retNum").equals("0")) {
                            msgHint.dismiss();
                            client.stopTrace(trace, stopTraceListener);
                            client.onDestroy();
                            client = null;
                            pointList.clear();
                            finishA(true);
                        }else{
                            Util.Tip(mContext,jsonObject.optString("retMsg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Util.Tip(mContext,"上传失败！");
                }

            }
        }.execute() ;
    }

    /**
     * 上传跑步记录接口调用
     */
    private String uploadRecord() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("mile");
        proInfo.setValue(distance+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("start_time");
        proInfo.setValue(startTime+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("end_time");
        proInfo.setValue(endTime+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("last");
        proInfo.setValue(temp+"");
        proInfoList.add(proInfo);


        proInfo = new PropertyInfo();
        proInfo.setName("calory");
        proInfo.setValue((int)calory+"");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.UploadRecord,
                        proInfoList
                );

        return jsonData;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        bmapView.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        bmapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        bmapView.onPause();
    }
}
