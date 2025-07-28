package com.prtech.spiapp;

import android.app.Application;

import com.prtech.spiapp.utils.CrashHandler;

public class WiFiApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}
