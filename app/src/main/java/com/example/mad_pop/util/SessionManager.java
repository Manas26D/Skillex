package com.example.mad_pop.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "skillex_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId, String role) {
        preferences.edit().putLong(KEY_USER_ID, userId).putString(KEY_ROLE, role).apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }
}

