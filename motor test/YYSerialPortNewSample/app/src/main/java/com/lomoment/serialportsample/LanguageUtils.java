package com.lomoment.serialportsample;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageUtils {

    public static boolean comparison(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (configuration.getLocales().get(0).getLanguage() == locale.getLanguage()) {
                return true;
            }
        } else {
            if (configuration.locale == locale) {
                return true;
            }
        }
        return false;
    }


    /**
     * 更改应用语言
     *
     * @param locale
     */
    public static void changeAppLanguage(Context context, Locale locale) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        context.getResources().updateConfiguration(configuration, metrics);
    }
}
