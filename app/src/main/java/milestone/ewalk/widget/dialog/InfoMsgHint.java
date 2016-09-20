package milestone.ewalk.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import milestone.ewalk.R;

public class InfoMsgHint extends Dialog {

	private TextView dialog_info_content_;
	private Button dialog_info_ok,dialog_info_cancel,dialog_info_upload;

	public InfoMsgHint(Context context) {
		super(context);
		init();
	}

	public InfoMsgHint(Context context, boolean cancelable,
                       OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public InfoMsgHint(Context context, int theme) {
		super(context, theme);
		init();
	}

	public void init() {
		setContentView(R.layout.dialog_infomsghint);
		initView();
		initData();
		setListener();
	}

	protected void initView() {
		dialog_info_content_ = (TextView) findViewById(R.id.dialog_info_content_);
        dialog_info_content_.setMovementMethod(new ScrollingMovementMethod());
		dialog_info_ok = (Button) findViewById(R.id.dialog_info_OK);
        dialog_info_cancel = (Button) findViewById(R.id.dialog_info_cancel);
        dialog_info_upload = (Button) findViewById(R.id.dialog_info_upload);
	}

	protected void initData() {

	}

	protected void setListener() {

	}

	public void setContent(String content) {
        dialog_info_content_.setText(content);

    }

    public void hideUpLode() {
        dialog_info_upload.setVisibility(View.GONE);

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

    public void setCancelListener(android.view.View.OnClickListener clickListener) {
        dialog_info_cancel.setOnClickListener(clickListener);
    }

    public void setupLoadListener(android.view.View.OnClickListener clickListener) {
        dialog_info_upload.setOnClickListener(clickListener);
    }

}
