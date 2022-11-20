package com.chuanshanjia.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.util.DensityUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chaunshanjia on 18/04/03.
 */
public class CSJWebView extends RelativeLayout implements OnClickListener {
    public WebView mWebView;
    public Button mButton;
    Activity mContext;

    public CSJWebView(Activity context) {
        super(context);
        mContext = context;
        initChildView(context);
    }

    protected void initChildView(Context context) {
//        this.mButton = new Button(context);
//        this.mButton.setMaxWidth(5);
//        this.mButton.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
//                (DensityUtil.dp2px(context, 40), DensityUtil.dp2px(context, 40));
//        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
//        this.mButton.setLayoutParams(layoutParams);
//        mButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                YWWebView.this.removeAllViews();
//            }
//        });
//        this.addView(mButton);
        this.mWebView = new WebView(context);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        //this.mWebView.setBackgroundColor(0);
        this.mWebView.requestFocus();
        this.mWebView.setVerticalScrollBarEnabled(true);

        //this.mWebView.loadUrl("http://kkyun.com");

        this.addView(this.mWebView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        this.mWebView.loadUrl("http://sdk.ichuanshanjia.com");
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
        Toast.makeText(mContext, "返回...", Toast.LENGTH_LONG).show();
        //return false;
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return false;
        }else{
            if (keyCode == KeyEvent.KEYCODE_BACK){
                Toast.makeText(mContext, "返回按钮", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {

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
            //YWCallBackListener.mOnPayProcessListener.sendMessage(message);
//            ViewGroup view = (ViewGroup) mContext.getWindow().getDecorView();
//            view.removeView(YWWebView.this);
            if (CSJCallBackListener.mCloseWebViewListener != null) {
                CSJCallBackListener.mCloseWebViewListener.sendMessage(message);
            }

        }
    }
}
