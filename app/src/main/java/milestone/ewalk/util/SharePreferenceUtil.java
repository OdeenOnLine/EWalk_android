package milestone.ewalk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class SharePreferenceUtil {
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String name) {
		mSharedPreferences = context.getSharedPreferences(name,
				Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}

	// 用户名
	private String USERNAME = "username";
	// 密码
	private String PASSWORD = "password";
    // 最新消息时间
    private String MESSAGE_TIME = "message_time";

    // 新增步数
    private String M_DETECTOR = "mDetector";
    // 当天步数
    private String DAY_DETECTOR = "dayDetector";
    // 步数开始时间
    private String Start_TIME = "startTime";


	public void setUSERNAME(String value) {
		editor.putString(USERNAME, value);
		editor.commit();
	}

	public String getUSERNAME() {
		return mSharedPreferences.getString(USERNAME, "");
	}

	public void setPASSWORD(String pwd) {
		editor.putString(PASSWORD, pwd);
		editor.commit();
	}

	public String getPASSWORD() {
		return mSharedPreferences.getString(PASSWORD, "");
	}

    public void setMESSAGE_TIME(String message_time) {
        editor.putString(MESSAGE_TIME, message_time);
        editor.commit();
    }

    public String getMESSAGE_TIME() {
        return mSharedPreferences.getString(MESSAGE_TIME, "0");
    }


    public long getStart_TIME() {
        return  mSharedPreferences.getLong(Start_TIME,0);
    }

    public void setStart_TIME(long start_TIME) {
        editor.putLong(Start_TIME, start_TIME);
        editor.commit();
    }

    public float getDAY_DETECTOR() {
        return mSharedPreferences.getFloat(DAY_DETECTOR,0);
    }

    public void setDAY_DETECTOR(float day_detector) {
        editor.putFloat(DAY_DETECTOR, day_detector);
        editor.commit();
    }

    public float getM_DETECTOR() {
        return mSharedPreferences.getFloat(M_DETECTOR,0);
    }

    public void setM_DETECTOR(float m_DETECTOR) {
        editor.putFloat(M_DETECTOR, m_DETECTOR);
        editor.commit();
    }
}
