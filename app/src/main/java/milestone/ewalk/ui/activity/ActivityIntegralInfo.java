package milestone.ewalk.ui.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.util.ArrayList;
import java.util.List;

import milestone.ewalk.R;
import milestone.ewalk.bean.IntegralBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.BigDecimalUtil;
import milestone.ewalk.util.Util;

/**
 * Created by ltf on 2016/10/13.
 */
public class ActivityIntegralInfo extends ActivityBase{
    private PullToRefreshListView lv_integral;
    private int currentPage = 1;
    private UserBean userBean;
    private boolean isMore = false;
    private boolean isLastPage = false;
    private Handler handler = new Handler();
    private int pageSize=10;
    private List<IntegralBean> integralBeans = new ArrayList<>();
    private DataAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integral_info);
        initView();
    }

    private void initView() {
        userBean = mApplication.getUserBean();


        lv_integral = (PullToRefreshListView) findViewById(R.id.lv_integral);
        lv_integral.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(isLastPage){
                    handler.post(myRunnar);
                    Util.Tip(mContext, "无更多数据");
                }else{
                    currentPage++;
                    isMore = true;
                    IntegralTask();
                }
            }
        });
        adapter = new DataAdapter();
        lv_integral.setAdapter(adapter);
        IntegralTask();
    }

    Runnable myRunnar = new Runnable() {
        @Override
        public void run() {
            lv_integral.onRefreshComplete();
        }
    };

    //新建异步任务排行信息
    private void IntegralTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return integral();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                handler.post(myRunnar);
                hideLoadingDialog();
                Util.Log("ltf","jsonData============"+jsonData);
                if(jsonData==null){
                    Util.Tip(mContext, "获取信息失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            if(!isMore){
                                integralBeans.clear();
                            }
//                            int pageNum = jsonObject.optInt("pageNum");
//                            if(pageNum<=currentPage){
//                                isLastPage = true;
//                            }

                            JSONArray jsonArray = jsonObject.getJSONArray("integralList");
                            if(jsonArray!=null && jsonArray.length()>0){
                                if(jsonArray.length()<pageSize){
                                    isLastPage = true;
                                }
                                for(int i=0;i<jsonArray.length();i++){
                                    IntegralBean integralBean = new IntegralBean();
                                    integralBean.parseJSON(jsonArray.getJSONObject(i));
                                    integralBeans.add(integralBean);
                                }
                            }
                            adapter.notifyDataSetChanged();
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


    private String integral() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);


        proInfo = new PropertyInfo();
        proInfo.setName("pageNum");
        proInfo.setValue(currentPage+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("pageSize");
        proInfo.setValue(pageSize+"");
        proInfoList.add(proInfo);

        String jsonData = ConnectWebservice.getInStance().connectEwalk
                    (
                            AndroidConfig.IntegralInfo,
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

    private class DataAdapter extends BaseAdapter {
        public int getCount() {
            if (integralBeans != null) {
                return integralBeans.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            if (integralBeans != null) {
                return integralBeans.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder localViewHolder = null;

            LayoutInflater inflater = LayoutInflater.from(mContext);

            if (convertView == null) {
                localViewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_integral_info, null);
                localViewHolder.tv_integral = (TextView) convertView.findViewById(R.id.tv_integral);
                localViewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                localViewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                convertView.setTag(localViewHolder);
            } else {
                localViewHolder = (ViewHolder) convertView.getTag();
            }


            final IntegralBean bean = integralBeans.get(position);

            localViewHolder.tv_content.setText(bean.getPdsource());
            if(bean.getPdtime()!=null && !bean.getPdtime().equals("null")) {
                localViewHolder.tv_time.setText(bean.getPdtime());
            }
            double integral = BigDecimalUtil.doubleChange(bean.getPdpoints(),1);
            localViewHolder.tv_integral.setText(integral+"积分");

            return convertView;
        }

        class ViewHolder {
            private TextView tv_content;
            private TextView tv_time;
            private TextView tv_integral;
        }
    }
}
