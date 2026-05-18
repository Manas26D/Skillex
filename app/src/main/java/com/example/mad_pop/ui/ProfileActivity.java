package com.example.mad_pop.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;
import com.example.mad_pop.data.AuthRepository;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.model.User;
import com.example.mad_pop.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        String role = sessionManager.getRole();
        if (userId < 0 || (!"MENTOR".equals(role) && !"MENTEE".equals(role))) {
            finish();
            return;
        }

        AuthRepository authRepository = new AuthRepository(this);
        CourseRepository courseRepository = new CourseRepository(this);

        EditText etName = findViewById(R.id.etProfileName);
        EditText etEmail = findViewById(R.id.etProfileEmail);
        TextView tvRole = findViewById(R.id.tvProfileRole);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        Button btnLogout = findViewById(R.id.btnProfileLogout);

        View cardMentee = findViewById(R.id.cardMenteeEnrollments);
        View cardMentor = findViewById(R.id.cardMentorEarnings);
        TextView tvEnrolledCourses = findViewById(R.id.tvEnrolledCourses);
        TextView tvMentorEarnings = findViewById(R.id.tvMentorEarnings);

        tvRole.setText(getString(R.string.role_label, role));
        cardMentee.setVisibility("MENTEE".equals(role) ? View.VISIBLE : View.GONE);
        cardMentor.setVisibility("MENTOR".equals(role) ? View.VISIBLE : View.GONE);

        executorService.execute(() -> {
            User user = authRepository.getUserById(userId);
            List<Course> enrolled = "MENTEE".equals(role) ? courseRepository.getEnrolledCoursesForMentee(userId) : null;
            double earnings = "MENTOR".equals(role) ? courseRepository.getTotalEarningsForMentor(userId) : 0;

            runOnUiThread(() -> {
                if (user != null) {
                    etName.setText(user.getFullName());
                    etEmail.setText(user.getEmail());
                }

                if ("MENTEE".equals(role)) {
                    if (enrolled == null || enrolled.isEmpty()) {
                        tvEnrolledCourses.setText(R.string.no_data);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (Course course : enrolled) {
                            if (sb.length() > 0) {
                                sb.append("\n");
                            }
                            sb.append(getString(R.string.profile_course_line, course.getTitle(), course.getPrice()));
                        }
                        tvEnrolledCourses.setText(sb.toString());
                    }
                }

                if ("MENTOR".equals(role)) {
                    tvMentorEarnings.setText(getString(R.string.total_earnings_value, earnings));
                }
            });
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!"admin".equals(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            executorService.execute(() -> {
                boolean ok = authRepository.updateProfile(userId, name, email);
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, ok ? "Profile updated" : "Unable to update profile", Toast.LENGTH_SHORT).show();
                });
            });
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finishAffinity();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}



