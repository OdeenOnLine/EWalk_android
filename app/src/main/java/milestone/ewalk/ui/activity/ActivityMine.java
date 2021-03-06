package milestone.ewalk.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.serialization.PropertyInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import milestone.ewalk.widget.dialog.InfoMsgHint;

/**
 * 我的信息
 * Created by ltf on 2016/6/15.
 */
public class ActivityMine extends ActivityBase{
    private ImageView iv_poster;
    private ImageView iv_switch;
    private LinearLayout ll_record,ll_integral;
    private TextView tv_union,tv_name,tv_company,tv_height_weight,tv_step,tv_mile,tv_points,tv_kcal;
    private LinearLayout ll_pwd_change,ll_message_center,ll_version,ll_help;
    private TextView tv_version;
    private TextView tv_logout;

    private boolean isSwitch = true;
    private UserBean userBean;
    private static final int PIC_Select_POSTER_ImageFromLocal = 2;// 头像相册取图
    private int step=0;
    private CircularImage iv_message_hint;
    private String versionName = "";
    private InfoMsgHint msgHint;
    private TextView tv_email;
    private String path = Environment.getExternalStorageDirectory().getPath()+ "/eWalk";//上传步数文件路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);

        userBean = mApplication.getUserBean();
        step = getIntent().getIntExtra("step",0);
        initView();
    }

    private void initView() {
        iv_poster = (ImageView) findViewById(R.id.iv_poster);
        iv_poster.setImageResource(R.drawable.icon_default_poster);
        iv_poster.setOnClickListener(this);
        iv_switch = (ImageView) findViewById(R.id.iv_switch);
        iv_switch.setOnClickListener(this);
        ll_record = (LinearLayout) findViewById(R.id.ll_record);
        ll_record.setOnClickListener(this);
        ll_integral = (LinearLayout) findViewById(R.id.ll_integral);
        ll_integral.setOnClickListener(this);
        tv_union = (TextView) findViewById(R.id.tv_union);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_company = (TextView) findViewById(R.id.tv_company);
        tv_height_weight = (TextView) findViewById(R.id.tv_height_weight);
        tv_step = (TextView) findViewById(R.id.tv_step);
        tv_mile = (TextView) findViewById(R.id.tv_mile);
        tv_points = (TextView) findViewById(R.id.tv_points);
        tv_kcal = (TextView) findViewById(R.id.tv_kcal);
        tv_logout = (TextView) findViewById(R.id.tv_logout);
        tv_logout.setOnClickListener(this);
        ll_pwd_change = (LinearLayout) findViewById(R.id.ll_pwd_change);
        ll_pwd_change.setOnClickListener(this);
        ll_message_center = (LinearLayout) findViewById(R.id.ll_message_center);
        ll_message_center.setOnClickListener(this);
        iv_message_hint = (CircularImage) findViewById(R.id.iv_message_hint);
        ll_version = (LinearLayout) findViewById(R.id.ll_version);
        ll_version.setOnClickListener(this);
        tv_version = (TextView) findViewById(R.id.tv_version);
        ll_help = (LinearLayout) findViewById(R.id.ll_help);
        ll_help.setOnClickListener(this);
        tv_email = (TextView) findViewById(R.id.tv_email);
        tv_email.setOnClickListener(this);

        getVersion();
        personInfoTask();
    }

    /**
     * 获取版本号
     */
    private void getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            versionName = info.versionName;
            tv_version.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ActivityMain.hasNewMsg){
            iv_message_hint.setImageResource(R.color.my_red);;
            iv_message_hint.setVisibility(View.VISIBLE);
        }else{
            iv_message_hint.setVisibility(View.GONE);
        }
    }


    //新建异步任务个人信息
    private void personInfoTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog("");
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

    /**
     * 显示用户信息
     */
    private void initUser() {

        ImageLoader.getInstance().displayImage(userBean.getPoster(),iv_poster);
        tv_union.setText(userBean.getUnion());
        tv_name.setText(userBean.getName());
        tv_company.setText(userBean.getCompany());
        tv_height_weight.setText(userBean.getHeight()+"cm,"+userBean.getWeight()+"kg");
        tv_step.setText(step+"");
        tv_mile.setText(BigDecimalUtil.doubleChange(userBean.getRecord(),2)+"");
        tv_points.setText(BigDecimalUtil.doubleChange(userBean.getPoints(),1)+"");
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

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.PersonInfo,
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
            case R.id.iv_switch:
                if(isSwitch){
                    isSwitch = false;
                    iv_switch.setImageResource(R.drawable.icon_switch_close);
                }else{
                    isSwitch = true;
                    iv_switch.setImageResource(R.drawable.icon_switch_open);
                }
                break;
            case R.id.ll_record:
                startA(ActivityHistoryRecord.class,false,true);
                break;
            case R.id.ll_integral:
                startA(ActivityIntegralInfo.class,false,true);
                break;
            case R.id.iv_poster:
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PIC_Select_POSTER_ImageFromLocal);
                break;
            case R.id.ll_pwd_change:
                startA(ActivityPwdChange.class,false,true);
                break;
            case R.id.tv_logout:
                logoutTask();

                break;
            case R.id.ll_message_center:
                startA(ActivityMessageCenter.class,false,true);
                break;
            case R.id.ll_version:
                updateVersionTask();

