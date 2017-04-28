package milestone.ewalk.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.util.LinkedList;
import java.util.List;

import milestone.ewalk.R;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.util.SharePreferenceUtil;
import milestone.ewalk.util.Util;


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
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration//
//                .Builder(getApplicationContext())//
//                .defaultDisplayImageOptions(defaultOptions)//
//                .discCacheSize(50 * 1024 * 1024)//
//                .discCacheFileCount(100)// 缓存一百张图片
//                .writeDebugLogs()//
//                .build();//

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getApplicationContext())
                .memoryCacheExtraOptions(480, 800) //即保存的每个缓存文件的最大长宽
                .threadPoolSize(3) //线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                //解释：当同一个Uri获取不同大小的图片，缓存到内存时，只缓存一个。默认会缓存多个不同的大小的相同图片
                .denyCacheImageMultipleSizesInMemory()  //拒绝缓存多个图片。
                .memoryCache(new LruMemoryCache(5 * 1024 * 1024)) //缓存策略你可以通过自己的内存缓存实现 ，这里用弱引用，缺点是太容易被回收了，不是很好！
                .memoryCacheSize(5 * 1024 * 1024) //设置内存缓存的大小
                .diskCacheSize(50 * 1024 * 1024) //设置磁盘缓存大小 50M
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()) //将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO) //设置图片下载和显示的工作队列排序
                .diskCacheFileCount(100) //缓存的文件数量
                .defaultDisplayImageOptions(defaultOptions) //显示图片的参数，默认：DisplayImageOptions.createSimple()
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() //打开调试日志
                .build();//开始构建
        ImageLoader.getInstance().init(config);

//        getSensorList();
    }


    private void getSensorList() {
        // 获取传感器管理器
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 获取全部传感器列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // 打印每个传感器信息
        StringBuilder strLog = new StringBuilder();
        int iIndex = 1;
        for (Sensor item : sensors) {
            strLog.append("	Sensor Name - " + item.getName() + "\r\n");
            iIndex++;
        }
        Log.e("ltf", "strLog.toString()===" + strLog.toString());
        System.out.println(strLog.toString());
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

    public void clearTopActivity(){
        for(int i=0;i<mBaseActivityList.size()-1;i++){
            if (mBaseActivityList.get(i) != null) {
                mBaseActivityList.get(i).finish();
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
