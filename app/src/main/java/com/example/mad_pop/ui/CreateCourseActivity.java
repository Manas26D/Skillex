package com.example.mad_pop.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.example.mad_pop.R;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.util.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateCourseActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        CourseRepository repository = new CourseRepository(this);
        SessionManager sessionManager = new SessionManager(this);

        EditText etTitle = findViewById(R.id.etCourseTitle);
        EditText etCategory = findViewById(R.id.etCourseCategory);
        EditText etDescription = findViewById(R.id.etCourseDescription);
        EditText etStartDate = findViewById(R.id.etCourseStartDate);
        EditText etEndDate = findViewById(R.id.etCourseEndDate);
        EditText etImageKey = findViewById(R.id.etCourseImageKey);
        EditText etPrice = findViewById(R.id.etCoursePrice);
        Button btnSave = findViewById(R.id.btnSaveCourse);

        attachDatePicker(etStartDate);
        attachDatePicker(etEndDate);

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String startDate = etStartDate.getText().toString().trim();
            String endDate = etEndDate.getText().toString().trim();
            String imageKey = etImageKey.getText().toString().trim();
            String priceText = etPrice.getText().toString().trim();

            if (title.isEmpty() || category.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            final double price;
            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Enter a valid numeric price", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!repository.isValidCoursePrice(price)) {
                Toast.makeText(this, "Price must be 0 or more", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!repository.isValidCourseDateRange(startDate, endDate)) {
                Toast.makeText(this, "Use valid dates (yyyy-MM-dd) and keep end date after start date", Toast.LENGTH_LONG).show();
                return;
            }

            btnSave.setEnabled(false);
            executorService.execute(() -> {
                boolean ok = repository.addCourse(
                        title,
                        category,
                        description,
                        sessionManager.getUserId(),
                        startDate,
                        endDate,
                        imageKey.isEmpty() ? null : imageKey,
                        price
                );
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    if (ok) {
                        Toast.makeText(this, "Course posted", Toast.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        Toast.makeText(this, "Unable to save course", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
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
                // Fallback to today's date when current text is malformed.
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
