package milestone.ewalk.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import milestone.ewalk.R;
import milestone.ewalk.bean.MessageBean;
import milestone.ewalk.ui.ActivityBase;

/**
 * Created by ltf on 2016/7/14.
 * 消息详情
 */
public class ActivityMessageDetail extends ActivityBase{
    private TextView tv_title,tv_time,tv_content;
    private MessageBean messageBean;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        messageBean = (MessageBean) getIntent().getSerializableExtra("messageBean");
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_content = (TextView) findViewById(R.id.tv_content);

        tv_title.setText(messageBean.getTitle());
        tv_content.setText(messageBean.getContent());
        Date date = new Date(messageBean.getTime()*1000);
        tv_time.setText(simpleDateFormat.format(date));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
        }
    }
}
