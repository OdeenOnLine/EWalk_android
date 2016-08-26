package milestone.ewalk.ui.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import milestone.ewalk.R;
import milestone.ewalk.adapter.RankAdapter;
import milestone.ewalk.bean.RankBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.CircularImage;

/**
 * 排行榜
 * Created by ltf on 2016/6/14.
 */
public class ActivityRank extends ActivityBase{
    private CircularImage iv_poster;
    private ImageView iv_rank;
    private TextView tv_rank,tv_name,tv_core,tv_company_name,tv_wanbu;
    private TextView tv_my_team;
    private PullToRefreshListView lv_rank;
    private RankAdapter rankAdapter;
    private List<RankBean> rankBeans = new ArrayList<>();
    private ImageView iv_back;
    private TextView tv_person,tv_team;
    private int type = 1;//1:个人 2:团队
    private TextView tv_today,tv_yesterday,tv_week,tv_month;
    private LinearLayout ll_top;
    private ImageView ivBottomLine;
    private int bottomLineWidth;
    private int offset = 0;
    private int position_one;
    private int currIndex = 0;
    private int currentPage = 1;
    private UserBean userBean;
    private boolean isMore = false;

    private int myRank=0;
    private boolean isLastPage = false;
    private Handler handler = new Handler();
    private int pageSize=10;
    private int companyId=0;
    private LinearLayout ll_mine;

