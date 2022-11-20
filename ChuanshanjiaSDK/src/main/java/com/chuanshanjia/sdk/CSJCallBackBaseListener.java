package com.chuanshanjia.sdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.chuanshanjia.sdk.base.Constants;
import com.chuanshanjia.sdk.log.Log;


public abstract class CSJCallBackBaseListener {

	private static final String TAG = Constants.G_TAG + "CSJCallBackBaseListener";
	private Handler mHandler;

	public CSJCallBackBaseListener() {
		if(Looper.myLooper() != null){
			mHandler = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					CSJCallBackBaseListener.this.handleMessage(msg);
				}
			};
		}
	}

	public abstract void handleMessage(Message msg);
	
	public void sendMessage(Message msg){
        if(mHandler != null){
        	mHandler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
	}
	
	public void sendEmptyMessage(int what){
        Message msg = Message.obtain();
        msg.what = what;
        Log.i(TAG,"sendEmptyMessage");
		if(mHandler != null){
        	mHandler.sendEmptyMessage(what);
        } else {
            handleMessage(msg);
        }
	}
}
