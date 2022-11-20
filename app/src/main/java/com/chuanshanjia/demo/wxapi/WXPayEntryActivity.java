package com.chuanshanjia.demo.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.chuanshanjia.sdk.CSJCallBackListener;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.model.CSJAppInfo;

/**
 * Created by yaowan on 16/11/11.
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //注册API
        api = WXAPIFactory.createWXAPI(this, CSJAppInfo.wxAppId);
        api.handleIntent(getIntent(), this);
    }


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp instanceof PayResp){
            PayResp newResp = (PayResp)resp;
            Message message = new Message();
            Log.i("WXPayEntryActivity:",newResp.errCode+""+newResp.toString());
            if (newResp.errCode == BaseResp.ErrCode.ERR_OK){
                message.what = CSJReturnCode.SUCCESS;
            }else{
                message.what = CSJReturnCode.FAIL;
            }
            CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
            finish();
        }


    }
}
