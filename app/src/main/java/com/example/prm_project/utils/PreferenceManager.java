package com.example.prm_project.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "pet_app_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean value) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
