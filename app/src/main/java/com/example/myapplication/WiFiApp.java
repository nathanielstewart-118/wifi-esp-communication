package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.utils.CrashHandler;

public class WiFiApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}
