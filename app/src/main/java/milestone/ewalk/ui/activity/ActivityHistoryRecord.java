package milestone.ewalk.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import milestone.ewalk.R;
import milestone.ewalk.bean.RunRecordBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.CircularImage;

/**
 * 历史记录
 * Created by ltf on 2016/6/15.
 */
public class ActivityHistoryRecord extends ActivityBase{
    private PullToRefreshListView lv_record;
    private DataAdapter adapter;
    List<RunRecordBean> runRecordBeans = new ArrayList<>();
    private int currentPage=1;
    private UserBean userBean;
    private int pageSize = 10;
    private boolean isMore = false;
    private boolean isLastPage = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_record);

        initView();
        initData();
    }

    private void initView() {
        userBean = mApplication.getUserBean();
        lv_record = (PullToRefreshListView) findViewById(R.id.lv_record);
        adapter = new DataAdapter();
        lv_record.setAdapter(adapter);

        lv_record.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(isLastPage){
                    handler.post(myRunnar);
                    Util.Tip(mContext,"无更多数据");
                }else{
                    currentPage++;
                    isMore = true;
                    recordTask();
                }
            }
        });
    }

    Runnable myRunnar = new Runnable() {
        @Override
        public void run() {
            lv_record.onRefreshComplete();
        }
    };

    private void initData() {
        recordTask();
    }

    //新建异步任务修改密码
    private void recordTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return record();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                lv_record.onRefreshComplete();
                if(jsonData==null){
                    Util.Tip(mContext, "获取记录失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        if (jsonObject.optInt("retNum")==0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("record");
                            if(jsonArray!=null && jsonArray.length()>0){
                                if(jsonArray.length()<pageSize){
                                    isLastPage = true;
                                }
                                if(!isMore){
                                    runRecordBeans.clear();
                                }

                                for(int i=0;i<jsonArray.length();i++){
                                    RunRecordBean bean = new RunRecordBean();
                                    bean.parseJSON(jsonArray.getJSONObject(i));
                                    runRecordBeans.add(bean);
                                }
                            }else{
                                isLastPage = true;
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

    private String record() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("page");
        proInfo.setValue(currentPage+"");
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("pageSize");
        proInfo.setValue(pageSize+"");
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.Record,
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
            if (runRecordBeans != null) {
                return runRecordBeans.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            if (runRecordBeans != null) {
                return runRecordBeans.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder localViewHolder = null;

            LayoutInflater inflater = LayoutInflater.from(ActivityHistoryRecord.this);

            if (convertView == null) {
                localViewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_history_record, null);
                localViewHolder.iv_point = (CircularImage) convertView.findViewById(R.id.iv_point);
                localViewHolder.tv_start_time = (TextView) convertView.findViewById(R.id.tv_start_time);
                localViewHolder.tv_last = (TextView) convertView.findViewById(R.id.tv_last);
                localViewHolder.tv_mile = (TextView) convertView.findViewById(R.id.tv_mile);
                localViewHolder.tv_kcal = (TextView) convertView.findViewById(R.id.tv_kcal);
                localViewHolder.tv_speed = (TextView) convertView.findViewById(R.id.tv_speed);
                convertView.setTag(localViewHolder);
            } else {
                localViewHolder = (ViewHolder) convertView.getTag();
            }

            localViewHolder.iv_point.setImageResource(R.color.my_blue);

            final RunRecordBean bean = runRecordBeans.get(position);
            Date date = new Date(bean.getStart_time()*1000);
            localViewHolder.tv_start_time.setText(simpleDateFormat.format(date));
            long time = bean.getLast();
            int h= (int) (time/3600);
            int m= (int) ((time-h*3600)/60);
            int s= (int) ((time-h*3600)%60);
            localViewHolder.tv_last.setText(h + ":"+m + ":"+s);
            localViewHolder.tv_mile.setText(bean.getMile()+"公里");
            localViewHolder.tv_kcal.setText(bean.getCalory()+"千卡");
            if(bean.getMile()==0){
                localViewHolder.tv_speed.setText("0'0\"");
            }else {
                int second = (int) (time / bean.getMile());
                localViewHolder.tv_speed.setText(second / 60 + "'" + second % 60 + "\"");
            }


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(mContext,ActivityRunMapRecord.class);
                    intent.putExtra("runRecordBean",bean);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            });
            return convertView;
        }

        class ViewHolder {
            private CircularImage iv_point;
            private TextView tv_start_time;
            private TextView tv_last;
            private TextView tv_mile;
            private TextView tv_kcal;
            private TextView tv_speed;
        }
    }
}
