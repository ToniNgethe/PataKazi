package com.toni.patakazi.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.toni.patakazi.App;
import com.toni.patakazi.model.responses.LoginResponse;

/**
 * Created by toni on 12/22/17.
 */

public class PrefManager {
    private static final String USER_INFO = "user_information";
    private static final String LOGGED_IN = "logged_in";
    private static Gson gson = new Gson();

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()
                .getApplicationContext());
    }

    public static int getInt(String preferenceKey, int preferenceDefaultValue) {
        return getPreferences().getInt(preferenceKey, preferenceDefaultValue);
    }

    public static void putInt(String preferenceKey, int preferenceValue) {
        getPreferences().edit().putInt(preferenceKey, preferenceValue).apply();
    }

    public static boolean getBoolean(String preferenceKey, boolean preferenceDefaultValue) {
        return getPreferences().getBoolean(preferenceKey, preferenceDefaultValue);
    }

    public static void putBoolean(String preferenceKey, boolean preferenceValue) {
        getPreferences().edit().putBoolean(preferenceKey, preferenceValue).apply();
    }


    public static String getString(String preferenceKey, String preferenceDefaultValue) {
        return getPreferences().getString(preferenceKey, preferenceDefaultValue);
    }

    public static void putString(String preferenceKey, String preferenceValue) {
        getPreferences().edit().putString(preferenceKey, preferenceValue).apply();
    }

    public static void clearPrefs() {
        getPreferences().edit().clear().apply();
    }

    // store user info
    public static void storeUser(LoginResponse user) {
        putString(USER_INFO, gson.toJson(user));
    }

    // get user info
    public static LoginResponse getUserInfo() {
        String storedUser = getString(USER_INFO, null);
        return gson.fromJson(storedUser, LoginResponse.class);
    }


    //set login session..
    public static void setLoggedIn() {
        putBoolean(LOGGED_IN, true);
    }

    // check if user is logged in or not
    public static boolean isLoggedIn() {
        return getBoolean(LOGGED_IN, false);
    }


    // get user email
    public static String userEmail() {
        LoginResponse loginResponse = getUserInfo();
        return loginResponse.getData().getEmail();
    }

}
