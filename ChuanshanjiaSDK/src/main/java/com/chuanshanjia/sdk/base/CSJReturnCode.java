package com.chuanshanjia.sdk.base;

public final class CSJReturnCode {
	
	public final static int SUCCESS = 0;                //成功
	public final static int APPID_NOT_EXIST = 10001;    //应用Id不存在
	public final static int SIGN_ERROR = 10002;        //签名错误
	public final static int TOKEN_ERROR = 10003;       //令牌错误
	public final static int USERNAME_NOT_EXIST = 10004;//用户名不存在
	public final static int PASSWORD_ERROR = 10005;    //密码错误
	public final static int CODE_ERROR = 10006;        //手机验证码错误
	public final static int MOBILE_EXIST = 10007;      //手机号码已经被注册
	public final static int USERNAME_EXIST = 10008;    //用户名已经被注册
	public final static int REALNAME_ERROR = 10012;    //真实姓名有误
	public final static int SFZ_ERROR      = 10013;    //身份证号码有误
	public final static int WEIXIN_NOT_INSTALLED = 10100;//微信未安装
	public final static int FAIL = -1;                 //失败

}
