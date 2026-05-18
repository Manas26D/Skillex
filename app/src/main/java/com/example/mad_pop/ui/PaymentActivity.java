package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;
import com.example.mad_pop.data.CourseRepository;
import com.example.mad_pop.model.Course;
import com.example.mad_pop.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        if (userId < 0 || !"MENTEE".equals(sessionManager.getRole())) {
            finish();
            return;
        }

        long courseId = getIntent().getLongExtra("course_id", -1L);
        if (courseId < 0) {
            finish();
            return;
        }

        CourseRepository repository = new CourseRepository(this);
        TextView tvCourse = findViewById(R.id.tvPaymentCourse);
        TextView tvAmount = findViewById(R.id.tvPaymentAmount);
        RadioGroup rgMethod = findViewById(R.id.rgPaymentMethod);
        RadioButton rbGpay = findViewById(R.id.rbGpay);
        LinearLayout layoutCardFields = findViewById(R.id.layoutCardFields);
        Button btnPayNow = findViewById(R.id.btnPayNow);

        EditText etCardNumber = findViewById(R.id.etCardNumber);
        EditText etCardHolder = findViewById(R.id.etCardHolder);
        EditText etCardExpiry = findViewById(R.id.etCardExpiry);
        EditText etCardCvv = findViewById(R.id.etCardCvv);

        final Course[] selectedCourse = new Course[1];
        executorService.execute(() -> {
            Course course = repository.getCourseById(courseId);
            runOnUiThread(() -> {
                selectedCourse[0] = course;
                if (course == null) {
                    Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                tvCourse.setText(getString(R.string.course_label, course.getTitle()));
                tvAmount.setText(getString(R.string.payment_amount, course.getPrice()));
            });
        });

        rgMethod.setOnCheckedChangeListener((group, checkedId) -> {
            boolean cardMode = checkedId == R.id.rbCard;
            layoutCardFields.setVisibility(cardMode ? View.VISIBLE : View.GONE);
        });

        btnPayNow.setOnClickListener(v -> {
            Course course = selectedCourse[0];
            if (course == null) {
                return;
            }

            boolean cardMode = rgMethod.getCheckedRadioButtonId() == R.id.rbCard;
            if (cardMode) {
                String cardNumber = etCardNumber.getText().toString().trim();
                String holder = etCardHolder.getText().toString().trim();
                String expiry = etCardExpiry.getText().toString().trim();
                String cvv = etCardCvv.getText().toString().trim();

                if (cardNumber.length() < 12 || holder.isEmpty() || expiry.length() < 4 || cvv.length() < 3) {
                    Toast.makeText(this, "Enter valid card details", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String method = rbGpay.isChecked() ? "GPAY_QR" : "CARD";
            btnPayNow.setEnabled(false);
            executorService.execute(() -> {
                boolean ok = repository.enrollInCourseWithPayment(userId, courseId, method);
                runOnUiThread(() -> {
                    btnPayNow.setEnabled(true);
                    if (!ok) {
                        Toast.makeText(this, "Already enrolled or payment failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent();
                    intent.setClassName(this, "com.example.mad_pop.ui.PaymentConfirmationActivity");
                    intent.putExtra("course_title", course.getTitle());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                });
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
}


