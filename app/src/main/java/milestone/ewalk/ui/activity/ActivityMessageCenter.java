package milestone.ewalk.ui.activity;

import android.content.Intent;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import milestone.ewalk.R;
import milestone.ewalk.bean.MessageBean;
import milestone.ewalk.bean.RunRecordBean;
import milestone.ewalk.bean.UserBean;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.exception.NetRequestException;
import milestone.ewalk.net.ConnectWebservice;
import milestone.ewalk.ui.ActivityBase;
import milestone.ewalk.util.Util;
import milestone.ewalk.widget.CircularImage;

/**
 * 消息中心
 * Created by ltf on 2016/7/14.
 */
public class ActivityMessageCenter extends ActivityBase{
    private PullToRefreshListView lv_message;
    private DataAdapter adapter;
    List<MessageBean> messageBeans = new ArrayList<>();
    private int currentPage=1;
    private UserBean userBean;
    private int pageSize = 10;
    private boolean isMore = false;
    private boolean isLastPage = false;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);

        initView();
        initData();
    }

    private void initView() {
        userBean = mApplication.getUserBean();
        lv_message = (PullToRefreshListView) findViewById(R.id.lv_message);
        adapter = new DataAdapter();
        lv_message.setAdapter(adapter);

        lv_message.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if(isLastPage){
                    handler.post(myRunnar);
                    Util.Tip(mContext,"无更多数据");
                }else{
                    currentPage++;
                    isMore = true;
                    messageTask();
                }
            }
        });
    }

    Runnable myRunnar = new Runnable() {
        @Override
        public void run() {
            lv_message.onRefreshComplete();
        }
    };

    private void initData() {
        messageTask();
    }

    //新建异步任务修改密码
    private void messageTask() {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected void onPreExecute() {
                showLoadingDialog();
            }

            @Override
            protected String doInBackground(Void... params) {
                return message();		//封装参数
            }

            @Override
            protected void onPostExecute(String jsonData) {
                hideLoadingDialog();
                lv_message.onRefreshComplete();
                if(jsonData==null){
                    Util.Tip(mContext, "获取消息失败");
                }else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        Util.Log("ltf","jsonObject=============="+jsonObject);
                        if (jsonObject.optInt("retNum")==0) {
                            JSONArray jsonArray = jsonObject.getJSONArray("messages");
                            if(jsonArray!=null && jsonArray.length()>0){
                                if(jsonArray.length()<pageSize){
                                    isLastPage = true;
                                }
                                if(!isMore){
                                    messageBeans.clear();
                                }

                                for(int i=0;i<jsonArray.length();i++){
                                    MessageBean bean = new MessageBean();
                                    bean.parseJSON(jsonArray.getJSONObject(i));
                                    messageBeans.add(bean);
                                }
                            }else{
                                isLastPage = true;
                            }
                            adapter.notifyDataSetChanged();
                            ActivityMain.hasNewMsg = false;
                            spUtil.setMESSAGE_TIME(new Date().getTime()/1000+"");
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

    private String message() {
        ArrayList<PropertyInfo> proInfoList = new ArrayList<PropertyInfo>();
        PropertyInfo proInfo = new PropertyInfo();
        proInfo.setName("token");
        proInfo.setValue(userBean.getToken());
        proInfoList.add(proInfo);

        proInfo = new PropertyInfo();
        proInfo.setName("page");
        proInfo.setValue(currentPage+"");
        proInfoList.add(proInfo);


        String jsonData = ConnectWebservice.getInStance().connectEwalk
                (
                        AndroidConfig.MessageCenter,
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
            if (messageBeans != null) {
                return messageBeans.size();
            }
            return 0;
        }

        public Object getItem(int position) {
            if (messageBeans != null) {
                return messageBeans.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder localViewHolder = null;

            LayoutInflater inflater = LayoutInflater.from(ActivityMessageCenter.this);

            if (convertView == null) {
                localViewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_message_center, null);
                localViewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                localViewHolder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
                localViewHolder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
                convertView.setTag(localViewHolder);
            } else {
                localViewHolder = (ViewHolder) convertView.getTag();
            }


            final MessageBean bean = messageBeans.get(position);

            localViewHolder.tv_title.setText(bean.getTitle());
            localViewHolder.tv_content.setText(bean.getContent());
            Date date = new Date(bean.getTime()*1000);
            localViewHolder.tv_time.setText(simpleDateFormat.format(date));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(mContext,ActivityMessageDetail.class);
                    intent.putExtra("messageBean",bean);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            });
            return convertView;
        }

        class ViewHolder {
            private TextView tv_title;
            private TextView tv_time;
            private TextView tv_content;
        }
    }
}
