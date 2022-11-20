package com.chuanshanjia.sdk.model;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chuanshanjia.sdk.R;

/**
 * Created by chuanshanjia on 18/04/03.
 */

public class LoadingCustom extends Dialog {
    private static LoadingCustom mLoadingProgress;

    public LoadingCustom(Context context) {
        super(context);

    }

    public LoadingCustom(Context context, int theme) {
        super(context, theme);
    }
    public static void showprogress(Context context,CharSequence message,boolean iscanCancel){
        mLoadingProgress=new LoadingCustom(context, R.layout.loading_layout);
        mLoadingProgress.setCanceledOnTouchOutside(false);

        mLoadingProgress.setTitle("");
        mLoadingProgress.setContentView(R.layout.loading_layout);
        if(message==null|| TextUtils.isEmpty(message)){
            mLoadingProgress.findViewById(R.id.loading_tv).setVisibility(View.GONE);
        }else {
            TextView tv = (TextView) mLoadingProgress.findViewById(R.id.loading_tv);
            tv.setText(message);
        }
        //按返回键响应是否取消等待框的显示
        mLoadingProgress.setCancelable(iscanCancel);

        mLoadingProgress.show();

    }
    public static void dismissprogress(){
        if(mLoadingProgress!=null){
            mLoadingProgress.dismiss();
        }
    }
}
