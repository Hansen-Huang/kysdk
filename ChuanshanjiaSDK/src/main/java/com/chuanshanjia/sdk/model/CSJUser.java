package com.chuanshanjia.sdk.model;

/**
 * Created by chuanshanjia on 18/04/03.
 */
public class CSJUser {
    private String accountId;
    private String token;

    public String getAccountId(){
        return accountId;
    }

    public void setAccountId(String accountId){
        this.accountId = accountId;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }
}
