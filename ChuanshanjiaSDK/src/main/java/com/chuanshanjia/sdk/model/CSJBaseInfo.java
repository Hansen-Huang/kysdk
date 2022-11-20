package com.chuanshanjia.sdk.model;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.WindowManager;

import java.util.ArrayList;

//br.class
public class CSJBaseInfo {
	public static final String gVersion = "chuanshanjia_V1.0"; // 此SDK版本号 第一次发布

	// 默认竖屏
	public static int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; // 屏幕方向
																					// 0-竖
																					// 1-横
	// 切换账号时 是否游戏重启
	public static boolean gIsRestartWhenSwitchAccount = false;

	// 退出平台时 支付是否完成回调过回调函数
	private static boolean gIsPayCallback = false; // 对应退出支付

	// 退出平台时 支付是否完成回调过回调函数
	private static boolean gIsExitCallback = true; // 是否调用 退出平台回调函数

	public static String gAppId = ""; // appid
	
	public static String member_id=""; //SDK的userid
	
	public static String name = "";  //游戏名

	//public static String gAppKey = "90c1bd1f31295130"; // appKey
	public static String gAppKey = "yw&^OwszzlgameYWgame";
	public static Context gContext = null; // 全局 context

	public static String gIMSI = null;

	public static CSJBaseUserInfo gUserInfo = null;

	public static ArrayList<String> gCookiesList = null;

	public static boolean gIsPayForAllGame = false; // 支付方式 : 应用内支付，所有支付

	public static boolean gIsDebugModel = false; // 是否为debug模式

	public static boolean gIsTourist; // 是否为游客登录

	public static String gPayAppId = "";// 支付游戏的app id

	public static String g19payCardType = "CMJFK00010001|CMJFK||全国移动充值卡|CMJFK00010014|CMJFK||福建移动呱呱通充值卡|CMJFK00010112|CMJFK||浙江移动缴费券|DXJFK00010001|DXJFK||中国电信充值付费卡|GMJFK00010001|GMJFK||盛大一卡通|GMJFK00010002|GMJFK||征途游戏卡|GMJFK00010003|GMJFK||搜狐一卡通|GMJFK00010005|GMJFK||久游一卡通|GMJFK00010006|GMJFK||腾讯Q币卡|GMJFK00010007|GMJFK||骏网一卡通|GMJFK00010011|GMJFK||完美一卡通|GMJFK00010012|GMJFK||网易一卡通|GMJFK00010013|GMJFK||天宏一卡通|GMJFK00010014|GMJFK||盛付通卡|GMJFK00016186|GMJFK||纵游一卡通|LTJFK00020000|LTJFK||全国联通一卡充"; // 19pay支付渠道充值卡类型

	public static String gChannel = ""; // 渠道编码

	public static String gBand = "";// 手机品牌

	public static String gModel = "";// 手机型号

	public static String gMacAddress = "";

	public static String gDeviceId = "";

	public static String gDeviceOSVersion = "";
	
	public static boolean gIsFloat=false;
	
	public static WindowManager gFloatWindowManager=null;
	
	public static int gAccountType = 3;// 账号类型 0 qq 1 微信 	3yaowan
	
	/**
	 * 设置支付请求 是否 回调
	 * 
	 * @param isCallback
	 */
	public static void setIsPayCallback(boolean isCallback) {
		gIsPayCallback = isCallback;
	}

	/**
	 * 获取支付请求是否被回调
	 * 
	 * @return
	 */
	public static boolean isPayCallback() {
		return gIsPayCallback;
	}

	/**
	 * 设置支付请求 是否 回调
	 * 
	 * @param isCallback
	 */
	public static void setIsExitCallback(boolean isCallback) {
		gIsExitCallback = isCallback;
	}

	/**
	 * 获取支付请求是否被回调
	 * 
	 * @return
	 */
	public static boolean isExitCallback() {
		return gIsExitCallback;
	}
}
