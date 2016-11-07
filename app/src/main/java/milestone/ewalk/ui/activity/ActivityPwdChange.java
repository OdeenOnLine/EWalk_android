package milestone.ewalk.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.util.ArrayList;

import milestone.ewalk.R;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.Util;

/**
 * Created by ltf on 2016/7/6.
 * 密码修改
 */
public class ActivityPwdChange extends ActivityBase{
    private EditText edt_pwd,edt_new_pwd,edt_confirm_pwd;
    private Button btn_edit;
    private String pwd,newPwd,confirmPwd;
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pwd_change);

        initView();
    }

    private void initView() {
        userBean = mApplication.getUserBean();
        edt_pwd = (EditText) findViewById(R.id.edt_pwd);
        edt_new_pwd = (EditText) findViewById(R.id.edt_new_pwd);
        edt_confirm_pwd = (EditText) findViewById(R.id.edt_confirm_pwd);
        btn_edit = (Button) findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
            case R.id.btn_edit:
                pwd = String.valueOf(edt_pwd.getText()).trim();
                newPwd = String.valueOf(edt_new_pwd.getText()).trim();
                confirmPwd = String.valueOf(edt_confirm_pwd.getText()).trim();
                if(pwd.equals("")){
                    Util.Tip(mContext,"请输入原密码");
                }else if(newPwd.equals("")){
                    Util.Tip(mContext,"请输入新密码");
                }else if(confirmPwd.equals("")){
                    Util.Tip(mContext,"请确认密码");
                }else if(!newPwd.equals(confirmPwd)){
                    Util.Tip(mContext,"输入密码不一致");
                }else{
                    pwdChangeTask();
                }

                break;
        }
    }

    //新建异步任务修改密码
    private void pwdChangeTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return pwdChange();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext,"修改密码失败，请稍后再试");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            Util.Tip(mContext,"密码已成功修改");
                            finishA(true);
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

    private String pwdChange() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("pswd1");
        proInfo.setValue(pwd);
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("pswd2");
        proInfo.setValue(newPwd);
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.ResetPswd,
                        proInfoList
                );

        return jsonData;
    }
}
