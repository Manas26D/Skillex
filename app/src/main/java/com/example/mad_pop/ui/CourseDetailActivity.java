package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.example.mad_pop.R;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseDetailActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        long courseId = getIntent().getLongExtra("course_id", -1L);
        if (courseId < 0) {
            finish();
            return;
        }

        CourseRepository repository = new CourseRepository(this);
        ImageView imageView = findViewById(R.id.ivCourseImage);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvMentor = findViewById(R.id.tvDetailMentor);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvDates = findViewById(R.id.tvDetailDates);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvEnrollment = findViewById(R.id.tvDetailEnrollment);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        Button btnEnroll = findViewById(R.id.btnEnrollCourse);
        ImageButton btnBack = findViewById(R.id.btnBackCourse);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        executorService.execute(() -> {
            try {
                Course course = repository.getCourseById(courseId);
                boolean enrolled = repository.isUserEnrolled(sessionManager.getUserId(), courseId);
                int enrollmentCount = repository.getEnrollmentCountForCourse(courseId);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                if (course == null) {
                    Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                tvTitle.setText(course.getTitle());
                tvMentor.setText(getString(R.string.mentor_label, course.getMentorName()));
                tvCategory.setText(getString(R.string.category_label, course.getCategory()));
                tvDates.setText(getString(R.string.schedule_label, course.getStartDate(), course.getEndDate()));
                tvPrice.setText(getString(R.string.course_price_value, course.getPrice()));
                tvEnrollment.setText(getString(R.string.course_enrollment_count, enrollmentCount));
                tvDescription.setText(course.getDescription() == null || course.getDescription().trim().isEmpty()
                        ? getString(R.string.no_data)
                        : course.getDescription());
                imageView.setImageResource(resolveImageRes(course.getImageUrl()));

                boolean isMentee = "MENTEE".equals(sessionManager.getRole());
                btnEnroll.setVisibility(isMentee ? View.VISIBLE : View.GONE);
                btnEnroll.setEnabled(!enrolled);
                btnEnroll.setText(enrolled ? getString(R.string.enrolled) : getString(R.string.enroll_now));

                btnEnroll.setOnClickListener(v -> {
                    if (enrolled) {
                        Toast.makeText(this, "Already enrolled", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent paymentIntent = new Intent(this, PaymentActivity.class);
                    paymentIntent.putExtra("course_id", courseId);
                    startActivity(paymentIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                 });
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Unable to load course details", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private int resolveImageRes(String imageKey) {
        if ("demo_android".equals(imageKey)) {
            return android.R.drawable.ic_menu_manage;
        }
        if ("demo_sql".equals(imageKey)) {
            return android.R.drawable.ic_menu_sort_by_size;
        }
        if ("demo_uiux".equals(imageKey) || "uiux".equals(imageKey)) {
            return R.drawable.uiux;
        }
        if ("demo_python".equals(imageKey) || "datascience".equals(imageKey)) {
            return R.drawable.datascience;
        }
        return android.R.drawable.ic_menu_report_image;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
