package com.chuanshanjia.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;

import com.chuanshanjia.sdk.base.ResourceExchange;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.base.CSJSendMessageType;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.model.MyCountTimer;
import com.chuanshanjia.sdk.model.CSJUser;
import com.chuanshanjia.sdk.model.LoadingCustom;

public class RegisterActivity extends Activity {
    Button csj_btn;
    TextView phone;
    TextView password;
    TextView regcode;
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
        //setContentView(R.layout.yw_account_phone_register);
        ResourceExchange mRes = ResourceExchange.getInstance(RegisterActivity.this);
        LayoutInflater inflater = (LayoutInflater) RegisterActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(mRes.getLayoutId("csj_account_phone_register"),null);
        setContentView(view);
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
        ResourceExchange mRes = ResourceExchange.getInstance(RegisterActivity.this);
        if (i == mRes.getIdId("csj_register_button")) {
            //注册
            phone = (TextView)findViewById(mRes.getIdId("csj_register_phone_number"));
            String phoneStr = phone.getText().toString();
            if (phoneStr.length() == 0){
                showToast("请输入手机号");
                return;
            }
            password = (TextView)findViewById(mRes.getIdId("csj_account_register_password"));
            String passwordStr = password.getText().toString();
            if (passwordStr.length() == 0){
                showToast("请输入密码");
                return;
            }
            regcode = (TextView)findViewById(mRes.getIdId("csj_register_authcode"));
            String codeStr = regcode.getText().toString();
            if (codeStr.length() == 0){
                showToast("请输入验证码");
                return;
            }

            ChuanshanjiaSDK.getInstance().regMobile(CSJAppInfo.ctx, phoneStr, passwordStr, codeStr, new CSJCallBackListener.OnRegProcessListener() {
                @Override
                public void onRegSuccess(CSJUser user) {
                    finish();
                    Message message = new Message();
                    message.what = CSJReturnCode.SUCCESS;
                    message.obj = user;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }
                }
                @Override
                public void onRegFailed(int code) {
                    Message message = new Message();
                    message.what = code;
                    if (CSJCallBackListener.mOnLoginListener != null) {
                        CSJCallBackListener.mOnLoginListener.sendMessage(message);
                    }
                    //showToast("注册失败：" + code);
                }
            });
        } else if (i == mRes.getIdId("csj_back_login_button")) {
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        } else if (i == mRes.getIdId("csj_register_authcode_btn")) {
            csj_btn = (Button)findViewById(mRes.getIdId("csj_register_authcode_btn"));
            phone = (TextView)findViewById(mRes.getIdId("csj_register_phone_number"));
            String phoneStr = phone.getText().toString();
            if (phoneStr.length() == 0) {
                showToast("请输入手机号");
                return;
            }

            //LoadingCustom loadingCustom = new LoadingCustom(CSJAppInfo.ctx,mRes.getLayoutId("loading_dialog"));
            //loadingCustom.showprogress(CSJAppInfo.ctx,"正在加载",true);

            ChuanshanjiaSDK.getInstance().sendMessage(CSJAppInfo.ctx, phoneStr, CSJSendMessageType.REGISTER, new CSJCallBackListener.OnCallbackListener() {
                @Override
                public void callback(int code) {
                    MyCountTimer timer = new MyCountTimer(60,1000,csj_btn);
                    switch (code) {
                        case CSJReturnCode.SUCCESS:
                            timer.start();
                            showToast("发送成功");
                            break;
                        case CSJReturnCode.MOBILE_EXIST:
                            showToast("用户名已经存在");
                            break;
                        default:
                            showToast("发送失败：" + code);
                            break;
                    }

                }
            });
            //发送验证码
        }
    }
}
