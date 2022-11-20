package com.chuanshanjia.sdk.util;

import android.content.Context;

/**
 * Package :com.chuanshanjia.sdk.util
 * Description :
 * Author :huangsu
* Created by chuanshanjia on 18/04/03.
 */

public class DensityUtil {

    public static int  dp2px(Context context,int dp){

        float scale=context.getResources().getDisplayMetrics().density;
        return (int) (dp*scale+0.5f);
    }

    public static int px2dp(Context context, int px){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int) (px/scale+0.5f);
    }
}
