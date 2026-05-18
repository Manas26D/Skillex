package com.example.mad_pop.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.adapter.CourseAdapter;
import com.example.mad_pop.adapter.UserAdapter;
import com.example.mad_pop.data.AuthRepository;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.model.User;
import com.example.mad_pop.util.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MentorDataVaultActivity extends AppCompatActivity {
    private AuthRepository authRepository;
    private CourseRepository courseRepository;
    private SessionManager sessionManager;
    private UserAdapter userAdapter;
    private CourseAdapter courseAdapter;
    private TextView tvDataDump;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_vault);

        sessionManager = new SessionManager(this);
        if (!"ADMIN".equals(sessionManager.getRole())) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
            return;
        }

        authRepository = new AuthRepository(this);
        courseRepository = new CourseRepository(this);

        EditText etName = findViewById(R.id.etAdminName);
        EditText etEmail = findViewById(R.id.etAdminEmail);
        EditText etPassword = findViewById(R.id.etAdminPassword);
        Spinner spinnerRole = findViewById(R.id.spinnerAdminRole);
        Button btnAddUser = findViewById(R.id.btnAddUser);
        Button btnAddCourse = findViewById(R.id.btnAddCourseAdmin);
        EditText etCourseTitle = findViewById(R.id.etAdminCourseTitle);
        EditText etCourseCategory = findViewById(R.id.etAdminCourseCategory);
        EditText etCourseDescription = findViewById(R.id.etAdminCourseDescription);
        EditText etCourseStartDate = findViewById(R.id.etAdminCourseStartDate);
        EditText etCourseEndDate = findViewById(R.id.etAdminCourseEndDate);
        EditText etCourseImage = findViewById(R.id.etAdminCourseImage);
        EditText etCoursePrice = findViewById(R.id.etAdminCoursePrice);
        Spinner spinnerMentor = findViewById(R.id.spinnerAdminMentor);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);
        tvDataDump = findViewById(R.id.tvDataDump);

        btnAdminLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        attachDatePicker(etCourseStartDate);
        attachDatePicker(etCourseEndDate);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"ADMIN", "MENTOR", "MENTEE"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        RecyclerView recyclerUsers = findViewById(R.id.recyclerAdminUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this::openUserEditor);
        recyclerUsers.setAdapter(userAdapter);

        RecyclerView recyclerCourses = findViewById(R.id.recyclerAdminCourses);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        courseAdapter = new CourseAdapter(this::openCourseEditor);
        recyclerCourses.setAdapter(courseAdapter);

        btnAddUser.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String role = (String) spinnerRole.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!"admin".equals(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAddUser.setEnabled(false);
            executorService.execute(() -> {
                boolean ok = authRepository.register(name, email, password, role);
                runOnUiThread(() -> {
                    btnAddUser.setEnabled(true);
                    if (!ok) {
                        Toast.makeText(this, "Unable to add user. Email may already exist.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    etName.setText("");
                    etEmail.setText("");
                    etPassword.setText("");
                    Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
                    refreshData();
                });
            });
        });

        btnAddCourse.setOnClickListener(v -> {
            String title = etCourseTitle.getText().toString().trim();
            String category = etCourseCategory.getText().toString().trim();
            String description = etCourseDescription.getText().toString().trim();
            String startDate = etCourseStartDate.getText().toString().trim();
            String endDate = etCourseEndDate.getText().toString().trim();
            String imageKey = etCourseImage.getText().toString().trim();
            String priceText = etCoursePrice.getText().toString().trim();
            MentorSelection mentorSelection = (MentorSelection) spinnerMentor.getSelectedItem();

            if (mentorSelection == null || title.isEmpty() || category.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Please complete all course fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!courseRepository.isValidCourseDateRange(startDate, endDate)) {
                Toast.makeText(this, "Invalid dates. Use yyyy-MM-dd and keep end after start.", Toast.LENGTH_LONG).show();
                return;
            }

            final double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Enter a valid numeric price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!courseRepository.isValidCoursePrice(price)) {
                Toast.makeText(this, "Price must be 0 or more", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAddCourse.setEnabled(false);
            executorService.execute(() -> {
                boolean ok = courseRepository.addCourse(title, category, description, mentorSelection.userId, startDate, endDate, imageKey.isEmpty() ? null : imageKey, price);
                runOnUiThread(() -> {
                    btnAddCourse.setEnabled(true);
                    if (!ok) {
                        Toast.makeText(this, "Unable to add course", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    etCourseTitle.setText("");
                    etCourseCategory.setText("");
                    etCourseDescription.setText("");
                    etCourseStartDate.setText("");
                    etCourseEndDate.setText("");
                    etCourseImage.setText("");
                    etCoursePrice.setText("");
                    Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show();
                    refreshData();
                });
            });
        });

        refreshData();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void openUserEditor(User user) {
        if (user.getId() == sessionManager.getUserId()) {
            Toast.makeText(this, "You cannot modify your own account from this panel", Toast.LENGTH_SHORT).show();
        }

        EditText etName = new EditText(this);
        etName.setHint("Full name");
        etName.setText(user.getFullName());

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(user.getEmail());

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"ADMIN", "MENTOR", "MENTEE"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(user.getRole()));

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int p = 24;
        layout.setPadding(p, p, p, p);
        layout.addView(etName);
        layout.addView(etEmail);
        layout.addView(spinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Edit user")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    if (user.getId() == sessionManager.getUserId()) {
                        return;
                    }
                    String updatedEmail = etEmail.getText().toString().trim();
                    if (!"admin".equals(updatedEmail) && !Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
                        Toast.makeText(this, "Enter a valid email format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executorService.execute(() -> {
                        boolean ok = authRepository.updateUser(
                                user.getId(),
                                etName.getText().toString().trim(),
                                updatedEmail,
                                (String) spinner.getSelectedItem()
                        );
                        runOnUiThread(() -> {
                            Toast.makeText(this, ok ? "User updated" : "Unable to update user", Toast.LENGTH_SHORT).show();
                            refreshData();
                        });
                    });
                })
                .setNegativeButton("Cancel", null);

        if (user.getId() != sessionManager.getUserId()) {
            builder.setNeutralButton("Delete", (dialog, which) -> executorService.execute(() -> {
                boolean deleted = authRepository.deleteUserById(user.getId());
                runOnUiThread(() -> {
                    Toast.makeText(this, deleted ? "User deleted" : "Unable to delete user", Toast.LENGTH_SHORT).show();
                    refreshData();
                });
            }));
        }

        builder.show();
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
                            refreshData();
                        });
                    });
                })
                .setNeutralButton("Delete", (dialog, which) -> executorService.execute(() -> {
                    boolean deleted = courseRepository.deleteCourse(course.getId());
                    runOnUiThread(() -> {
                        Toast.makeText(this, deleted ? "Course deleted" : "Unable to delete course", Toast.LENGTH_SHORT).show();
                        refreshData();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshData() {
        executorService.execute(() -> {
            List<User> users = authRepository.getAllUsers();
            List<Course> courses = courseRepository.getAllCourses();
            String dataDump = authRepository.exportAllDataForMentor();
            List<MentorSelection> mentorSelections = new ArrayList<>();
            for (User user : users) {
                if ("MENTOR".equals(user.getRole())) {
                    mentorSelections.add(new MentorSelection(user.getId(), user.getFullName()));
                }
            }
            runOnUiThread(() -> {
                userAdapter.submitList(users);
                courseAdapter.submitList(courses);
                tvDataDump.setText(dataDump);

                Spinner spinnerMentor = findViewById(R.id.spinnerAdminMentor);
                ArrayAdapter<MentorSelection> mentorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mentorSelections);
                mentorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMentor.setAdapter(mentorAdapter);
            });
        });
    }

    private static class MentorSelection {
        final long userId;
        final String name;

        MentorSelection(long userId, String name) {
            this.userId = userId;
            this.name = name;
        }

        @Override
        @NonNull
        public String toString() {
            return name;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
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
                // Keep today's date when existing text is not parseable.
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
