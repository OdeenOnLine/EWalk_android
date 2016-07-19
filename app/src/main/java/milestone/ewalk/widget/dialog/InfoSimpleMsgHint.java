package milestone.ewalk.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import milestone.ewalk.R;

public class InfoSimpleMsgHint extends Dialog {

	private TextView dialog_info_content_;
	private Button dialog_info_ok;

	public InfoSimpleMsgHint(Context context) {
		super(context);
		init();
	}

	public InfoSimpleMsgHint(Context context, boolean cancelable,
                             OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public InfoSimpleMsgHint(Context context, int theme) {
		super(context, theme);
		init();
	}

	public void init() {
		setContentView(R.layout.dialog_simple_infomsghint);
		initView();
		initData();
		setListener();
	}

	protected void initView() {
//		dialog_info_content = (MyTextView) findViewById(R.id.dialog_info_content);
		dialog_info_content_ = (TextView) findViewById(R.id.dialog_info_content_);
        dialog_info_content_.setMovementMethod(new ScrollingMovementMethod());
		dialog_info_ok = (Button) findViewById(R.id.dialog_info_OK);
	}

	protected void initData() {

	}

	protected void setListener() {

	}

	public void setContent(String content) {
		dialog_info_content_.setText(content);

	}

	public void setContent(String content_title, String content,
			String OKString, String cancleString) {
//		dialog_info_content.setText(content_title);
		dialog_info_content_.setText(content);
		dialog_info_ok.setText(OKString);

	}
	
	public void setBtnContent(String content){
		dialog_info_ok.setText(content);
	}

	public void setOKListener(android.view.View.OnClickListener clickListener) {
		dialog_info_ok.setOnClickListener(clickListener);
	}

}
