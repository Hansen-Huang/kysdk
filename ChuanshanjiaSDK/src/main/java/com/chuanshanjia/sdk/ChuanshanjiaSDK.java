package com.chuanshanjia.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.chuanshanjia.sdk.alipay.PayResult;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.log.Log;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.model.CSJBaseUserInfo;
import com.chuanshanjia.sdk.model.CSJUser;
import com.chuanshanjia.sdk.util.MD5Util;

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
    static final String BASE_UEL = "http://sdk.keyyuewan.com/api.php/";
    private static final String TAG = CSJLoginType.class.getSimpleName();
    private static final int SDK_PAY_FLAG = 1;
    private static ChuanshanjiaSDK commplatform = null;

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

    public int init(final CSJAppInfo info, CSJCallBackListener.OnInitCompleteListener listener) {

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
                hashMap.put("channel_id", CSJAppInfo.channelId);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
        //每一条请求在添加参数键值对的同事时加入一个sign的键值对，值是根据所有键值加上key的键值对对从a-z升序然后md5
        return 1;
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
                hashMap.put("channel", appInfo.channelId);
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
                        saveAccount(context, username, password);
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
                hashMap.put("channel", appInfo.channelId);

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
                    Log.i("quick_register:", s);
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
                        saveAccount(context, jsonObject.getString("username"), jsonObject.getString("password"));
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("游戏账号信息");
                        builder.setMessage("您的账号:" + jsonObject.getString("username") + "\n您的密码:" + jsonObject.getString("password") + "\n请保存好账号和密码，截屏保存到您的相册");
                        builder.setPositiveButton("好的", null);
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
                hashMap.put("channel", appInfo.channelId);
                hashMap.put("sign", createSign("UTF-8", hashMap));

                return hashMap;
            }
        };
        requestQueue.add(request);
    }

    private void printScreen(Context context) {
        Activity c = (Activity) context;
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
     * 注册登录框
     *
     * @param context
     * @param listener
     */

    public void loginView(final Context context, CSJCallBackListener.OnLoginListener listener) {
        if (listener != null) {
            CSJCallBackListener.mOnLoginListener = listener;
        }
        context.startActivity(new Intent(context, LoginActivity.class));

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
     * 创角
     *
     * @param context
     * @param roleId     玩家的角色id
     * @param roleName   角色名称
     * @param serverId   玩家所在服务器id
     * @param serverName 玩家所在服务器名称
     * @param listener
     */
    public void createRole(final Context context, final String roleId, final String roleName, final String serverId, final String serverName, CSJCallBackListener.OnCallbackListener listener) {

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

    public void handleResultData(int requestCode, int resultCode, Intent data) {
        Log.i("ChuanshanjiaSDK", "requestCode：" + requestCode + "resultCode：" + resultCode);
        if (requestCode == Constants.REQUEST_LOGIN) {
            if (resultCode == Constants.ACTIVITY_CANCEL) {
                Message message = new Message();
                message.what = CSJReturnCode.FAIL;
                if (CSJCallBackListener.mOnLoginProcessListener != null) {
                    CSJCallBackListener.mOnLoginProcessListener.sendMessage(message);
                }
            } else {
                Log.i("ChuanshanjiaSDK", "handleResultData");
            }
        }
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
                    Log.i("select_pay:", s);
                    jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("code") == CSJReturnCode.SUCCESS) {
                        Message message = new Message();
                        message.what = CSJReturnCode.SUCCESS;
                        CSJCallBackListener.mOnPayProcessListener.sendMessage(message);
                    } else {
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
     * @param context
     * @param amount     商品价格 单位:元
     * @param outTradeNO 订单号
     * @param goodId     商品id
     * @param goodName   商品名称
     * @param roleId     玩家的角色id
     * @param roleName   角色名称
     * @param serverId   玩家所在服务器id
     * @param serverName 玩家所在服务器名称
     * @param notifyUrl  支付回调通知的游戏服地址
     * @param extension  透传参数，会直接回传给第三方
     * @param listener
     */
    public void payView(Activity context, String amount, String outTradeNO, String goodId, String goodName, String roleId, String roleName, String serverId, String serverName, String notifyUrl, String extension, final CSJCallBackListener.OnCallbackListener listener) {
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
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", "http://sdk.ichuanshanjia.com");
        String url = BASE_UEL + "order/order_view?" + createUrl(hashMap) + "&sign=" + createSign("UTF-8", hashMap);
        Log.i("url:", url);
        Log.i("url:", "http://sdk.ichuanshanjia.com");
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

    public void saveUser(Context context, CSJUser user) {
        CSJBaseUserInfo.token = user.getToken();
        SharedPreferences userShared = context.getSharedPreferences("yw_user", 0);
        SharedPreferences.Editor editor = userShared.edit();
        editor.putString("account_id", user.getAccountId());
        editor.putString("token", user.getToken());
        editor.commit();
    }

    public void saveAccount(Context context, String username, String password) {
        SharedPreferences userShared = context.getSharedPreferences("yw_user", 0);
        SharedPreferences.Editor editor = userShared.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
    }

    /**
     * 实名认证
     *
     * @param context
     * @param realname 真实姓名
     * @param sfzno    身份证号码
     * @param listener
     */

    public void submitRealname(final Context context, final String realname, final String sfzno, CSJCallBackListener.OnCallbackListener listener) {

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
                Log.i("CSJBaseUserInfo.token=", CSJBaseUserInfo.token);
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
}
