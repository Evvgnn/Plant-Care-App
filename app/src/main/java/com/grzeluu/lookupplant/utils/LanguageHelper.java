package com.grzeluu.lookupplant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LanguageHelper {

    private static final String PREFS_NAME = "Settings";
    private static final String LANGUAGE_KEY = "language";

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE_KEY, "en");
    }

    public static void loadLocale(Context context) {
        String language = getLanguage(context);
        setLocale(context, language);
    }
}