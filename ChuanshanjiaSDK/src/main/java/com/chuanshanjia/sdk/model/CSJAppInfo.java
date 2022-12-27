package com.chuanshanjia.sdk.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.util.SortedMap;
import java.util.TreeMap;

public class CSJAppInfo {
	public static Context ctx;
	public static String appId;
	public static String appKey;
	public static String wxAppId;
	public static String wxAppSecret;
	public static String wxApiKey;
	public static String qqAppId;
	public static String wbAppId;
	public static String channelId;
	
	public String getWxAppId() {
		return wxAppId;
	}

	public void setWxAppId(String wxAppId) {
		this.wxAppId = wxAppId;
	}

	public String getWxAppSecret() {
		return wxAppSecret;
	}

	public void setWxAppSecret(String wxAppSecret) {
		this.wxAppSecret = wxAppSecret;
	}

	public String getWxApiKey() {
		return wxApiKey;
	}



	public void setWxApiKey(String wxApiKey) {
		this.wxApiKey = wxApiKey;
	}


	public Context getCtx() {
		return ctx;
	}
	
	public void setCtx(Context ctx) {
		this.ctx = ctx;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public String getQqAppId() {
		return qqAppId;
	}
	public void setQqAppId(String qqAppId) {
		this.qqAppId = qqAppId;
	}
	public String getWbAppId() {
		return wbAppId;
	}
	public void setWbAppId(String wbAppId) {
		this.wbAppId = wbAppId;
	}

	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public SortedMap<String, String> getBaseInfo(Context context){

		SortedMap<String, String> hashMap = new TreeMap<String, String>();
		hashMap.put("appid",CSJAppInfo.appId);

		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			//hashMap.put("channel",info.metaData.getString("channel_code"));
		} catch (Exception e) {

		}

		hashMap.put("band", android.os.Build.BRAND);
		hashMap.put("model", android.os.Build.MODEL);
		String macAddress = null;
		WifiManager wifiMgr = (WifiManager) context.getSystemService(
				Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		if (null != info) {
			macAddress = info.getMacAddress();
		}
		hashMap.put("mac", macAddress);

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		hashMap.put("device_id", "shfkjsdhfjkshf");

		hashMap.put("device_version", android.os.Build.VERSION.RELEASE);

		return hashMap;
	}
}
