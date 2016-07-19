package milestone.ewalk.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.TraceLocation;

import java.util.ArrayList;
import java.util.List;

import milestone.ewalk.R;
import milestone.ewalk.bean.RunRecordBean;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.GsonService;
import milestone.ewalk.util.HistoryTrackData;
import milestone.ewalk.util.Util;

/**
 * 运动记录轨迹
 * Created by ltf on 2016/6/15.
 */
public class ActivityRunMapRecord extends ActivityBase{
    protected static MapView bmapView = null;
    protected static BaiduMap mBaiduMap = null;
    //鹰眼服务ID
    long serviceId = 115120;
    //entity标识
    String entityName = "falsesheep";
    //是否返回精简的结果（0 : 将只返回经纬度，1 : 将返回经纬度及其他属性信息）
    int simpleReturn = 0;
    //开始时间（Unix时间戳）
    int startTime = 1464796800;
    //结束时间（Unix时间戳）
    int endTime = 1464883200;
    //分页大小
    int pageSize = 5000;
    //分页索引
    int pageIndex = 1;
    /**
     * 轨迹服务客户端
     */
    protected static LBSTraceClient client = null;
    private static List<LatLng> pointList = new ArrayList<LatLng>();
    // 路线覆盖物
    private static PolylineOptions polyline = null;
    protected static MapStatusUpdate msUpdate = null;
    // 起点图标
    private static BitmapDescriptor bmStart;
    // 终点图标
    private static BitmapDescriptor bmEnd;

    // 起点图标覆盖物
    private static MarkerOptions startMarker = null;
    // 终点图标覆盖物
    private static MarkerOptions endMarker = null;

    private RunRecordBean runRecordBean;

    private TextView tv_speed,tv_mile,tv_kcal,tv_last;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_run_map_record);

        initView();
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(getApplicationContext());
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);

        entityName = mApplication.getUserBean().getUserId()+"";
        runRecordBean = (RunRecordBean) getIntent().getSerializableExtra("runRecordBean");
        tv_mile.setText(runRecordBean.getMile()+"公里");
        tv_kcal.setText(runRecordBean.getCalory()+"千卡");
        int last = (int) runRecordBean.getLast();
        tv_last.setText(FormatMiss(last));
        if(runRecordBean.getMile()!=0) {
            int time = (int) (last / runRecordBean.getMile());
            tv_speed.setText(time/60+"'"+time%60+"\"");
        }else{
            tv_speed.setText("0'0\"");
        }



        startTime = (int) runRecordBean.getStart_time();
        endTime = (int) runRecordBean.getEnd_time();
        Util.Log("ltf",entityName+"===="+startTime+"==="+endTime);
        //查询历史轨迹
        client.queryHistoryTrack(serviceId, entityName, simpleReturn, startTime, endTime, pageSize,pageIndex,trackListener);
//        client.queryRealtimeLoc(serviceId, entityListener);
    }

    // 将秒转化成小时分钟秒
    public String FormatMiss(int miss){
        String hh=miss/3600>9?miss/3600+"":"0"+miss/3600;
        String  mm=(miss % 3600)/60>9?(miss % 3600)/60+"":"0"+(miss % 3600)/60;
        String ss=(miss % 3600) % 60>9?(miss % 3600) % 60+"":"0"+(miss % 3600) % 60;
        return hh+":"+mm+":"+ss;
    }

    /**
     * 初始化组件
     */
    private void initView() {
        // 初始化控件
        bmapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = bmapView.getMap();
        bmapView.showZoomControls(false);
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(15);
        mBaiduMap.animateMapStatus(u);

        tv_last = (TextView) findViewById(R.id.tv_last);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_mile = (TextView) findViewById(R.id.tv_mile);
        tv_kcal = (TextView) findViewById(R.id.tv_kcal);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
        }
    }

    //轨迹查询监听器
    OnTrackListener trackListener = new OnTrackListener() {
        //请求失败回调接口
        @Override
        public void onRequestFailedCallback(String arg0) {
            System.out.println("track请求失败回调接口消息 : " + arg0);
        }

        // 查询历史轨迹回调接口
        @Override
        public void onQueryHistoryTrackCallback(String arg0) {
            System.out.println("查询历史轨迹回调接口消息 : " + arg0);
            showHistoryTrack(arg0);
        }

    };

    /**
     * 显示历史轨迹
     *
     * @param historyTrack
     */
    protected void showHistoryTrack(String historyTrack) {

        HistoryTrackData historyTrackData = GsonService.parseJson(historyTrack,
                HistoryTrackData.class);

        List<LatLng> latLngList = new ArrayList<LatLng>();
        if (historyTrackData != null && historyTrackData.getStatus() == 0) {
            if (historyTrackData.getListPoints() != null) {
                latLngList.addAll(historyTrackData.getListPoints());
            }

            // 绘制历史轨迹
            drawHistoryTrack(latLngList, historyTrackData.distance);

        }

    }

    /**
     * 绘制历史轨迹
     *
     * @param points
     */
    private void drawHistoryTrack(final List<LatLng> points, final double distance) {
        // 绘制新覆盖物前，清空之前的覆盖物
        mBaiduMap.clear();

        if (points == null || points.size() == 0) {
            Looper.prepare();
            Toast.makeText(ActivityRunMapRecord.this, "当前查询无轨迹点", Toast.LENGTH_SHORT).show();
            Looper.loop();
            resetMarker();
        } else if (points.size() > 1) {

            LatLng llC = points.get(0);
            LatLng llD = points.get(points.size() - 1);
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(llC).include(llD).build();

            msUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);



            bmStart = BitmapDescriptorFactory.fromResource(R.drawable.icon_map_start);
            bmEnd = BitmapDescriptorFactory.fromResource(R.drawable.icon_map_end);

            // 添加起点图标
            startMarker = new MarkerOptions()
                    .position(points.get(points.size() - 1)).icon(bmStart)
                    .zIndex(9).draggable(true);

            // 添加终点图标
            endMarker = new MarkerOptions().position(points.get(0))
                    .icon(bmEnd).zIndex(9).draggable(true);

            // 添加路线（轨迹）
            polyline = new PolylineOptions().width(10)
                    .color(Color.RED).points(points);

            addMarker();

//            Looper.prepare();
//            Toast.makeText(getActivity(), "当前轨迹里程为 : " + (int) distance + "米", Toast.LENGTH_SHORT).show();
//            Looper.loop();

        }

    }

    /**
     * 添加覆盖物
     */
    protected void addMarker() {

        if (null != msUpdate) {
            mBaiduMap.setMapStatus(msUpdate);
        }

        if (null != startMarker) {
            mBaiduMap.addOverlay(startMarker);
        }

        if (null != endMarker) {
            mBaiduMap.addOverlay(endMarker);
        }

        if (null != polyline) {
            mBaiduMap.addOverlay(polyline);
        }

    }

    /**
     * 重置覆盖物
     */
    private void resetMarker() {
        startMarker = null;
        endMarker = null;
        polyline = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
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
