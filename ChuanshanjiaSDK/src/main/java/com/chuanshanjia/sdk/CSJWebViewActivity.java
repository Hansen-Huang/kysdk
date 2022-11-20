package com.chuanshanjia.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.model.CSJAppInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chuanshanjia on 18/04/03.
 */
public class CSJWebViewActivity extends Activity implements OnClickListener {
    public WebView mWebView;
    public Button mButton;
    Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initChildView(this);
    }

    protected void initChildView(Context context) {
        this.mWebView = new WebView(context);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.requestFocus();
        this.mWebView.setVerticalScrollBarEnabled(true);

        setContentView(this.mWebView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        Intent intent=getIntent();
        this.mWebView.loadUrl(intent.getStringExtra("url"));


        this.mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                com.chuanshanjia.sdk.log.Log.i("payurl:", url);
                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    CSJAppInfo.ctx.startActivity(intent);
                    com.chuanshanjia.sdk.log.Log.i("payurl:", url);
                }else{
                    Map<String,String> extraHeaders = new HashMap<String, String>();
                    extraHeaders.put("Referer", "http://sdk.ichuanshanjia.com");
                    view.loadUrl(url,extraHeaders);
                }
                return true;
            }
        });
        mWebView.addJavascriptInterface(new PayJavaScriptInterface(), "payInterface");
    }

    public void closeListener(CSJCallBackListener.OnCallbackListener listener) {
        if (listener != null) {
            CSJCallBackListener.mCloseWebViewListener = listener;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
//            mWebView.goBack();// 返回前一个页面
//            return false;
//        }else{
            if (keyCode == KeyEvent.KEYCODE_BACK){
                finish();
                return false;
            }
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult","-------------");
        ChuanshanjiaSDK.getInstance().handleResultData(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 支付返回结果页 调用java方法  通过js
     *
     * @author Administrator
     */
    class PayJavaScriptInterface {
        //传递 支付结果数据
        @JavascriptInterface
        public void handlerInfo(String info) {
            Log.i("PayJavaScriptInterface:", " handlerInfo:" + info);
            Message message = new Message();

            if (info.equals("success")) {
                message.what = CSJReturnCode.SUCCESS;
            } else {
                message.what = CSJReturnCode.FAIL;
            }
            if (info.equals("close_true")){
                message.what = CSJReturnCode.SUCCESS;
            }else if(info.equals("close_false")){
                message.what = CSJReturnCode.FAIL;
            }
            finish();

            if (CSJCallBackListener.mCloseWebViewListener != null) {
                CSJCallBackListener.mCloseWebViewListener.sendMessage(message);
            }

        }
    }
}
