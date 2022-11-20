package com.chuanshanjia.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chuanshanjia.sdk.base.ResourceExchange;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.base.CSJSendMessageType;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.model.ConfirmDialog;
import com.chuanshanjia.sdk.model.CSJUser;
import com.chuanshanjia.sdk.util.MD5Util;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class LoginActivity extends Activity {
    TextView csj_account_login_account;
    TextView csj_account_login_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*设置窗口样式activity宽高start*/
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        WindowManager.LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6);   //高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.7);    //宽度设置为屏幕的0.7
        p.alpha = 0.9f;      //设置本身透明度
        p.dimAmount = 0.5f;      //设置窗口外黑暗度
        getWindow().setAttributes(p);

        /*设置窗口样式activity宽高end*/
        //setContentView(R.layout.yw_login);
        ResourceExchange mRes = ResourceExchange.getInstance(LoginActivity.this);
        LayoutInflater inflater = (LayoutInflater) LoginActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(mRes.getLayoutId("csj_login"),null);
        setContentView(view);

        SharedPreferences userShared = CSJAppInfo.ctx.getSharedPreferences("csj_user", 0);
        if (userShared.getString("username", null) != null) {
            csj_account_login_account = (TextView) findViewById(mRes.getIdId("csj_account_login_account"));
            csj_account_login_account.setText(userShared.getString("username", null));
            csj_account_login_password = (TextView) findViewById(mRes.getIdId("csj_account_login_password"));
            csj_account_login_password.setText(userShared.getString("password", null));
        }

    }
    public void showToast(final String content) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(CSJAppInfo.ctx, content, Toast.LENGTH_LONG).show();
            }
        });
    }
    public void click(View view) {
        int i = view.getId();
        ResourceExchange mRes = ResourceExchange.getInstance(LoginActivity.this);

        if (i == mRes.getIdId("csj_account_login_log") ) {
            csj_account_login_account = (TextView) findViewById(mRes.getIdId("csj_account_login_account"));
            String username = csj_account_login_account.getText().toString();
            if (username.length() == 0){
                showToast("请输入手机号或账号");
                return;
            }
            csj_account_login_password = (TextView) findViewById(mRes.getIdId("csj_account_login_password"));
            String password = csj_account_login_password.getText().toString();
            if (password.length() == 0){
                showToast("请输入密码");
                return;
            }
            ChuanshanjiaSDK.getInstance().login(CSJAppInfo.ctx, username, password, new CSJCallBackListener.OnLoginProcessListener() {
                @Override
                public void onLoginSuccess(CSJUser user) {
                    finish();
                    Message message = new Message();
                    message.what = CSJReturnCode.SUCCESS;
                    message.obj = user;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }
                }

                @Override
                public void onLoginFailed(int code) {
                    Message message = new Message();
                    message.what = code;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }

                }
            });
        } else if (i == mRes.getIdId("csj_account_login_reg")) {
            finish();
            startActivity(new Intent(this,RegisterActivity.class));

        }else if (i == mRes.getIdId("csj_login_forget_password")) {
            finish();
            startActivity(new Intent(this,SetPasswordActivity.class));
        }else if (i == mRes.getIdId("csj_ks_register")) {

            ChuanshanjiaSDK.getInstance().quickRegister(CSJAppInfo.ctx, new CSJCallBackListener.OnLoginProcessListener() {
                @Override
                public void onLoginSuccess(CSJUser user) {
                    finish();
                    Message message = new Message();
                    message.what = CSJReturnCode.SUCCESS;
                    message.obj = user;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }
                }
                @Override
                public void onLoginFailed(int code) {
                    Message message = new Message();
                    message.what = code;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }
                }
            });
        }
    }
}
