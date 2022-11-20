package com.chuanshanjia.sdk;

import android.content.Context;
import android.content.Intent;
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

import com.chuanshanjia.sdk.base.ResourceExchange;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.base.CSJSendMessageType;
import com.chuanshanjia.sdk.model.MyCountTimer;
import com.chuanshanjia.sdk.model.CSJAppInfo;

public class SetPasswordActivity extends Activity {
    TextView phone;
    Button csj_btn;
    TextView mobilecode;
    TextView password;
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
        //setContentView(R.layout.yw_account_phone_reset_password);
        ResourceExchange mRes = ResourceExchange.getInstance(SetPasswordActivity.this);
        LayoutInflater inflater = (LayoutInflater) SetPasswordActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(mRes.getLayoutId("csj_account_phone_reset_password"),null);
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
        ResourceExchange mRes = ResourceExchange.getInstance(this);
        if (i == mRes.getIdId("csj_register_button")) {
            //重置密码
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
            mobilecode = (TextView)findViewById(mRes.getIdId("csj_register_authcode"));
            String codeStr = mobilecode.getText().toString();
            if (codeStr.length() == 0){
                showToast("请输入验证码");
                return;
            }
            ChuanshanjiaSDK.getInstance().resetPassword(CSJAppInfo.ctx, phoneStr, passwordStr, codeStr, new CSJCallBackListener.OnCallbackListener() {
                @Override
                public void callback(int code) {
                    switch (code) {
                        case CSJReturnCode.SUCCESS:
                            showToast("密码修改成功");
                            finish();
                            startActivity(new Intent(CSJAppInfo.ctx,LoginActivity.class));
                            break;
                        case CSJReturnCode.FAIL:
                            showToast("密码修改失败");
                            break;
                        default:
                            break;
                    }
                }
            });
        } else if (i == mRes.getIdId("csj_back_login_button")) {
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }else if (i == mRes.getIdId("csj_register_authcode_btn")) {
            csj_btn = (Button)findViewById(mRes.getIdId("csj_register_authcode_btn"));
            phone = (TextView)findViewById(mRes.getIdId("csj_register_phone_number"));
            String phoneStr = phone.getText().toString();
            if (phoneStr.length() == 0){
                showToast("请输入手机号");
                return;
            }
            ChuanshanjiaSDK.getInstance().sendMessage(CSJAppInfo.ctx, phoneStr, CSJSendMessageType.RESET_PASSWORD, new CSJCallBackListener.OnCallbackListener() {
                @Override
                public void callback(int code) {
                    MyCountTimer timer = new MyCountTimer(60,1000,csj_btn);
                    switch (code) {
                        case CSJReturnCode.SUCCESS:
                            timer.start();
                            showToast("发送成功");
                            break;
                        case CSJReturnCode.USERNAME_NOT_EXIST:
                            showToast("该手机还没注册!");
                            break;
                        default:
                            showToast("发送失败：" + code);
                            break;
                    }

                }
            });
        }
    }
}
