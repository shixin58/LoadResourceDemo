package com.lonelypluto.loadresourcedemo.application;

import android.app.Application;

import com.lonelypluto.loadresourcedemo.utils.SharedPreferencesUtil;
import com.lonelypluto.loadresourcedemo.utils.LoadResourceUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferencesUtil.init(this);
        LoadResourceUtil.getInstance().init(this, SharedPreferencesUtil.getResourcePath());
    }
}
