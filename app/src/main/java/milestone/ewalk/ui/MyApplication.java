package milestone.ewalk.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.LinkedList;

import milestone.ewalk.R;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.util.SharePreferenceUtil;


public class MyApplication extends Application {
    private static MyApplication mInstance;
    private LinkedList<Activity> mBaseActivityList = new LinkedList<Activity>();
    private UserBean userBean;
    private static SharePreferenceUtil shareUtils;

	@Override
	public void onCreate() {
		super.onCreate();
        mInstance = this;
        SDKInitializer.initialize(getApplicationContext());

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder() //
                .showImageForEmptyUri(R.drawable.icon_default_poster) //
                .showImageOnFail(R.drawable.icon_default_poster) //
                .cacheInMemory(true) //
                .cacheOnDisk(true) //
                .build();//
        ImageLoaderConfiguration config = new ImageLoaderConfiguration//
                .Builder(getApplicationContext())//
                .defaultDisplayImageOptions(defaultOptions)//
                .discCacheSize(50 * 1024 * 1024)//
                .discCacheFileCount(100)// 缓存一百张图片
                .writeDebugLogs()//
                .build();//
        ImageLoader.getInstance().init(config);

    }

    public static MyApplication getInstance() {
        // TODO Auto-generated method stub
        return mInstance;
    }

    public void addActivity(Activity activity) {
        mBaseActivityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        mBaseActivityList.remove(activity);
    }

    int i;
    long front;
    long later;

    public void shutDown() {
        i++;
        if (i < 2) {
            Toast.makeText(this, "再点一次退出程序", Toast.LENGTH_SHORT).show();
            front = System.currentTimeMillis();
            return;
        }
        if (i >= 2) {
            later = System.currentTimeMillis();
            if (later - front > 2000) {
                Toast.makeText(this, "再点一次退出程序", Toast.LENGTH_SHORT).show();
                front = System.currentTimeMillis();
                i = 1;
            } else {
                i = 0;
                exit();
            }
        }
    }

    public void exit() {


        for (Activity activity : mBaseActivityList) {
            if (activity != null) {
                activity.finish();
            }
        }

    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public SharePreferenceUtil getSpUtil() {

        if (shareUtils == null) {
            shareUtils = new SharePreferenceUtil(mInstance,
                    "Ewalk");
        }

        return shareUtils;
    }
}
