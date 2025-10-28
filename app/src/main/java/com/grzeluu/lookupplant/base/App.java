package com.grzeluu.lookupplant.base;

import static com.grzeluu.lookupplant.utils.notification.NotificationUtils.createNotificationChannel;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.grzeluu.lookupplant.utils.LocaleHelper;

public class App extends Application {

    private static Context context;


    public static Context getAppContext() {
        return App.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String lang = prefs.getString(LocaleHelper.KEY_LANGUAGE, "en");

        LocaleHelper.setLocale(this, lang);

        App.context = getApplicationContext();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        createNotificationChannel(context);
    }

}
