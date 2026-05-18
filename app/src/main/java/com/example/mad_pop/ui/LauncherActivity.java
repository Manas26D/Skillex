package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;
import com.example.mad_pop.util.SessionManager;

public class LauncherActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        handler.postDelayed(this::routeFromSession, 900);
    }

    private void routeFromSession() {
        Intent intent;
        try {
            SessionManager sessionManager = new SessionManager(this);
            if (sessionManager.isLoggedIn()) {
                String role = sessionManager.getRole();
                if ("MENTOR".equals(role)) {
                    intent = new Intent(this, MentorDashboardActivity.class);
                } else if ("ADMIN".equals(role)) {
                    intent = new Intent(this, MentorDataVaultActivity.class);
                } else if ("MENTEE".equals(role)) {
                    intent = new Intent(this, MenteeDashboardActivity.class);
                } else {
                    sessionManager.logout();
                    intent = new Intent(this, LoginActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
        } catch (Exception ex) {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}


