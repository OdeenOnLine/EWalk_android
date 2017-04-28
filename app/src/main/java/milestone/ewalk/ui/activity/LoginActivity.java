package milestone.ewalk.ui.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.ui.MyApplication;
import milestone.ewalk.util.Util;

/**
 * 登录页面
 * Created by ltf on 2016/5/27.
 */
public class LoginActivity extends ActivityBase{
    private Button btn_login;
    private TextView tv_protocol;
    private EditText edt_name,edt_pwd;
    private String name="";
    private String pwd="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyApplication.getInstance().clearTopActivity();
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        tv_protocol = (TextView) findViewById(R.id.tv_protocol);
        tv_protocol.setText(Html.fromHtml("<u>活动规则</u>"));
        tv_protocol.setOnClickListener(this);
        edt_name = (EditText) findViewById(R.id.edt_name);
        name = spUtil.getUSERNAME();
        edt_name.setText(name);
        edt_pwd = (EditText) findViewById(R.id.edt_pwd);
        pwd = spUtil.getPASSWORD();
        edt_pwd.setText(pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        if(!getIntent().hasExtra("autoLogin") && !name.equals("") && !pwd.equals("")){
            loginTask();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:
                name = edt_name.getText().toString().trim();
                pwd = edt_pwd.getText().toString().trim();
                if(name==null || name.equals("")){
                    Util.Tip(LoginActivity.this, "请输入用户名");
                }else if(pwd==null || pwd.equals("")){
                    Util.Tip(LoginActivity.this, "请输入密码");
                }else{
                    if(Util.isNetworkAvailable(mContext)) {
                        loginTask();
//                        startA(ActivityMain.class, true, true);
                    }
                }
                break;
            case R.id.tv_protocol:
                startA(ActivityProtocol.class,false,true);
                break;
        }
    }


    //新建异步任务登录
    private void loginTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return login();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(LoginActivity.this,"登录失败，请稍后再试");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optString("retNum").equals("0")) {
                            UserBean userBean = new UserBean();
                            userBean.parseJSON(jsonObject);
                            userBean.setName(name);
                            userBean.setPwd(pwd);
                            MyApplication.getInstance().setUserBean(userBean);
                            spUtil.setUSERNAME(name);
                            spUtil.setPASSWORD(pwd);
                            startA(ActivityMain.class, true, true);
                        } else {
                            Util.Tip(LoginActivity.this, jsonObject.optString("retMsg"));
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

    private String login() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("name");
        proInfo.setValue(name);
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("password");
        proInfo.setValue(pwd);
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("open_id");
        proInfo.setValue("");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("auth_name");
        proInfo.setValue("");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.Login,
                        proInfoList
                );

        return jsonData;
    }

    @Override
    public void onBackPressed() {
        MyApplication.getInstance().shutDown();
    }
}
