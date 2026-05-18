package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.MentorAnalytics;
import com.example.mad_pop.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MentorDashboardActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_dashboard);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();

        Button btnCourses = findViewById(R.id.btnMyCourses);
        Button btnChats = findViewById(R.id.btnMentorChats);
        View btnProfile = findViewById(R.id.btnMentorProfile);
        TextView tvTotalCourses = findViewById(R.id.tvTotalCourses);
        TextView tvTotalEnrollments = findViewById(R.id.tvTotalEnrollments);
        TextView tvTopCourse = findViewById(R.id.tvTopCourse);
        TextView tvStudentsEnrolled = findViewById(R.id.tvStudentsEnrolled);

        btnCourses.setOnClickListener(v -> openWithTransition(MentorCoursesActivity.class));
        btnChats.setOnClickListener(v -> openWithTransition(MentorChatsActivity.class));
        btnProfile.setOnClickListener(v -> openWithTransition(ProfileActivity.class));


        if (userId < 0 || !"MENTOR".equals(sessionManager.getRole())) {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
            return;
        }

        CourseRepository courseRepository = new CourseRepository(this);
        executorService.execute(() -> {
            MentorAnalytics analytics = courseRepository.getMentorAnalytics(userId);
            runOnUiThread(() -> {
                tvTotalCourses.setText(getString(R.string.analytics_courses_created, analytics.getTotalCourses()));
                tvTotalEnrollments.setText(getString(R.string.analytics_total_enrollments, analytics.getTotalEnrollments()));
                tvTopCourse.setText(getString(R.string.analytics_top_course, analytics.getTopCourseTitle(), analytics.getTopCourseEnrollments()));
                tvStudentsEnrolled.setText(getString(R.string.analytics_students_enrolled, analytics.getEnrolledStudentsSummary()));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void openWithTransition(Class<?> destination) {
        startActivity(new Intent(this, destination));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}

