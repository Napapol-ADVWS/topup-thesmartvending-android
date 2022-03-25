package com.lomoment.serialportsample;

import android.app.Application;

import java.util.Locale;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!LanguageUtils.comparison(this, Locale.ENGLISH)) {
            LanguageUtils.changeAppLanguage(this, Locale.ENGLISH);
        }
    }
}
