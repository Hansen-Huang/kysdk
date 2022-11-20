package com.chuanshanjia.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.chuanshanjia.sdk.alipay.PayResult;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.log.Log;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.model.CSJBaseUserInfo;
import com.chuanshanjia.sdk.model.CSJUser;
import com.chuanshanjia.sdk.util.MD5Util;
import com.chuanshanjia.sdk.util.WechatUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.chuanshanjia.sdk.base.CSJSendMessageType.BIND_MOBILE;
import static com.chuanshanjia.sdk.base.CSJSendMessageType.REGISTER;
import static com.chuanshanjia.sdk.base.CSJSendMessageType.RESET_PASSWORD;



/**
 * Created by chaunshanjian on 18/04/03.
 */
final class CSJLoginType {


    public final static int WX = 0;
    public final static int QQ = 1;
    public final static int WB = 2;

}

public class ChuanshanjiaSDK {
    static final String BASE_UEL = "http://sdk.ichuanshanjia.com/api.php/";
    static final String WB_REDIRECT_URL = "http://sdk.ichuanshanjia.com";
    static final String WB_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private static final String TAG = CSJLoginType.class.getSimpleName();
    private static final int SDK_PAY_FLAG = 1;
    private static ChuanshanjiaSDK commplatform = null;
    public CSJUser user;
    public boolean hasLogin;
    IWXAPI msgApi;
    Tencent mTencent;
    IUiListener qqListener;

    IUiListener qqShareListener;
    IUiListener qqShareQZoneListener;
    private PopupWindow mPopupWindow;
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    Message message = new Message();
                    //Log.i("alipay", (String) msg.obj);
                    if (TextUtils.equals(resultStatus, "9000") || TextUtils.equals(resultStatus, "4001")) {
                        message.what = CSJReturnCode.SUCCESS;
                    } else {
                        message.what = CSJReturnCode.FAIL;
                    }
                    CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
                    break;
                }
                default:
                    break;
            }
        }

        ;
    };

    private ChuanshanjiaSDK() {
    }

    public static ChuanshanjiaSDK getInstance() {
        if (commplatform == null) {
            commplatform = new ChuanshanjiaSDK();
        }
        return commplatform;
    }

    //将键值对按a-z升序。最后加上key再转字符串再md5
    public static String createSign(String characterEncoding, SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("appkey=" + CSJAppInfo.appKey);
        Log.i("createSign:", sb.toString());
        String sign = MD5Util.MD5Encode(sb.toString().toLowerCase(), characterEncoding);
        Log.i("Sign:", sign);
        return sign;
    }
    public static String createUrl(SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        return sb.toString();
    }

    /**
     * 初始化
     *
     * @param info
     * @param listener
     * @return
     */

    public int init( final CSJAppInfo info, CSJCallBackListener.OnInitCompleteListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnInitCompleteListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(info.getCtx());
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "index/initAuth", new Response.Listener<String>() {
            //请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    int code = jsonObject.getInt("code");
                    Message message = new Message();
                    message.what = code;
                    SharedPreferences userShared = info.getCtx().getSharedPreferences("csj_user", 0);
                    if (userShared.getString("token", null) != null) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(userShared.getString("account_id", null));
                        user.setToken(userShared.getString("token", null));
                        message.obj = user;
                        CSJBaseUserInfo.token = user.getToken();
                    }
                    if (CSJCallBackListener.mOnInitCompleteListener != null)
                        CSJCallBackListener.mOnInitCompleteListener
                                .sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数getDeviceId
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnInitCompleteListener != null)
                    CSJCallBackListener.mOnInitCompleteListener
                            .sendMessage(message);
            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(info.getCtx());
                hashMap.put("channel_id",CSJAppInfo.channelId);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
