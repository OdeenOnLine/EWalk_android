package milestone.ewalk.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.math.BigDecimal;
import java.util.ArrayList;

import milestone.ewalk.R;
import milestone.ewalk.bean.SevenMessageBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.Util;

/**
 * 我的跑步
 * Created by ltf on 2016/5/31.
 */
public class ActivityMyRun extends ActivityBase{
    private UserBean userBean;
    private TextView tv_distance,tv_time,tv_num,tv_kcal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_run);

        initView();

    }

    private void initView() {
        userBean = mApplication.getUserBean();
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_num = (TextView) findViewById(R.id.tv_num);
        tv_kcal = (TextView) findViewById(R.id.tv_kcal);
        getMyRunTask();
    }

    /**
     * 新建异步任务获取我的战绩
     */
    private void getMyRunTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return getMyRun();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext,"获取信息失败，请稍后再试");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optString("retNum").equals("0")) {
                            tv_distance.setText(doubleGet2(jsonObject.optDouble("mile")) + "");
                            double time = jsonObject.optDouble("last") / 60;
                            tv_time.setText(doubleGet2(time) + "");
                            tv_num.setText(jsonObject.optInt("times") + "");
                            tv_kcal.setText(jsonObject.optInt("calory") + "");
                        }else if(jsonObject.optInt("retNum")==2016){
                            Util.Tip(mContext,jsonObject.optString("retMsg"));
                            stopService(ActivityMain.service);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("autoLogin",false);
                            startA(LoginActivity.class,bundle,true,true,true);
                        } else {
                            Util.Tip(mContext, jsonObject.optString("retMsg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute() ;
    }

    /**
     * 2位小数四舍五入
     * @return
     */
    private double doubleGet2(double num){
        BigDecimal b   =   new   BigDecimal(num);
        return b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 我的战绩信息接口调用
     */
    private String getMyRun() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.MyRun,
                        proInfoList
                );

        return jsonData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
            case R.id.btn_run:
                startA(ActivityRunMap.class,null,false,true,true);
                break;
        }
    }
}
