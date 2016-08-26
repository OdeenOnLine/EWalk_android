package milestone.ewalk.ui.activity;

import android.os.Bundle;
import android.view.View;

import milestone.ewalk.R;
import milestone.ewalk.ui.ActivityBase;

/**
 * 活动规则
 * Created by ltf on 2016/8/19.
 */
public class ActivityProtocol extends ActivityBase{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotocol);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
                finishA(true);
                break;
        }
    }
}
