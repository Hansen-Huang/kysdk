package com.chuanshanjia.demo.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.chuanshanjia.sdk.ChuanshanjiaSDK;
import com.chuanshanjia.sdk.model.CSJAppInfo;

/**
 * Created by yaowan on 16/11/11.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册API
        api = WXAPIFactory.createWXAPI(this, CSJAppInfo.wxAppId);
        api.handleIntent(getIntent(), this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent,this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp resp) {
      /*处理微信登录和分享的回调*/
        ChuanshanjiaSDK.getInstance().handleWeixinLogin(this,resp);
        finish();

    }
}
