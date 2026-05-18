package com.example.mad_pop.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.adapter.CourseAdapter;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.util.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MentorCoursesActivity extends AppCompatActivity {
    private CourseRepository courseRepository;
    private CourseAdapter courseAdapter;
    private long mentorId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_courses);

        SessionManager sessionManager = new SessionManager(this);
        mentorId = sessionManager.getUserId();
        if (!"MENTOR".equals(sessionManager.getRole())) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
            return;
        }

        courseRepository = new CourseRepository(this);
        courseAdapter = new CourseAdapter(this::openCourseEditor);

        RecyclerView recyclerView = findViewById(R.id.recyclerMentorCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(courseAdapter);

        Button btnAdd = findViewById(R.id.btnOpenAddCourse);
        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateCourseActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMentorCourses();
    }

    private void loadMentorCourses() {
        executorService.execute(() -> {
            List<Course> courses = courseRepository.getCoursesForMentor(mentorId);
            runOnUiThread(() -> courseAdapter.submitList(courses));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void openCourseEditor(Course course) {
        EditText etTitle = new EditText(this);
        etTitle.setHint("Course title");
        etTitle.setText(course.getTitle());

        EditText etCategory = new EditText(this);
        etCategory.setHint("Category");
        etCategory.setText(course.getCategory());

        EditText etDescription = new EditText(this);
        etDescription.setHint("Description");
        etDescription.setText(course.getDescription());

        EditText etStartDate = new EditText(this);
        etStartDate.setHint("Start date (yyyy-MM-dd)");
        etStartDate.setText(course.getStartDate());
        attachDatePicker(etStartDate);

        EditText etEndDate = new EditText(this);
        etEndDate.setHint("End date (yyyy-MM-dd)");
        etEndDate.setText(course.getEndDate());
        attachDatePicker(etEndDate);

        EditText etImageKey = new EditText(this);
        etImageKey.setHint("Image key");
        etImageKey.setText(course.getImageUrl());

        EditText etPrice = new EditText(this);
        etPrice.setHint("Price");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setText(String.valueOf(course.getPrice()));

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int p = 24;
        layout.setPadding(p, p, p, p);
        layout.addView(etTitle);
        layout.addView(etCategory);
        layout.addView(etDescription);
        layout.addView(etStartDate);
        layout.addView(etEndDate);
        layout.addView(etImageKey);
        layout.addView(etPrice);

        new AlertDialog.Builder(this)
                .setTitle("Edit course")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String startDate = etStartDate.getText().toString().trim();
                    String endDate = etEndDate.getText().toString().trim();
                    if (!courseRepository.isValidCourseDateRange(startDate, endDate)) {
                        Toast.makeText(this, "Invalid dates. Use yyyy-MM-dd and keep end after start.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    final double price;
                    try {
                        price = Double.parseDouble(etPrice.getText().toString().trim());
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Enter a valid numeric price", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!courseRepository.isValidCoursePrice(price)) {
                        Toast.makeText(this, "Price must be 0 or more", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    executorService.execute(() -> {
                        boolean ok = courseRepository.updateCourse(
                                course.getId(),
                                etTitle.getText().toString().trim(),
                                etCategory.getText().toString().trim(),
                                etDescription.getText().toString().trim(),
                                startDate,
                                endDate,
                                etImageKey.getText().toString().trim(),
                                price
                        );
                        runOnUiThread(() -> {
                            Toast.makeText(this, ok ? "Course updated" : "Unable to update course", Toast.LENGTH_SHORT).show();
                            loadMentorCourses();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void attachDatePicker(EditText target) {
        target.setShowSoftInputOnFocus(false);
        target.setOnClickListener(v -> showDatePicker(target));
        target.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker(target);
            }
        });
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        String current = target.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                LocalDate parsed = LocalDate.parse(current);
                calendar.set(parsed.getYear(), parsed.getMonthValue() - 1, parsed.getDayOfMonth());
            } catch (DateTimeParseException ignored) {
                // Keep current day when a date cannot be parsed.
            }
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> target.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }
}
