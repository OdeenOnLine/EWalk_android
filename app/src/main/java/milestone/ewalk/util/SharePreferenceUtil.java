package milestone.ewalk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class SharePreferenceUtil {
	private SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor editor;

	public SharePreferenceUtil(Context context, String name) {
		mSharedPreferences = context.getSharedPreferences(name,
				Context.MODE_PRIVATE);
		editor = mSharedPreferences.edit();
	}

	// 用户名
	private String USERNAME = "username";
	// 密码
	private String PASSWORD = "password";

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


}