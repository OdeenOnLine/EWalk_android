package milestone.ewalk.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import milestone.ewalk.R;
import milestone.ewalk.config.AndroidConfig;
import milestone.ewalk.ui.ActivityBase;

/**
 * Created by ltf on 2016/7/19.
 */
public class ActivityWelcome extends ActivityBase{
    private WebView web_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        web_view = (WebView) findViewById(R.id.web_view);
        web_view.setWebViewClient(new webViewClient());
        web_view.loadUrl(AndroidConfig.ip_address+"/kyweb_oracle/splash.action");
        WebSettings webSettings1=web_view.getSettings();
        webSettings1.setJavaScriptEnabled(true);
        webSettings1.setSupportZoom(true);
        webSettings1.setUseWideViewPort(true);
        webSettings1.setLoadWithOverviewMode(true);
        webSettings1.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings1.setBlockNetworkImage(false);
        web_view.setWebChromeClient(new WebChromeClient());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    startA(LoginActivity.class, true, true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class webViewClient extends WebViewClient {

        //重写shouldOverrideUrlLoading方法，使点击链接后不使用其他的浏览器打开。

        @Override

        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            view.loadUrl(url);


            //如果不需要其他对点击链接事件的处理返回true，否则返回false

            return true;

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showLoadingDialog();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideLoadingDialog();
        }



    }

}
