package com.example.myapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String languageCode) {
        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(newLocale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = newLocale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }

    public static void applyLocale(Activity activity, String languageCode) {
        Context context = setLocale(activity, languageCode);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        activity.getBaseContext().getResources().updateConfiguration(config, resources.getDisplayMetrics());
    }

}

