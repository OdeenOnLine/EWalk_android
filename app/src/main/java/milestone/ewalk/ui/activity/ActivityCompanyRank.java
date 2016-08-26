package milestone.ewalk.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

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
 * Created by ltf on 2016/7/20.
 */
public class ActivityCompanyRank extends ActivityBase{
    private PullToRefreshListView lv_rank;
    private RankAdapter rankAdapter;
    private List<RankBean> rankBeans = new ArrayList<>();
    private int currentPage = 1;
    private UserBean userBean;
    private boolean isMore = false;

    private int myRank=0;
    private boolean isLastPage = false;
    private Handler handler = new Handler();
    private int pageSize=10;
    private int companyId=0;
    private int addition=0;

    private LinearLayout ll_mine;
    private CircularImage iv_poster;
    private ImageView iv_rank;
    private TextView tv_rank,tv_name,tv_core,tv_company_name,tv_wanbu;

    private int step = 0;
    private boolean mine = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_rank);

        companyId = getIntent().getIntExtra("companyId",0);
        addition = getIntent().getIntExtra("addition",0);
        step = getIntent().getIntExtra("step",0);
        mine = getIntent().getBooleanExtra("mine", false);
        initView();
    }

    private void initView() {
        userBean = mApplication.getUserBean();

        ll_mine = (LinearLayout) findViewById(R.id.ll_mine);
        ll_mine.setOnClickListener(this);
        if(mine){
            ll_mine.setVisibility(View.VISIBLE);
        }else{
            ll_mine.setVisibility(View.GONE);
        }
        iv_poster = (CircularImage) findViewById(R.id.iv_poster);
        iv_rank = (ImageView) findViewById(R.id.iv_rank);
        tv_rank = (TextView) findViewById(R.id.tv_rank);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_core = (TextView) findViewById(R.id.tv_core);
        tv_company_name = (TextView) findViewById(R.id.tv_company_name);
        tv_wanbu = (TextView) findViewById(R.id.tv_wanbu);

        lv_rank = (PullToRefreshListView) findViewById(R.id.lv_rank);
        lv_rank.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(isLastPage){
                    handler.post(myRunnar);
                    Util.Tip(mContext, "无更多数据");
                }else{
                    currentPage++;
                    isMore = true;
                    rankInfoTask();
                }
            }
        });
        rankAdapter = new RankAdapter(ActivityCompanyRank.this,rankBeans,1);
        lv_rank.setAdapter(rankAdapter);
        rankInfoTask();
    }

    Runnable myRunnar = new Runnable() {
        @Override
        public void run() {
            lv_rank.onRefreshComplete();
        }
    };

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
                                tv_company_name.setVisibility(View.VISIBLE);
                                tv_company_name.setText(jsonObject.optString("company"));
                                tv_core.setText(jsonObject.optInt("steps")+"");
                                tv_wanbu.setText("万步率:"+ BigDecimalUtil.doubleChange(jsonObject.optDouble("wanbu") * 100, 2) +"%");
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
        proInfo.setValue(addition+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("page");
        proInfo.setValue(currentPage+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("companyId");
        proInfo.setValue(companyId+"");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                    (
                            AndroidConfig.Rank,
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
            case R.id.ll_mine:
                Bundle bundle = new Bundle();
                bundle.putInt("step", step);
                startA(ActivityMine.class, bundle, false, true, false);
                break;
        }
    }
}
