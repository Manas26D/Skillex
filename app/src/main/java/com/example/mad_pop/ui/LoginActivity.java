package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;
import com.example.mad_pop.data.AuthRepository;
import com.example.mad_pop.model.User;
import com.example.mad_pop.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private SessionManager sessionManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authRepository = new AuthRepository(this);
        sessionManager = new SessionManager(this);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        RadioGroup roleGroup = findViewById(R.id.roleGroup);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            int selectedRoleId = roleGroup.getCheckedRadioButtonId();
            String role;
            if (selectedRoleId == R.id.rbMentor) {
                role = "MENTOR";
            } else if (selectedRoleId == R.id.rbAdmin) {
                role = "ADMIN";
            } else {
                role = "MENTEE";
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!"admin".equals(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("ADMIN".equals(role) && (!"admin".equals(email) || !"admin".equals(password))) {
                Toast.makeText(this, "Admin login requires email: admin and password: admin", Toast.LENGTH_LONG).show();
                return;
            }

            btnLogin.setEnabled(false);
            executorService.execute(() -> {
                User user = authRepository.login(email, password, role);
                runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    if (user == null) {
                        Toast.makeText(this, "Invalid credentials for selected role", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sessionManager.login(user.getId(), user.getRole());
                    routeByRole(user.getRole());
                });
            });
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void routeByRole(String role) {
        Intent intent;
        if ("MENTOR".equals(role)) {
            intent = new Intent(this, MentorDashboardActivity.class);
        } else if ("ADMIN".equals(role)) {
            intent = new Intent(this, MentorDataVaultActivity.class);
        } else {
            intent = new Intent(this, MenteeDashboardActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