//                try {
//                    Intent i = new Intent(Intent.ACTION_VIEW);
//                    i.setData(Uri.parse("market://details?id=com.milestone.wtz"));
//                    startActivity(i);
//                } catch (Exception e) {
//                    Util.Tip(mContext, "您的手机上没有安装Android应用市场");
//                    e.printStackTrace();
//                }
                break;
            case R.id.ll_help:
                startA(ActivityHelpMessage.class,false,true);
                break;
            case R.id.tv_email:

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                String[] tos = { "515173307@qq.com","yayu.li@qq.com" };

                emailIntent.putExtra(Intent.EXTRA_EMAIL, tos);

                emailIntent.putExtra(Intent.EXTRA_TEXT, "步数数据");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "计步");
                File emailFile = new File(path + "/emailSteps.csv");
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(emailFile));
//                emailIntent.setType("image/*");
                emailIntent.setType("message/rfc882");
                Intent.createChooser(emailIntent, "Choose Email Client");
                startActivity(emailIntent);
                break;
        }
    }

    //获取最新版本
    private void updateVersionTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog("");
            }

            @Override
            protected String doInBackground(Void... params) {
                return updateVersion();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext, "获取最新版本失败");
                }else {
                    try {
                        final JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            String version = jsonObject.optString("versionName");
                            final String url = jsonObject.optString("url");
                            if(version.compareTo(versionName)<=0){
//                            if(version.equals(versionName)){
                                Util.Tip(mContext,"当前已是最新版本");
                            }else{
                                msgHint = new InfoMsgHint(mContext,R.style.MyDialog1);
                                msgHint.setContent("有新版本，需要下载更新吗?");
                                msgHint.hideUpLode();
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
                                        Uri uri = Uri.parse(url);
                                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                        startActivity(intent);

                                    }
                                });
                                msgHint.show();
                            }

//                            Util.Log("ltf","jsonObject==========="+jsonObject);
                        } else if(jsonObject.optInt("retNum")==2016){
                            Util.Tip(mContext,jsonObject.optString("retMsg"));
                            stopService(ActivityMain.service);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("autoLogin",false);
                            startA(LoginActivity.class,bundle,true,true,true);
                        }else {
                            Util.Tip(mContext, jsonObject.optString("retMsg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute() ;
    }


    private String updateVersion() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
//        PropertyInfo proInfo = new PropertyInfo();
//        proInfo.setName("token");
//        proInfo.setValue(userBean.getToken());
//        proInfoList.add(proInfo);
        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.UpdateVersion,
                        proInfoList
                );

        return jsonData;
    }



    //新建异步任务退出
    private void logoutTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog("");
            }

            @Override
            protected String doInBackground(Void... params) {
                return logout();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext, "退出登录失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            stopService(ActivityMain.service);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("autoLogin",false);
                            spUtil.setPASSWORD("");
                            startA(LoginActivity.class,bundle,true,true,true);
                        } else if(jsonObject.optInt("retNum")==2016){
                            Util.Tip(mContext,jsonObject.optString("retMsg"));
                            stopService(ActivityMain.service);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("autoLogin",false);
                            startA(LoginActivity.class,bundle,true,true,true);
                        }else {
                            Util.Tip(mContext, jsonObject.optString("retMsg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.execute() ;
    }


    private String logout() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.Logout,
                        proInfoList
                );

        return jsonData;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PIC_Select_POSTER_ImageFromLocal:
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    showLoadingDialog("");
                    changeInfoTask(uri);
                }
                break;
        }
    }

    //新建异步任务修改个人信息
    private void changeInfoTask(final Uri uri) {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog("更新中");
            }

            @Override
            protected String doInBackground(Void... params) {
                return changeInfo(uri);		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                if(jsonData==null){
                    Util.Tip(mContext, "修改头像失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
//                            iv_poster.setImageURI(uri);
                            ImageLoader.getInstance().displayImage(jsonObject.optString("retMsg"),iv_poster);
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



    private String changeInfo(Uri uri) {
        String path= Util.getRealFilePath(mContext,uri);
        String uploadBuffer = "";
        try {
            FileInputStream fis = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];

            int count = 0;
            while((count = fis.read(buffer)) >= 0){
                baos.write(buffer, 0, count);
            }
            Bitmap bitmapSelected = decodeUriAsBitmap(uri);
            int size = 100;
            bitmapSelected.compress(Bitmap.CompressFormat.JPEG, size, baos);
            while (baos.toByteArray().length / 1024 > 100) {
                baos.reset();
                size -= 10;
                bitmapSelected.compress(Bitmap.CompressFormat.JPEG, size, baos);
            }
            uploadBuffer = new String(Base64.encode(baos.toByteArray()));  //
            bitmapSelected.recycle();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Util.Log("ltf","uploadBuffer============="+uploadBuffer);
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("key");
        proInfo.setValue("poster");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("value");
        proInfo.setValue(uploadBuffer);
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("mime");
        proInfo.setValue("jpg");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.ChangeInfo,
                        proInfoList
                );

        return jsonData;
    }

    /**
     * 根据URI获取位图
     *
     * @param uri
     * @return 对应的位图
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
}
