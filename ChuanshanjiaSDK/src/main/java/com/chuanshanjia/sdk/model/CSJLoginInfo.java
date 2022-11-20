package com.chuanshanjia.sdk.model;

public class CSJLoginInfo {
	long uid;
	String name;
	String pwd;
	long time;
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "LoginInfo [uid=" + uid + ", name=CSJ" + name + ", pwd=" + pwd
				+ ", time=" + time + "]";
	}
	
}
