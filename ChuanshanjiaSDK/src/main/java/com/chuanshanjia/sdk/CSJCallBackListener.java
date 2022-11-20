package com.chuanshanjia.sdk;

import android.os.Message;

import com.chuanshanjia.sdk.model.CSJUser;

public class CSJCallBackListener {
    public static OnLoginListener mOnLoginListener;     //登录回调
    public static OnLoginProcessListener mOnLoginProcessListener;     //登录回调
    public static OnPayProcessListener mOnPayProcessListener;         //支付结果通知

    public static OnRegProcessListener mOnRegProcessListener;                 //注册

    public static OnCallbackListener mResetPasswordListener;         //修改密码

    public static OnCallbackListener mSendMessageListener;          //发送验证码

    public static OnCallbackListener mBindMobileListener;           //绑定手机号

    public static OnCallbackListener mCloseWebViewListener;

    public static OnCallbackListener mVerifyTokenListener;          //验证token

    public static OnExitPlatformListener mExitPlatformListener;       //退出平台的 回调函数

    public static OnCallbackListener mRegisterListener;               //注册

    public static OnSessionInvalidListener mSessionInvalidListener;   //会话过期

    public static OnSwitchAccountListener mOnSwitchAccountListener;   //切换账户

    public static OnInitCompleteListener mOnInitCompleteListener;     //初始化

    public static OnLogoutSessionListener mOnlogoutSessionListener;  //注销session
    public static OnCallbackListener onSubmitRealnameListener;       //提交实名认证

    public static OnEditUserProcessListener mOnEditUserProcessListener;//修改用户后的回调

    public static OnLogoutListener mOnLogoutListener;                //退出账号   public static Handler mHandler;

    /*分享回调*/
    public static OnShareListener mOnShareListener;

    /*分享接口*/
    public interface OnShareListener {

        void onSuccess();

        void onError();

        void onCancel();

    }

    //退出平台 通知回调
    public static abstract interface OnExitPlatformListener {
        public abstract void onExitPlatform();
    }


    //退出账号的回调

    public static abstract class OnLogoutListener extends CSJCallBackBaseListener {

        @Override
        public void handleMessage(Message msg) {
            finishLogoutProcess(msg.what);
        }

        public abstract void finishLogoutProcess(int code);
    }
    //登录之后的回调
    public static abstract class OnLoginListener extends CSJCallBackBaseListener {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                onLoginSuccess((CSJUser) msg.obj);
            } else {
                onLoginFailed(msg.what);
            }
        }

        public abstract void onLoginSuccess(CSJUser user);

        public abstract void onLoginFailed(int code);
    }
    //登录之后的回调
    public static abstract class OnLoginProcessListener extends CSJCallBackBaseListener {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                onLoginSuccess((CSJUser) msg.obj);
            } else {
                onLoginFailed(msg.what);
            }
        }

        public abstract void onLoginSuccess(CSJUser user);

        public abstract void onLoginFailed(int code);
    }

    //购买支付流程回调
    public static abstract class OnPayProcessListener extends CSJCallBackBaseListener {
        @Override
        public void handleMessage(Message msg) {
            finishPayProcess(msg.what);
        }

        public abstract void finishPayProcess(int code);
    }

    //
    public static abstract class OnCallbackListener extends CSJCallBackBaseListener {
        @Override
        public void handleMessage(Message msg) {
            callback(msg.what);
        }

        public abstract void callback(int code);
    }

    //捕捉会话过期通知
    public static abstract class OnSessionInvalidListener extends CSJCallBackBaseListener {
        @Override
        public void handleMessage(Message msg) {
            onSessionInvalid();
        }

        public abstract void onSessionInvalid();
    }

    //切换账号回调
    public static abstract class OnSwitchAccountListener extends CSJCallBackBaseListener {
        @Override

        public void handleMessage(Message msg) {
            onSwitchAccount(msg.what);
        }

        public abstract void onSwitchAccount(int code);
    }

    public static abstract class OnInitCompleteListener extends CSJCallBackBaseListener {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj == null) {
                onComplete(msg.what, false, (CSJUser) msg.obj);
            } else {
                onComplete(msg.what, true, (CSJUser) msg.obj);
            }

        }

        public abstract void onComplete(int code, boolean hasLogin, CSJUser user);
    }

    //注册之后的回调
    public static abstract class OnRegProcessListener extends CSJCallBackBaseListener {


        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0) {
                onRegSuccess((CSJUser) msg.obj);
            } else {
                onRegFailed(msg.what);
            }
        }

        public abstract void onRegSuccess(CSJUser user);

        public abstract void onRegFailed(int code);
    }

    //快速注册之后的回调
    public static abstract class OnQuickRegProcessListener extends CSJCallBackBaseListener {
        public static final int REG_SUCCESS = 0;//成功

        @Override
        public void handleMessage(Message msg) {
            finishRegProcess(msg.what);
        }

        public abstract void finishRegProcess(int code);
    }

    /**
     * 修改用户名和密码之后的回调
     *
     * @author Administrator
     */
    public static abstract class OnEditUserProcessListener extends CSJCallBackBaseListener {
        public static final int EDIT_SUCCESS = 0;//成功

        @Override
        public void handleMessage(Message msg) {
            finishEditProcess(msg.what);
        }

        public abstract void finishEditProcess(int code);
    }

    /**
     * 绑定帐号之后的回调
     *
     * @author Administrator
     */
    public static abstract class OnBindAccountProcessListener extends CSJCallBackBaseListener {
        public static final int EDIT_SUCCESS = 0;//成功

        @Override
        public void handleMessage(Message msg) {
            finishEditProcess(msg.what);
        }

        public abstract void finishEditProcess(int code);
    }

    //注销session之后的回调
    public static abstract class OnLogoutSessionListener {
        public static final int LOGOUT_SUCCESS = 0;//成功
        public static final int LOGOUT_ERROR = 1;//失败

        public abstract void finishLogoutProcess(int code);
    }


}
