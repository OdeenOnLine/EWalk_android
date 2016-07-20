package milestone.ewalk.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
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
 * 信息反馈
 * Created by ltf on 2016/7/15.
 */
public class ActivityFeedBack extends ActivityBase{
    private EditText edt_title,edt_content;
    private Button btn_submit;
    private String title,content="";
    private UserBean userBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        initView();
    }

    private void initView() {
        userBean = mApplication.getUserBean();
        edt_title = (EditText) findViewById(R.id.edt_title);
        edt_content = (EditText) findViewById(R.id.edt_content);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
            case R.id.btn_submit:
                title = String.valueOf(edt_title.getText()).trim();
                content = String.valueOf(edt_content.getText()).trim();
                if(title.equals("") || content.equals("")){
                    Util.Tip(mContext,"请输入标题及内容");
                }else if(title.length()>10){
                    Util.Tip(mContext,"标题不能超过10个字符");
                }else if(content.length()>100){
                    Util.Tip(mContext,"内容不能超过100个字符");
                }else{
                    showLoadingDialog("提交中");
                    feedbackTask();
                }


                break;
        }
    }


    /**
     * 新建异步任务意见反馈
     */
    private void feedbackTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return feedback();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData!=null){
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            Util.Tip(mContext, "提交成功");
                            edt_title.setText("");
                            edt_content.setText("");
                        }else {
                            Util.Tip(mContext, jsonObject.optString("retMsg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    Util.Tip(mContext, "上传失败");
                }
            }
        }.execute() ;
    }

    /**
     * 意见反馈接口调用
     */
    private String feedback() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("title");
        proInfo.setValue(title);
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("content");
        proInfo.setValue(content);
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.FeedBack,
                        proInfoList
                );

        return jsonData;
    }
}
