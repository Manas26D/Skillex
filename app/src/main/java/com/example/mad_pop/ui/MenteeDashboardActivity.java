package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.adapter.CourseAdapter;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenteeDashboardActivity extends AppCompatActivity {
    private CourseRepository courseRepository;
    private CourseAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentee_dashboard);

        SessionManager sessionManager = new SessionManager(this);
        if (!"MENTEE".equals(sessionManager.getRole())) {
            sessionManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
            return;
        }

        courseRepository = new CourseRepository(this);
        adapter = new CourseAdapter(this::openCourseDetailScreen);

        RecyclerView recyclerCourses = findViewById(R.id.recyclerMenteeCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourses.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etCourseSearch);
        Button btnSearch = findViewById(R.id.btnSearchCourse);
        Button btnMentorChat = findViewById(R.id.btnMentorList);
        View btnProfile = findViewById(R.id.btnMenteeProfile);

        btnSearch.setOnClickListener(v -> loadCourses(etSearch.getText().toString()));
        btnMentorChat.setOnClickListener(v -> {
            startActivity(new Intent(this, MentorListActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        loadCourses(null);
    }

    private void loadCourses(String query) {
        executorService.execute(() -> {
            List<Course> courses = courseRepository.searchCourses(query);
            runOnUiThread(() -> adapter.submitList(courses));
        });
    }

    private void openCourseDetailScreen(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
