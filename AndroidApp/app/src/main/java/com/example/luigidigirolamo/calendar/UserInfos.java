package com.example.luigidigirolamo.calendar;

import android.app.Application;

public class UserInfos extends Application {
    private static UserInfos instance;
    private String username;
    private String token;
    private String ipAddress;
    private String password;

    public static UserInfos getInstance() {
        if(instance==null)
            instance = new UserInfos();
        return instance;
    }

    private UserInfos() {
    }

    public void setUserName(String username) {
        this.username = username;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUserName() {
        return this.username;
    }
    public String getToken() {
        return this.token;
    }
    public String getIpAddress() {
        return this.ipAddress;
    }
    public String getPassword() { return this.password; }
}
