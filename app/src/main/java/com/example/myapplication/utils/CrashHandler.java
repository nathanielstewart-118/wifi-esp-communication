package com.example.myapplication.utils;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.interfaces.LogCallback;

import java.io.IOException;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e("CRASH_HANDLER", "App crashed!", throwable);
        // Optionally write to a file
        try {
            LogHelper.sendLogWithCallback(
                Constants.LOGGING_BASE_URL,
                Constants.LOGGING_REQUEST_METHOD,
                "App crashed due to " + Log.getStackTraceString(throwable) + " at " + new Date().toString(),
                Constants.LOGGING_BEARER_TOKEN, new LogCallback() {
                    @Override
                    public void onSuccess() {
                        System.exit(1);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        System.exit(1);
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);

        }
        // Let the default handler (or system) handle it afterward
    }
}

