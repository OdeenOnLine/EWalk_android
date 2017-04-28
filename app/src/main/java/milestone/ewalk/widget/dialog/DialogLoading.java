package milestone.ewalk.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import milestone.ewalk.R;


public class DialogLoading extends Dialog {
    private TextView tv;
    private String content="";

    public DialogLoading(Context context) {
        super(context, R.style.hintDialog);
    }

    private DialogLoading(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);

        tv = (TextView) this.findViewById(R.id.tv);
        tv.setText("加载中...");
        if(!content.equals("")){
            tv.setText(content);
        }

    }

    public void setContent(String content) {
        this.content = content;
    }

}
