package milestone.ewalk.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.serialization.PropertyInfo;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import milestone.ewalk.R;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.CircularImage;

/**
 * 其他人信息
 * Created by ltf on 2016/8/29.
 */
public class ActivityPersonInfo extends ActivityBase{
    private CircularImage iv_poster;
    private TextView tv_union,tv_name,tv_company,tv_height_weight,tv_step,tv_mile,tv_points,tv_kcal;
    private UserBean userBean;
    private int userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        userBean = mApplication.getUserBean();
        userId = getIntent().getIntExtra("userId",0);
        initView();
    }

    private void initView() {
        iv_poster = (CircularImage) findViewById(R.id.iv_poster);
        iv_poster.setImageResource(R.drawable.icon_default_poster);
        iv_poster.setOnClickListener(this);
        tv_union = (TextView) findViewById(R.id.tv_union);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_company = (TextView) findViewById(R.id.tv_company);
        tv_height_weight = (TextView) findViewById(R.id.tv_height_weight);
        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_mile = (TextView) findViewById(R.id.tv_mile);
        tv_points = (TextView) findViewById(R.id.tv_points);
        tv_kcal = (TextView) findViewById(R.id.tv_kcal);
        personInfoTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //新建异步任务个人信息
    private void personInfoTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return personInfo();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext, "获取个人信息失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            userBean.parseJSON(jsonObject);
                            initUser();
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
                    } catch (NetRequestException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute() ;
    }

    private void initUser() {

        ImageLoader.getInstance().displayImage(userBean.getPoster(),iv_poster);
        tv_union.setText(userBean.getUnion());
        tv_name.setText(userBean.getName());
        tv_company.setText(userBean.getCompany());
        tv_height_weight.setText(userBean.getHeight()+"cm,"+userBean.getWeight()+"kg");
        tv_step.setText(userBean.getSteps()+"");
        tv_mile.setText(BigDecimalUtil.doubleChange(userBean.getRecord(),2)+"");
        tv_points.setText(userBean.getPoints()+"");
        double kcal = Util.getCalory(userBean.getWeight(), userBean.getRecord());
        kcal = BigDecimalUtil.doubleChange(kcal, 0);
        tv_kcal.setText(kcal+"");
    }

    private String personInfo() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("userId");
        proInfo.setValue(userId+"");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.PersonInfoById,
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
        }
    }

}