//每一条请求在添加参数键值对的同事时加入一个sign的键值对，值是根据所有键值加上key的键值对对从a-z升序然后md5

        return 1;
    }


    /***
     * 是否安装了QQ
     *
     * @return
     */
    public boolean isQQAppInstalled() {
        return mTencent.isSupportSSOLogin((Activity) CSJAppInfo.ctx);
    }

    /**
     * 用户账号密码注册
     *
     * @param context
     * @param username
     * @param password
     * @param listener
     */

    public void register(final Context context, final String username, final String password, CSJCallBackListener.OnRegProcessListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (listener != null) {
            CSJCallBackListener.mOnRegProcessListener = listener;
        }

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "regLogin/register", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    int code = jsonObject.getInt("code");
                    Message message = new Message();
                    message.what = code;
                    if (CSJCallBackListener.mOnRegProcessListener != null)
                        CSJCallBackListener.mOnRegProcessListener
                                .sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("username", username);
                hashMap.put("password", password);


                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 手机号注册
     *
     * @param context
     * @param mobile
     * @param password
     * @param code
     * @param listener
     */
    public void regMobile(final Context context, final String mobile, final String password, final String code, CSJCallBackListener.OnRegProcessListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (listener != null) {
            CSJCallBackListener.mOnRegProcessListener = listener;
        }

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "regLogin/mobile_reg", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (message.what == CSJReturnCode.SUCCESS) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(jsonObject.getString("account_id"));
                        user.setToken(jsonObject.getString("token"));
                        message.obj = user;
                        saveUser(context, user);
                    }
                    if (CSJCallBackListener.mOnRegProcessListener != null)
                        CSJCallBackListener.mOnRegProcessListener
                                .sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("mobile", mobile);
                hashMap.put("password", password);
                hashMap.put("code", code);
                hashMap.put("channel",appInfo.channelId);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    public void autoLogin(final Context context, final String openid, CSJCallBackListener.OnRegProcessListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (listener != null) {
            CSJCallBackListener.mOnRegProcessListener = listener;
        }

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "regLogin/auto_login", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (message.what == CSJReturnCode.SUCCESS) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(jsonObject.getString("account_id"));
                        user.setToken(jsonObject.getString("token"));
                        message.obj = user;
                        saveUser(context, user);
                    }
                    if (CSJCallBackListener.mOnRegProcessListener != null)
                        CSJCallBackListener.mOnRegProcessListener
                                .sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("openid", openid);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 用户名密码登录
     *
     * @param context
     * @param username
     * @param password
     * @param listener
     */

    public void login(final Context context, final String username, final String password, CSJCallBackListener.OnLoginProcessListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (listener != null) {
            CSJCallBackListener.mOnLoginProcessListener = listener;
        }

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "regLogin/login", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (message.what == CSJReturnCode.SUCCESS) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(jsonObject.getString("account_id"));
                        user.setToken(jsonObject.getString("token"));
                        CSJBaseUserInfo.token = jsonObject.getString("token");
                        message.obj = user;
                        saveUser(context, user);
                        saveAccount(context,username, password);
                    }
                    if (CSJCallBackListener.mOnLoginProcessListener != null) {
                        CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("username", username);
                hashMap.put("password", password);
                hashMap.put("channel",appInfo.channelId);

                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 快速注册
     *
     * @param context
     * @param listener
     */
    public void quickRegister(final Context context, CSJCallBackListener.OnLoginProcessListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        if (listener != null) {
            CSJCallBackListener.mOnLoginProcessListener = listener;
        }

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "regLogin/quick_register", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                try {
                    Log.i("quick_register:",s);
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (message.what == CSJReturnCode.SUCCESS) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(jsonObject.getString("account_id"));
                        CSJBaseUserInfo.token = jsonObject.getString("token");
                        user.setToken(jsonObject.getString("token"));
                        message.obj = user;
                        saveUser(context, user);
                        saveAccount(context,jsonObject.getString("username"),jsonObject.getString("password"));
                        AlertDialog.Builder builder  = new AlertDialog.Builder(context);
                        builder.setTitle("游戏账号信息" ) ;
                        builder.setMessage("您的账号:"+jsonObject.getString("username")+"\n您的密码:"+jsonObject.getString("password")+"\n请保存好账号和密码，截屏保存到您的相册" ) ;
                        builder.setPositiveButton("好的" ,  null );
                        builder.show();
                        printScreen(context);
                    }
                    if (CSJCallBackListener.mOnLoginProcessListener != null) {
                        CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("channel",appInfo.channelId);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    private void printScreen(Context context) {
        Activity c = (Activity)context;
        View view = c.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        if (bmp != null) {
            try {
                String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "1.png";
                File file = new File(path);
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送验证码
     *
     * @param context
     * @param mobile
     * @param type     验证码的类型 YWSendMessageType
     * @param listener
     */
    public void sendMessage(final Context context, final String mobile, final int type, CSJCallBackListener.OnCallbackListener listener) {

        if (listener != null) {
            CSJCallBackListener.mSendMessageListener = listener;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "Index/send_message", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();

                    if (message.what == CSJReturnCode.SUCCESS) {
                        message.what = jsonObject.getInt("code");
                    } else {
                        message.what = CSJReturnCode.FAIL;
                    }
                    if (CSJCallBackListener.mSendMessageListener != null) {
                        CSJCallBackListener.mSendMessageListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("mobile", mobile);
                switch (type) {
                    case BIND_MOBILE: {
                        hashMap.put("type", "bind");
                        break;
                    }
                    case RESET_PASSWORD: {
                        hashMap.put("type", "reset_pw");
                        break;
                    }
                    case REGISTER: {
                        hashMap.put("type", "register");
                        break;
                    }
                }


                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 绑定手机号码
     *
     * @param context
     * @param mobile
     * @param password
     * @param code
     * @param listener
     */
    public void bindMobile(final Context context, final String mobile, final String password, final String code, CSJCallBackListener.OnCallbackListener listener) {
        bindPhone(context, mobile, password, code, listener);
    }

    public void bindPhone(final Context context, final String mobile, final String password, final String code, CSJCallBackListener.OnCallbackListener listener) {

        if (listener != null) {
            CSJCallBackListener.mBindMobileListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "Index/bind_mobile", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (CSJCallBackListener.mBindMobileListener != null) {
                        CSJCallBackListener.mBindMobileListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("mobile", mobile);
                hashMap.put("password", password);
                hashMap.put("code", code);
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 手机号重置密码
     *
     * @param context
     * @param mobile
     * @param password
     * @param code
     * @param listener
     */
    public void resetPassword(final Context context, final String mobile, final String password, final String code, CSJCallBackListener.OnCallbackListener listener) {

        if (listener != null) {
            CSJCallBackListener.mResetPasswordListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "Index/reset_password", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (CSJCallBackListener.mResetPasswordListener != null) {
                        CSJCallBackListener.mResetPasswordListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("mobile", mobile);
                hashMap.put("password", password);
                hashMap.put("code", code);
                //hashMap.put("token", YWBaseUserInfo.token);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 微信登录
     *
     * @param context
     * @param listener
     */
    public void loginWithWeixin(final Context context, CSJCallBackListener.OnLoginProcessListener listener) {
        if (listener != null) {
            CSJCallBackListener.mOnLoginProcessListener = listener;
        }
        if (!msgApi.isWXAppInstalled()) {
            Message message = new Message();
            message.what = CSJReturnCode.WEIXIN_NOT_INSTALLED;
            if (CSJCallBackListener.mOnLoginProcessListener != null) {
                CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
            }
        } else {
            SendAuth.Req req = new SendAuth.Req();

            req.scope = "snsapi_userinfo";
            req.state = "yaowan_sdk";

            msgApi.sendReq(req);
        }


    }

    /**
     * QQ登录
     *
     * @param context
     * @param listener
     */
    public void loginWithQQ(final Context context, CSJCallBackListener.OnLoginProcessListener listener) {
        if (listener != null) {
            CSJCallBackListener.mOnLoginProcessListener = listener;
        }

        qqListener = new IUiListener() {

            @Override
            public void onComplete(Object arg0) {
                try {
                    JSONObject jo = (JSONObject) arg0;
                    int ret = jo.getInt("ret");
                    System.out.println("loginWithQQ" + String.valueOf(jo));
                    loginOpen(context, CSJLoginType.QQ, jo.getString("openid"), jo.getString("access_token"));

                } catch (Exception e) {
                    // TODO: handle exception
                    Message message = new Message();
                    message.what = CSJReturnCode.FAIL;
                    if (CSJCallBackListener.mOnLoginProcessListener != null) {
                        CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                    }
                }
            }

            @Override
            public void onError(UiError e) {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnLoginProcessListener != null) {
                    CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                }
            }

            @Override
            public void onCancel() {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnLoginProcessListener != null) {
                    CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                }
            }
        };
        if (!mTencent.isSessionValid()) {
            mTencent.login((Activity) CSJAppInfo.ctx, "all", qqListener);
        }
    }
    /**
     * 注册登录框
     *
     * @param context
     * @param listener
     */

    public void loginView(final Context context, CSJCallBackListener.OnLoginListener listener) {
        if (listener != null) {
            CSJCallBackListener.mOnLoginListener = listener;
        }
        context.startActivity(new Intent(context,LoginActivity.class));

    }
    /**
     * 退出账号
     *
     * @param context
     * @param listener
     */

    public void logout(final Context context, CSJCallBackListener.OnLogoutListener listener) {
        if (listener != null) {
            CSJCallBackListener.mOnLogoutListener = listener;
        }
        SharedPreferences userShared = context.getSharedPreferences("yw_user", 0);
        SharedPreferences.Editor editor = userShared.edit();
        editor.remove("account_id");
        editor.remove("token");
        editor.commit();
        Message message = new Message();
        message.what = CSJReturnCode.SUCCESS;
        if (CSJCallBackListener.mOnLogoutListener != null) {
            CSJCallBackListener.mOnLogoutListener.sendMessage(message);
        }

    }

    /**
     *创角
     *
     * @param context
     * @param roleId       玩家的角色id
     * @param roleName     角色名称
     * @param serverId     玩家所在服务器id
     * @param serverName   玩家所在服务器名称
     * @param listener
     */
    public void createRole(final Context context, final String roleId, final String roleName, final String serverId , final String  serverName, CSJCallBackListener.OnCallbackListener listener) {

//        if (listener != null) {
//            CSJCallBackListener.mResetPasswordListener = listener;
//        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "Index/create_role", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
//                    if (CSJCallBackListener.mResetPasswordListener != null) {
//                        CSJCallBackListener.mResetPasswordListener.sendMessage(message);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("role_id", roleId);
                hashMap.put("role_name", roleName);
                hashMap.put("server_id", serverId);
                hashMap.put("server_name", serverName);
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }
    /**
     * 易宝支付
     *
     * @param context
     * @param amount
     * @param outTradeNO
     * @param goodId
     * @param goodName
     * @param notifyURL
     * @param listener
     */
    public void payWithYeepay(final Activity context, String amount, String outTradeNO, String goodId, String goodName, String notifyURL, CSJCallBackListener.OnPayProcessListener listener) {


        if (listener != null) {
            CSJCallBackListener.mOnPayProcessListener = listener;
        }
        final CSJWebView webView = new CSJWebView(context);

        //mPopupWindow = new PopupWindow(webView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        SortedMap<String, String> hashMap = new TreeMap<String, String>();
        hashMap.put("appid", CSJAppInfo.appId); // appid
        hashMap.put("token", CSJBaseUserInfo.token);
        hashMap.put("amount", amount);
        hashMap.put("out_trade_no", outTradeNO);
        hashMap.put("good_id", goodId);
        hashMap.put("good_name", goodName);
        hashMap.put("pay_platform", "yeepay");
        hashMap.put("notify_url", notifyURL);

        hashMap.put("sign", createSign("UTF-8", hashMap));
        webView.mWebView.loadUrl(BASE_UEL + "order/create?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap));

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                Log.i("webView", "setOnKeyListener");
                return false;
            }
        });

        final ViewGroup view = (ViewGroup) context.getWindow().getDecorView();
        view.addView(webView);
        webView.closeListener(new CSJCallBackListener.OnCallbackListener() {
            @Override
            public void callback(int code) {
                view.removeView(webView);
            }
        });


        //mPopupWindow.showAtLocation(context.getWindow().getDecorView(),0,0,0);


    }

    public void handleResultData(int requestCode, int resultCode, Intent data) {
        Log.i("ChuanshanjiaSDK","requestCode：" + requestCode+"resultCode："+resultCode);
        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == Constants.ACTIVITY_CANCEL) {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnLoginProcessListener != null) {
                    CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                }
            } else {
                Log.i("ChuanshanjiaSDK", "handleResultData");
                Tencent.handleResultData(data, qqListener);
            }

        } else if (requestCode == Constants.REQUEST_QQ_SHARE) {
            mTencent.onActivityResultData(requestCode, resultCode, data, qqShareListener);

        }

    }

    /***
     * 处理微信登录
     *
     * @param context
     * @param resp
     */

    public void handleWeixinLogin(final Context context, BaseResp resp) {
        /*登录的回调*/
        if (resp instanceof SendAuth.Resp) {
            SendAuth.Resp newResp = (SendAuth.Resp) resp;

            //获取微信传回的code

            if (newResp.errCode == BaseResp.ErrCode.ERR_OK) {
                RequestQueue requestQueue = Volley.newRequestQueue(context);

                // 创建StringRequest，定义字符串请求的请求方式为POST，
                StringRequest request = new StringRequest(Request.Method.POST, "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + CSJAppInfo.wxAppId + "&secret=" + CSJAppInfo.wxAppSecret + "&code=" + newResp.code + "&grant_type=authorization_code", new Response.Listener<String>() {
                    // 请求成功后执行的函数
                    @Override
                    public void onResponse(String s) {
                        // 打印出POST请求返回的字符串
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(s);

                            loginOpen(context, CSJLoginType.WX, jsonObject.getString("openid"), jsonObject.getString("access_token"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    // 请求失败时执行的函数
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
                requestQueue.add(request);
            } else {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnLoginProcessListener != null) {
                    CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                }
            }


        }
        /*分享的回调*/
        else if (resp instanceof SendMessageToWX.Resp) {

            SendMessageToWX.Resp newResp = (SendMessageToWX.Resp) resp;
            Log.i(TAG, "handleWeixinLogin: " + newResp.errCode);
            if (newResp.errCode == BaseResp.ErrCode.ERR_OK) {
                CSJCallBackListener.mOnShareListener.onSuccess();

            } else if (newResp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
                CSJCallBackListener.mOnShareListener.onCancel();

            } else {
                CSJCallBackListener.mOnShareListener.onError();
            }
        }
    }

    private void loginOpen(final Context context, int loginType, final String openid, final String accessToken) {
        String urlStr = null;
        switch (loginType) {
            case CSJLoginType.WX:
                urlStr = "RegLogin/weixin_login";
                break;
            case CSJLoginType.QQ:
                urlStr = "RegLogin/qq_login";
                break;
            case CSJLoginType.WB:
                urlStr = "RegLogin/wb_login";
                break;

        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + urlStr, new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (message.what == CSJReturnCode.SUCCESS) {
                        CSJUser user = new CSJUser();
                        user.setAccountId(jsonObject.getString("account_id"));
                        CSJBaseUserInfo.token = jsonObject.getString("token");
                        user.setToken(jsonObject.getString("token"));
                        message.obj = user;
                        saveUser(context, user);
                    }
                    if (CSJCallBackListener.mOnLoginProcessListener != null) {
                        CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                hashMap.put("openid", openid);
                hashMap.put("access_token", accessToken);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);

    }

    public void checkPay(final Activity context, final String roleid, final int rolelv, CSJCallBackListener.OnPayProcessListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnPayProcessListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "index/select_pay", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                JSONObject jsonObject = null;
                try {
                    Log.i("select_pay:",s);
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("code") == CSJReturnCode.SUCCESS) {
                        Message message = new Message();
                        message.what = CSJReturnCode.SUCCESS;
                        CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
                    }else {
                        Message message = new Message();
                        message.what = CSJReturnCode.FAIL;
                        CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "VolleyError:" + volleyError.toString());
            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("roleid", roleid);
                hashMap.put("rolelv", String.valueOf(rolelv));
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 微信支付
     *
     * @param context
     * @param amount
     * @param outTradeNO
     * @param goodId
     * @param goodName
     * @param notifyURL
     * @param listener
     */

    public void payWithWeixin(final Activity context, final String amount, final String outTradeNO, final String goodId, final String goodName, final String notifyURL, CSJCallBackListener.OnPayProcessListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnPayProcessListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "order/create", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                Log.i("pay:", s);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("code") == CSJReturnCode.SUCCESS) {
                        JSONObject dataObject = new JSONObject(jsonObject.getString("platform_data"));
                        final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
                        msgApi.registerApp(dataObject.getString("appid"));
                        if (!msgApi.isWXAppInstalled()) {
                            Message message = new Message();
                            message.what = CSJReturnCode.WEIXIN_NOT_INSTALLED;
                            CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
                        } else {
                            PayReq req = new PayReq();
                            req.appId = dataObject.getString("appid");
                            req.partnerId = dataObject.getString("mch_id");
                            req.prepayId = dataObject.getString("prepay_id");
                            req.packageValue = "Sign=WXPay";
                            req.nonceStr = dataObject.getString("nonce_str");
                            req.timeStamp = jsonObject.getString("timestamp");

                            req.sign = dataObject.getString("sign");
                            msgApi.sendReq(req);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("amount", amount);
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("out_trade_no", outTradeNO);
                hashMap.put("good_id", goodId);
                hashMap.put("good_name", goodName);
                hashMap.put("pay_platform", "wxpay");
                hashMap.put("notify_url", notifyURL);

                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    /**
     * 支付宝支付
     *
     * @param context
     * @param amount
     * @param outTradeNO
     * @param goodId
     * @param goodName
     * @param notifyURL
     * @param listener
     */
    public void   payWithAlipay(Activity context, String amount, String outTradeNO, String goodId, String goodName, String notifyURL, CSJCallBackListener.OnPayProcessListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnPayProcessListener = listener;
        }

        //alipayTest(context);
        alipayClient(context, amount, outTradeNO, goodId, goodName, notifyURL);

//        try {
//            PackageInfo packageInfo=context.getPackageManager().getPackageInfo("com.eg.android.AlipayGphone", 0);
//            if (packageInfo == null) {
//                alipayWeb(context, amount,outTradeNO, goodId, goodName, notifyURL);
//            } else {
//                alipayClient(context, amount,outTradeNO, goodId, goodName, notifyURL);
//            }
//
//
//
//        } catch (Exception e) {
//            alipayWeb(context, amount,outTradeNO, goodId, goodName, notifyURL);
//        }


        //com.eg.android.alipayGphone


    }

    /**
     * 支付宝网页支付
     *
     * @param context
     * @param amount
     * @param outTradeNO
     * @param goodId
     * @param goodName
     * @param notifyURL
     */

    private void alipayWeb(Activity context, String amount, String outTradeNO, String goodId, String goodName, String notifyURL) {
        final CSJWebView webView = new CSJWebView(context);

        SortedMap<String, String> hashMap = new TreeMap<String, String>();
        hashMap.put("appid", CSJAppInfo.appId); // appid
        hashMap.put("token", CSJBaseUserInfo.token);
        hashMap.put("amount", amount);
        hashMap.put("out_trade_no", outTradeNO);
        hashMap.put("good_id", goodId);
        hashMap.put("good_name", goodName);
        hashMap.put("pay_platform", "alipayweb");
        hashMap.put("notify_url", notifyURL);

        hashMap.put("sign", createSign("UTF-8", hashMap));
        webView.mWebView.loadUrl(BASE_UEL + "order/create?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap));
        final ViewGroup view = (ViewGroup) context.getWindow().getDecorView();
        view.addView(webView);
        webView.closeListener(new CSJCallBackListener.OnCallbackListener() {
            @Override
            public void callback(int code) {
                view.removeView(webView);
            }
        });
    }

    /**
     *
     * @param context
     * @param amount       商品价格 单位:元
     * @param outTradeNO   订单号
     * @param goodId       商品id
     * @param goodName     商品名称
     * @param roleId       玩家的角色id
     * @param roleName     角色名称
     * @param serverId     玩家所在服务器id
     * @param serverName   玩家所在服务器名称
     * @param notifyUrl    支付回调通知的游戏服地址
     * @param extension    透传参数，会直接回传给第三方
     * @param listener
     */
    public void payView(Activity context, String amount, String outTradeNO, String goodId, String goodName, String roleId, String roleName,String serverId ,String  serverName,String notifyUrl,String  extension, final CSJCallBackListener.OnCallbackListener listener) {
        final CSJWebView webView = new CSJWebView(context);

        //mPopupWindow = new PopupWindow(webView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        SortedMap<String, String> hashMap = new TreeMap<String, String>();
        hashMap.put("appid", CSJAppInfo.appId); // appid
        hashMap.put("token", CSJBaseUserInfo.token);
        hashMap.put("amount", amount);
        hashMap.put("out_trade_no", outTradeNO);
        hashMap.put("good_id", goodId);
        hashMap.put("good_name", goodName);
        hashMap.put("role_id", roleId);
        hashMap.put("role_name", roleName);
        hashMap.put("server_id", serverId);
        hashMap.put("server_name", serverName);

        hashMap.put("notify_url", notifyUrl);

        hashMap.put("extension", extension);

        hashMap.put("sign", createSign("UTF-8", hashMap));
        Map<String,String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", "http://sdk.ichuanshanjia.com");
        String url = BASE_UEL + "order/order_view?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap);
        Log.i("url:",url);
        Log.i("url:","http://sdk.ichuanshanjia.com");
        //webView.mWebView.loadUrl(url);
        //webView.mWebView.loadUrl(BASE_UEL + "order/order_view?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap),extraHeaders);
        //webView.mWebView.loadUrl("http://ywsdk.allrace.com/test.html?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap));
        final ViewGroup view = (ViewGroup) context.getWindow().getDecorView();
        //view.addView(webView);
        Intent startIntent = new Intent(context,
                CSJWebViewActivity.class);
        startIntent.putExtra("url", url);
        context.startActivity(startIntent);
        webView.closeListener(new CSJCallBackListener.OnCallbackListener() {
            @Override
            public void callback(int code) {
                //view.removeView(webView);

            }
        });
    }
    private void alipayClient(final Activity context, final String amount, final String outTradeNO, final String goodId, final String goodName, final String notifyURL) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "order/create", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                Log.i("pay:", s);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final JSONObject finalJsonObject = jsonObject;
                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
/*
                        // 构造PayTask 对象
                        PayTask alipay = new PayTask(context);
                        // 调用支付接口，获取支付结果
                        String result = null;
                        try {
                            result = alipay.pay(finalJsonObject.getString("orderdata"), true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Message msg = new Message();
                        msg.what = SDK_PAY_FLAG;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
*/
                    }
                };

                // 必须异步调用
                Thread payThread = new Thread(payRunnable);
                payThread.start();


            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                SortedMap<String, String> hashMap = new TreeMap<String, String>();
                hashMap.put("appid", CSJAppInfo.appId); // appid
                hashMap.put("amount", amount);
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("out_trade_no", outTradeNO);
                hashMap.put("good_id", goodId);
                hashMap.put("good_name", goodName);
                hashMap.put("pay_platform", "alipay");
                hashMap.put("notify_url", notifyURL);

                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    private void alipayTest(final Activity context) {

        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.GET, "http://192.168.16.112/alipay/alipayapi.php", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(final String s) {
                // 打印出POST请求返回的字符串
                //Toast.makeText(context, "POST: " + s, Toast.LENGTH_LONG).show();
                Log.i("pay:", s);

                Runnable payRunnable = new Runnable() {

                    @Override
                    public void run() {
/*
                        // 构造PayTask 对象
                        PayTask alipay = new PayTask(context);
                        // 调用支付接口，获取支付结果
                        String result = alipay.pay(s, true);
                        ;


                        Message msg = new Message();
                        msg.what = SDK_PAY_FLAG;
                        msg.obj = result;
                        mHandler.sendMessage(msg);
*/
                    }
                };

                // 必须异步调用
                Thread payThread = new Thread(payRunnable);
                payThread.start();


            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {


        };
        requestQueue.add(request);
    }

    public void saveUser(Context context, CSJUser user) {
        CSJBaseUserInfo.token = user.getToken();
                SharedPreferences userShared = context.getSharedPreferences("yw_user", 0);
        SharedPreferences.Editor editor = userShared.edit();
        editor.putString("account_id", user.getAccountId());
        editor.putString("token", user.getToken());
        editor.commit();
    }

    public void saveAccount(Context context, String username,String password) {
        SharedPreferences userShared = context.getSharedPreferences("yw_user", 0);
        SharedPreferences.Editor editor = userShared.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
    }

    /**
     * qq分享
     *
     * @param activity
     * @param targetUrl 分享链接
     * @param imageUrl  缩略图链接
     * @param title     分享标题
     * @param content   分享内容
     * @param listener  分享回调
     */
    public void shareToQQ(Activity activity, String targetUrl, String imageUrl, String title,
                          String content, CSJCallBackListener.OnShareListener listener) {


        if (listener != null) {
            CSJCallBackListener.mOnShareListener = listener;
        }

        Bundle bundle = new Bundle();
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        qqShareListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                CSJCallBackListener.mOnShareListener.onSuccess();
            }

            @Override
            public void onError(UiError uiError) {
                CSJCallBackListener.mOnShareListener.onError();
            }

            @Override
            public void onCancel() {
                CSJCallBackListener.mOnShareListener.onCancel();
            }
        };
        mTencent.shareToQQ(activity, bundle, qqShareListener);
    }

    /**
     * qq空间分享
     *
     * @param activity
     * @param targetUrl 分享链接
     * @param imageUrl  缩略图链接
     * @param title     分享标题
     * @param content   分享内容
     * @param listener  分享回调
     */
    public void shareToQQZone(Activity activity, String targetUrl, String imageUrl, String title,
                              String content, CSJCallBackListener.OnShareListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnShareListener = listener;
        }

        Bundle bundle = new Bundle();
        bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
        bundle.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);
        bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);

        qqShareQZoneListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                CSJCallBackListener.mOnShareListener.onSuccess();
            }

            @Override
            public void onError(UiError uiError) {
                CSJCallBackListener.mOnShareListener.onError();
            }

            @Override
            public void onCancel() {
                CSJCallBackListener.mOnShareListener.onCancel();
            }
        };
        mTencent.shareToQQ(activity, bundle, qqShareListener);
    }

    /**
     * 微信分享
     *
     * @param targetUrl 分享链接
     * @param imageUrl  缩略图链接
     * @param title     分享标题
     * @param content   分享内容
     * @param listener  分享回调
     */
    public void shareToWechat(Activity context, final String targetUrl, final String imageUrl, final String title,
                                   final String content, CSJCallBackListener.OnShareListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnShareListener = listener;
        }
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message m) {
                super.handleMessage(m);
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = targetUrl;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = title;
                msg.description = content;
                msg.thumbData = WechatUtil.bmpToByteArray(((Bitmap) m.obj), true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneSession;//分享给朋友圈
                msgApi.sendReq(req);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = WechatUtil.getImageBitmap(imageUrl);
                //Bitmap bitmap = Bitmap.createScaledBitmap(bmp, 150, (int) ((float)bmp.getHeight()/(float)bmp.getWidth()*150), true);
                Message message = Message.obtain(handler, 0, bmp);
                message.sendToTarget();
            }
        }).start();

    }

    public void shareImageToWechat(Activity context, final Bitmap bmp, CSJCallBackListener.OnShareListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnShareListener = listener;
        }
        WXImageObject imgObj = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp,150, (int) ((float)bmp.getHeight()/(float)bmp.getWidth()*150),true);
        bmp.recycle();
        msg.thumbData = WechatUtil.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;//分享给朋友圈
        msgApi.sendReq(req);

    }


    /**
     * 朋友圈分享
     *
     * @param targetUrl 分享链接
     * @param imageUrl  缩略图链接
     * @param title     分享标题
     * @param content   分享内容
     * @param listener  分享回调
     */
    public void shareToWechatTimeline(final String targetUrl, final String imageUrl, final String title,
                                      final String content, CSJCallBackListener.OnShareListener listener) {

        if (listener != null) {
            CSJCallBackListener.mOnShareListener = listener;
        }
        final Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message m) {
                super.handleMessage(m);
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = targetUrl;
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = title;
                msg.description = content;
                msg.thumbData = WechatUtil.bmpToByteArray(((Bitmap) m.obj), true);

                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;//分享给朋友圈
                msgApi.sendReq(req);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = Bitmap.createScaledBitmap(WechatUtil.getImageBitmap(imageUrl), 120, 120, true);
                Message message = Message.obtain(handler, 0, bitmap);
                message.sendToTarget();
            }
        }).start();


    }

    /**
     * 实名认证
     *
     * @param context
     * @param realname 真实姓名
     * @param sfzno    身份证号码
     * @param listener
     */

    public void submitRealname(final Context context, final String realname, final String sfzno,  CSJCallBackListener.OnCallbackListener listener) {

        if (listener != null) {
            CSJCallBackListener.onSubmitRealnameListener = listener;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(context);


        // 创建StringRequest，定义字符串请求的请求方式为POST，
        StringRequest request = new StringRequest(Request.Method.POST, BASE_UEL + "Index/submit_real_name", new Response.Listener<String>() {
            // 请求成功后执行的函数
            @Override
            public void onResponse(String s) {
                // 打印出POST请求返回的字符串
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    Message message = new Message();
                    message.what = jsonObject.getInt("code");
                    if (CSJCallBackListener.onSubmitRealnameListener != null) {
                        CSJCallBackListener.onSubmitRealnameListener.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // 请求失败时执行的函数
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {

            // 定义请求数据
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                CSJAppInfo appInfo = new CSJAppInfo();
                SortedMap<String, String> hashMap = appInfo.getBaseInfo(context);
                Log.i("CSJBaseUserInfo.token=",CSJBaseUserInfo.token);
                hashMap.put("token", CSJBaseUserInfo.token);
                hashMap.put("realname", realname);
                hashMap.put("sfzno", sfzno);
                hashMap.put("sign", createSign("UTF-8", hashMap));
                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    public void realname(Activity context, final CSJCallBackListener.OnCallbackListener listener) {
        final CSJWebView webView = new CSJWebView(context);

        //mPopupWindow = new PopupWindow(webView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        SortedMap<String, String> hashMap = new TreeMap<String, String>();
        hashMap.put("appid", CSJAppInfo.appId); // appid
        hashMap.put("token", CSJBaseUserInfo.token);

        hashMap.put("sign", createSign("UTF-8", hashMap));
        webView.mWebView.loadUrl(BASE_UEL + "index/real_name?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap));
        final ViewGroup view = (ViewGroup) context.getWindow().getDecorView();
        view.addView(webView);
        webView.closeListener(new CSJCallBackListener.OnCallbackListener() {
            @Override
            public void callback(int code) {
                Message message = new Message();
                message.what = code;
                listener.sendMessage(message);
                view.removeView(webView);
            }
        });
    }


    /*生成微信分享唯一标签*/
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


}
