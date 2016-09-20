package milestone.ewalk.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;

import milestone.ewalk.R;
import milestone.ewalk.util.SharePreferenceUtil;
import milestone.ewalk.widget.dialog.DialogLoading;


public abstract class ActivityBase extends Activity implements  View.OnClickListener {
    public static final int eActivityStart = 0; // 跳转到新的场景
    private int mLayoutCount;
    private int mFocusChangedCount;
    private DialogLoading mDialogLoading;
    protected MyApplication mApplication;
    protected ActivityBase mContext;
    protected SharePreferenceUtil spUtil;

    public ActivityBase() {
        super();
        // TODO Auto-generated constructor stub
        init();
    }

    protected void init() {
        mLayoutCount = 0;
        mFocusChangedCount = 0;
    }

    private OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            onLayout(mLayoutCount);
            mLayoutCount = (mLayoutCount > 1024) ? 32 : (mLayoutCount + 1);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);

        onWindowFocus(mFocusChangedCount);
        mFocusChangedCount = (mFocusChangedCount > 1024) ? 32 : (mFocusChangedCount + 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置无标题栏
        // 强制设定为竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 隐藏软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mContext = this;

        mApplication =  MyApplication.getInstance();
        spUtil = mApplication.getSpUtil();
        mApplication.addActivity(this);

        mDialogLoading = new DialogLoading(this);
        mDialogLoading.setCanceledOnTouchOutside(false);

        // 注册布局回调
        this.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        super.onCreate(savedInstanceState);
    }


    protected void onWindowFocus(int i) {

    }

    protected void onLayout(int i) {

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mDialogLoading.dismiss();  // 防止Dialog has leaked window， 一般原因是Dialog show了以后，Activity退出
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        if (Util.m_debug) {
//            getMenuInflater().inflate(R.menu.main_test, menu);
//        }
//
//        return true;
//    }

    public void showLoadingDialog() {
        if (mDialogLoading != null) {
              mDialogLoading.show();
        }
    }

    public void showLoadingDialog(String content) {
        if (mDialogLoading != null) {
            mDialogLoading.setContent(content);
            mDialogLoading.show();
        }
    }

    public void hideLoadingDialog() {
        mDialogLoading.dismiss();


    }

    public void startA(Class<?> cls, boolean isFinish, boolean isAnime) {
        Intent intent = new Intent(this, cls);

        startA(intent, isFinish, isAnime);
    }


    public void startA(Class<?> cls, Bundle bundle, boolean isFinish, boolean isAnime, boolean isClear) {
        Intent intent = new Intent(this, cls);

        if (bundle != null)
            intent.putExtras(bundle);

        // 跳转的过程中将栈顶的全部清除掉
        // http://handsomeliuyang.iteye.com/blog/1315283
        if (isClear)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startA(intent, isFinish, isAnime);
    }

    public void startA(Class<?> cls, Bundle bundle, boolean isFinish, boolean isAnime, boolean isClear,int resultCode) {
        Intent intent = new Intent(this, cls);

        if (bundle != null)
            intent.putExtras(bundle);

        // 跳转的过程中将栈顶的全部清除掉
        // http://handsomeliuyang.iteye.com/blog/1315283
        if (isClear)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startA(intent, isFinish, isAnime,resultCode);
    }


    /**
     * 可以传递参数的方式
     *
     * @param intent
     * @param isFinish
     * @param isAnime
     */
    public void startA(Intent intent, boolean isFinish, boolean isAnime) {

        this.startActivity(intent);

        if (isFinish) {
            onLeave();
            this.finish();
        }

        if (isAnime) {
            //设置切换动画 [←]←
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    /**
     * 可以传递参数的方式
     *
     * @param intent
     * @param isFinish
     * @param isAnime
     * @param resultcode
     */
    public void startA(Intent intent, boolean isFinish, boolean isAnime,int resultcode) {

        this.startActivityForResult(intent,resultcode);

        if (isFinish) {
            onLeave();
            this.finish();
        }

        if (isAnime) {
            //设置切换动画 [←]←
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        }
    }

    public void finishA(boolean isAnime) {
        onLeave();
        this.finish();

        if (isAnime) {
            //设置切换动画 →[→]
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }
    }

    /**
     * 在所有this.finish之前调用,做一些缓存释放工作
     */
    public void onLeave() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApplication.removeActivity(this);
    }
}
