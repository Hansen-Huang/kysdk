package com.chuanshanjia.demo;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.chuanshanjia.sdk.LoginActivity;
import com.chuanshanjia.sdk.CSJCallBackListener;
import com.chuanshanjia.sdk.ChuanshanjiaSDK;
import com.chuanshanjia.sdk.base.CSJReturnCode;
import com.chuanshanjia.sdk.base.CSJSendMessageType;
import com.chuanshanjia.sdk.model.CSJAppInfo;
import com.chuanshanjia.sdk.model.CSJUser;
import com.chuanshanjia.demo.R;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    String imageurl = "http://h5sdk.yaowan.com/public/images/icon_2.png";
    String targeturl = "http://stackoverflow.com/search?q=recyclerview++scrollview+";
    String title = "Search";
    String content = "dvanced Search Tips results found containing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = MainActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_main);

        CSJAppInfo info = new CSJAppInfo();
        info.setAppId("800001");
        info.setAppKey("beff3ceace2e1b7e6229334b06ad0bd1");
        info.setWxAppId("wxa2f4cadc467d7f82");
        info.setWxAppSecret("56b68a1a43e24b826e2010da22a4cabb");
        info.setQqAppId("101412751");
        info.setWbAppId("3818406638");

        info.setChannelId("c0001");
        info.setCtx(this);
        ChuanshanjiaSDK.getInstance().init(info, new CSJCallBackListener.OnInitCompleteListener() {
            @Override
            public void onComplete(int code, boolean hasLogin, CSJUser user) {
                switch (code) {
                    case CSJReturnCode.SUCCESS:
                        // 成功
                        showToast("初始化成功");
                        if (hasLogin) {
                            Log.i("init:", user.getAccountId() + "|" + user.getToken());
                        } else {
                            Log.i("init:", "未登录");
                        }
                        break;
                    case CSJReturnCode.FAIL:
                        // 失败
                        showToast("初始化失败");
                        break;
                    default:
                        break;
                }
            }
        });
    }
    public void showToast(final String content) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
            }
        });
    }
    public String returnMessage(int code) {
        switch (code) {
            case CSJReturnCode.USERNAME_NOT_EXIST:
                return "用户名不存在";
            case CSJReturnCode.FAIL:

            default:
                return "失败";
        }
    }
    public void click(View view) {

        switch (view.getId()) {
            case R.id.tourist: {
                ChuanshanjiaSDK.getInstance().autoLogin(MainActivity.this, "11111111",new CSJCallBackListener.OnRegProcessListener() {
                    @Override
                    public void onRegSuccess(CSJUser user) {
                        showToast("注册成功" + user.getAccountId());
                    }

                    @Override
                    public void onRegFailed(int code) {
                        showToast("注册失败：" + code);
                    }
                });
                break;
            }

            case R.id.send_message: {
                ChuanshanjiaSDK.getInstance().logout(MainActivity.this,new CSJCallBackListener.OnLogoutListener(){
                    @Override
                    public void finishLogoutProcess(int code) {
                        switch (code) {
                            case CSJReturnCode.SUCCESS:

                                showToast("退出成功");

                                break;
                            default:
                                showToast("失败：" + code);
                                break;
                        }
                    }
                });
                break;
            }
            case R.id.bind_phone: {
                ChuanshanjiaSDK.getInstance().regMobile(MainActivity.this, "13450203360", "123456", "8355", new CSJCallBackListener.OnRegProcessListener() {
                    @Override
                    public void onRegSuccess(CSJUser user) {
                        showToast("注册成功" + user.getAccountId());
                    }

                    @Override
                    public void onRegFailed(int code) {
                        showToast("注册失败：" + code);
                    }
                });
                ChuanshanjiaSDK.getInstance().resetPassword(MainActivity.this, "13450203360", "123456", "752114", new CSJCallBackListener.OnCallbackListener() {
                    @Override
                    public void callback(int code) {
                        switch (code) {
                            case CSJReturnCode.SUCCESS:
                                // 密码修改成功
                                //showToast("密码修改成功");

                                break;
                            case CSJReturnCode.FAIL:
                                // 密码修改失败
                                // showToast("密码修改失败");

                                break;
                            default:

                                break;
                        }
                    }
                });
                break;
            }
            case R.id.login: {
                ChuanshanjiaSDK.getInstance().login(MainActivity.this, "18824198109", "qqqqqq", new CSJCallBackListener.OnLoginProcessListener() {
                    @Override
                    public void onLoginSuccess(CSJUser user) {
                        showToast(user.getToken());
                    }

                    @Override
                    public void onLoginFailed(int code) {
                        showToast("登录失败:" + code);
                    }
                });
                break;
            }
            case R.id.login_btn: {
                ChuanshanjiaSDK.getInstance().loginView(MainActivity.this, new CSJCallBackListener.OnLoginListener() {
                    @Override
                    public void onLoginSuccess(CSJUser user) {

                        showToast(user.getToken());
                    }

                    @Override
                    public void onLoginFailed(int code) {

                        showToast("登录失败:" + code);
                    }
                });
                break;
            }

            case R.id.submit_realname: {

                ChuanshanjiaSDK.getInstance().realname(MainActivity.this, new CSJCallBackListener.OnCallbackListener() {

                    @Override
                    public void callback(int code) {
                        if (code == CSJReturnCode.SUCCESS) {
                            showToast("认证成功");
                        } else {
                            showToast("未认证" );
                        }
                    }
                });
                break;
            }
            case R.id.weixin_pay: {
                ChuanshanjiaSDK.getInstance().checkPay(MainActivity.this,"100000",5, new CSJCallBackListener.OnPayProcessListener() {
                    @Override
                    public void finishPayProcess(int code) {

                        switch (code) {
                            case CSJReturnCode.SUCCESS:
                                showToast("切支付");

                                break;
                            case CSJReturnCode.FAIL:
                                showToast("不切");
                                break;
                            default:

                                break;
                        }
                    }
                });
                break;
            }
            case R.id.alipay: {
                ChuanshanjiaSDK.getInstance().payWithAlipay(MainActivity.this, "0.01", "1234565767758", "123", "金币", "http://yaowan.com", new CSJCallBackListener.OnPayProcessListener() {
                    @Override
                    public void finishPayProcess(int code) {

                        switch (code) {
                            case CSJReturnCode.SUCCESS:
                                // 充值成功
                                showToast("充值成功");

                                break;
                            case CSJReturnCode.FAIL:
                                showToast("充值失败");
                                // 充值失败
                                break;
                            default:

                                break;
                        }
                    }
                });
                break;
            }
            case R.id.pay_btn: {
                ChuanshanjiaSDK.getInstance().payView(MainActivity.this, "0.01", "1234565767758", "123", "金币", "1000001","角色名","100000","服务器名", "http://yaowan.com","透传参数", new CSJCallBackListener.OnCallbackListener() {

                    @Override
                    public void callback(int code) {

                    }
                });
                break;
            }
            case R.id.qq_share_btn:

                ChuanshanjiaSDK.getInstance().shareToQQ(MainActivity.this, targeturl, imageurl, title, content,
                        new CSJCallBackListener.OnShareListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "onSuccess: ");
                            }

                            @Override
                            public void onError() {
                                Log.i(TAG, "onError: ");
                            }

                            @Override
                            public void onCancel() {
                                Log.i(TAG, "onCancel: ");
                            }
                        });

                break;
            case R.id.qq_share_to_qzone_btn:
                ChuanshanjiaSDK.getInstance().shareToQQZone(MainActivity.this, targeturl, imageurl, title, content,
                        new CSJCallBackListener.OnShareListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "onSuccess: ");
                            }

                            @Override
                            public void onError() {
                                Log.i(TAG, "onError: ");
                            }

                            @Override
                            public void onCancel() {
                                Log.i(TAG, "onCancel: ");
                            }
                        });


                break;
            case R.id.wechat_share_btn:
                ChuanshanjiaSDK.getInstance().shareImageToWechat(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.logo),new CSJCallBackListener.OnShareListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, "onError: ");
                    }

                    @Override
                    public void onCancel() {
                        Log.i(TAG, "onCancel: ");
                    }
                });

                break;
            case R.id.wechat_timeline_share_btn:

                ChuanshanjiaSDK.getInstance().shareToWechatTimeline(targeturl, imageurl, title, content, new CSJCallBackListener.OnShareListener() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, "onError: ");
                    }

                    @Override
                    public void onCancel() {
                        Log.i(TAG, "onCancel: ");
                    }
                });
                break;

        }


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ChuanshanjiaSDK.getInstance().handleResultData(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