    private int step = 0;
    private int myCompanyId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        //页面切换下滑线
        InitWidth();
        initView();
        initData();
    }

    private void initView() {
        userBean = mApplication.getUserBean();
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        tv_person = (TextView) findViewById(R.id.tv_person);
        tv_person.setOnClickListener(this);
        iv_rank = (ImageView) findViewById(R.id.iv_rank);
        tv_rank = (TextView) findViewById(R.id.tv_rank);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_core = (TextView) findViewById(R.id.tv_core);
        tv_company_name = (TextView) findViewById(R.id.tv_company_name);
        tv_wanbu = (TextView) findViewById(R.id.tv_wanbu);
        tv_team = (TextView) findViewById(R.id.tv_team);
        tv_team.setOnClickListener(this);
        tv_today = (TextView) findViewById(R.id.tv_today);
        tv_today.setOnClickListener(new MyOnClickListener(0));
        tv_yesterday = (TextView) findViewById(R.id.tv_yesterday);
        tv_yesterday.setOnClickListener(new MyOnClickListener(1));
        tv_week = (TextView) findViewById(R.id.tv_week);
        tv_week.setOnClickListener(new MyOnClickListener(2));
        tv_month = (TextView) findViewById(R.id.tv_month);
        tv_month.setOnClickListener(new MyOnClickListener(3));
        iv_poster = (CircularImage) findViewById(R.id.iv_poster);
        tv_my_team = (TextView) findViewById(R.id.tv_my_team);
        lv_rank = (PullToRefreshListView) findViewById(R.id.lv_rank);
        lv_rank.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(isLastPage){
                    handler.post(myRunnar);
                    Util.Tip(mContext,"无更多数据");
                }else{
                    currentPage++;
                    isMore = true;
                    rankInfoTask();
                }
            }
        });
        lv_rank.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(type == 2){
                    Bundle bundle = new Bundle();
                    bundle.putInt("companyId",rankBeans.get(i-1).getCompanyId());
                    bundle.putInt("addition",currIndex+1);
                    bundle.putInt("step", step);
                    if(myCompanyId ==rankBeans.get(i-1).getCompanyId() ) {
                        bundle.putBoolean("mine", true);
                    }else{
                        bundle.putBoolean("mine", false);
                    }
                    startA(ActivityCompanyRank.class,bundle,false,true,false);
                }
            }
        });
        rankAdapter = new RankAdapter(ActivityRank.this,rankBeans,type);
        lv_rank.setAdapter(rankAdapter);
        ll_mine = (LinearLayout) findViewById(R.id.ll_mine);
        ll_mine.setOnClickListener(this);
    }

    Runnable myRunnar = new Runnable() {
        @Override
        public void run() {
            lv_rank.onRefreshComplete();
        }
    };

    /**
     * 页面切换下滑线
     */
    private void InitWidth()
    {
        ll_top = (LinearLayout) findViewById(R.id.ll_top);
        ivBottomLine = (ImageView) findViewById(R.id.iv_bottom_line);
        bottomLineWidth = ivBottomLine.getLayoutParams().width;
        ViewTreeObserver vto = ll_top.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ll_top.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int width = ll_top.getWidth();
                offset = (int) ((width / 4.0 - bottomLineWidth) / 2);
                position_one = (int) (width / 4.0);
            }
        });
    }

    private void initData() {
        step = getIntent().getIntExtra("step",0);
        rankInfoTask();
    }


    //新建异步任务排行信息
    private void rankInfoTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return rankInfo();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                lv_rank.onRefreshComplete();
                if(jsonData==null){
                    Util.Tip(mContext, "获取排行信息失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            if(!isMore){
                                rankBeans.clear();
                                myRank = jsonObject.optInt("rank");
                                rankAdapter.setMyRank(myRank);
                                if(myRank==1){
                                    iv_rank.setImageResource(R.drawable.icon_rank_first);
                                    iv_rank.setVisibility(View.VISIBLE);
                                    tv_rank.setVisibility(View.GONE);
                                }else if(myRank==2){
                                    iv_rank.setImageResource(R.drawable.icon_rank_second);
                                    iv_rank.setVisibility(View.VISIBLE);
                                    tv_rank.setVisibility(View.GONE);
                                }else if(myRank==3){
                                    iv_rank.setImageResource(R.drawable.icon_rank_third);
                                    iv_rank.setVisibility(View.VISIBLE);
                                    tv_rank.setVisibility(View.GONE);
                                }else{
                                    iv_rank.setVisibility(View.GONE);
                                    tv_rank.setVisibility(View.VISIBLE);
                                    tv_rank.setText(myRank+"");
                                }
                                ImageLoader.getInstance().displayImage(jsonObject.optString("poster"),iv_poster);
                                tv_name.setText(jsonObject.optString("name"));
                                if(type ==2){
                                    tv_company_name.setVisibility(View.GONE);
                                }else {
                                    tv_company_name.setVisibility(View.VISIBLE);
                                    tv_company_name.setText(jsonObject.optString("company"));
                                }
                                tv_core.setText(jsonObject.optInt("steps")+"");
                                tv_wanbu.setText("万步率:"+ BigDecimalUtil.doubleChange(jsonObject.optDouble("wanbu")*100,2) +"%");
                                myCompanyId = jsonObject.optInt("companyId");
                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("rankList");
                            if(jsonArray!=null && jsonArray.length()>0){
                                if(jsonArray.length()<pageSize){
                                    isLastPage = true;
                                }

                                for(int i=0;i<jsonArray.length();i++){
                                    RankBean rankBean = new RankBean();
                                    rankBean.parseJSON(jsonArray.getJSONObject(i));
                                    rankBeans.add(rankBean);
                                }
                            }
                            rankAdapter.notifyDataSetChanged();
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


    private String rankInfo() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("addition");
        int addition = currIndex+1;
        proInfo.setValue(addition+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("page");
        proInfo.setValue(currentPage+"");
        proInfoList.add(proInfo);

        String jsonData;
        if(type==1) {

            proInfo = new PropertyInfo();
            proInfo.setName("companyId");
            proInfo.setValue(companyId+"");
            proInfoList.add(proInfo);

            jsonData = ConnectWebservice.getInStance().connectEwalk
                    (
                            AndroidConfig.Rank,
                            proInfoList
                    );
        }else{
            jsonData = ConnectWebservice.getInStance().connectEwalk
                    (
                            AndroidConfig.TeamRank,
                            proInfoList
                    );
        }

        return jsonData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
            case R.id.tv_person:
                if(type == 2){
                    type = 1;
                    rankAdapter.setType(type);
                    tv_person.setBackgroundResource(R.drawable.bg_button_blue);
                    tv_person.setTextColor(Color.parseColor("#FFFFFF"));
                    tv_team.setBackgroundResource(0);
                    tv_team.setTextColor(Color.parseColor("#62D4D9"));
                    iv_poster.setVisibility(View.VISIBLE);
                    tv_my_team.setVisibility(View.GONE);

                    isMore = false;
                    currentPage = 1;
                    isLastPage = false;
                    rankInfoTask();
                }
                break;
            case R.id.tv_team:
                if(type == 1){
                    type = 2;
                    rankAdapter.setType(type);
                    tv_person.setBackgroundResource(0);
                    tv_person.setTextColor(Color.parseColor("#62D4D9"));
                    tv_team.setBackgroundResource(R.drawable.bg_button_blue);
                    tv_team.setTextColor(Color.parseColor("#FFFFFF"));
                    iv_poster.setVisibility(View.VISIBLE);
                    tv_my_team.setVisibility(View.VISIBLE);

                    isMore = false;
                    currentPage = 1;
                    isLastPage = false;
                    rankInfoTask();
                }
                break;
            case R.id.ll_mine:
                if(type==1) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("step", step);
                    startA(ActivityMine.class, bundle, false, true, false);
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putInt("companyId",myCompanyId);
                    bundle.putInt("addition",currIndex+1);
                    bundle.putInt("step", step);
                    bundle.putBoolean("mine",true);
                    startA(ActivityCompanyRank.class, bundle, false, true, false);
                }
                break;
        }
    }

    public class MyOnClickListener implements View.OnClickListener
    {
        private int index = 0;

        public MyOnClickListener(int i)
        {
            index = i;
        }

        @Override
        public void onClick(View v) {
            if (currIndex != index){
                Animation animation = null;
                switch (index) {
                    case 0:
                        if (currIndex == 1) {
                            animation = new TranslateAnimation(position_one, 0, 0, 0);
                            tv_today.setTextColor(Color.parseColor("#62D4D9"));
                            tv_yesterday.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 2) {
                            animation = new TranslateAnimation(2 * position_one, 0, 0, 0);
                            tv_today.setTextColor(Color.parseColor("#62D4D9"));
                            tv_week.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 3) {
                            animation = new TranslateAnimation(3 * position_one, 0, 0, 0);
                            tv_today.setTextColor(Color.parseColor("#62D4D9"));
                            tv_month.setTextColor(Color.parseColor("#B5B5B5"));
                        }

                        break;

                    case 1:
                        if (currIndex == 0) {
                            animation = new TranslateAnimation(offset, position_one, 0, 0);
                            tv_yesterday.setTextColor(Color.parseColor("#62D4D9"));
                            tv_today.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 2) {
                            animation = new TranslateAnimation(2 * position_one, position_one, 0, 0);
                            tv_yesterday.setTextColor(Color.parseColor("#62D4D9"));
                            tv_week.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 3) {
                            animation = new TranslateAnimation(3 * position_one, position_one, 0, 0);
                            tv_yesterday.setTextColor(Color.parseColor("#62D4D9"));
                            tv_month.setTextColor(Color.parseColor("#B5B5B5"));
                        }
                        break;
                    case 2:
                        if (currIndex == 0) {
                            animation = new TranslateAnimation(offset, 2 * position_one, 0, 0);
                            tv_week.setTextColor(Color.parseColor("#62D4D9"));
                            tv_today.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 1) {
                            animation = new TranslateAnimation(position_one, 2 * position_one, 0, 0);
                            tv_week.setTextColor(Color.parseColor("#62D4D9"));
                            tv_yesterday.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 3) {
                            animation = new TranslateAnimation(3*position_one, 2 * position_one, 0, 0);
                            tv_week.setTextColor(Color.parseColor("#62D4D9"));
                            tv_month.setTextColor(Color.parseColor("#B5B5B5"));
                        }
                        break;
                    case 3:
                        if (currIndex == 0) {
                            animation = new TranslateAnimation(offset, 3 * position_one, 0, 0);
                            tv_month.setTextColor(Color.parseColor("#62D4D9"));
                            tv_today.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 1) {
                            animation = new TranslateAnimation(position_one, 3 * position_one, 0, 0);
                            tv_month.setTextColor(Color.parseColor("#62D4D9"));
                            tv_yesterday.setTextColor(Color.parseColor("#B5B5B5"));
                        }else if (currIndex == 2) {
                            animation = new TranslateAnimation(2*position_one, 3 * position_one, 0, 0);
                            tv_month.setTextColor(Color.parseColor("#62D4D9"));
                            tv_week.setTextColor(Color.parseColor("#B5B5B5"));
                        }
                        break;
                }
                currIndex = index;
                animation.setFillAfter(true);
                animation.setDuration(300);
                ivBottomLine.startAnimation(animation);

                isMore = false;
                currentPage = 1;
                isLastPage = false;
                rankInfoTask();
            }
        }
    };
}
