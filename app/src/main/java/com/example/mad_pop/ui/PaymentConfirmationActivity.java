package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mad_pop.R;

public class PaymentConfirmationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);

        String courseTitle = getIntent().getStringExtra("course_title");
        if (courseTitle == null || courseTitle.trim().isEmpty()) {
            courseTitle = getString(R.string.no_data);
        }

        TextView tvConfirmation = findViewById(R.id.tvPaymentConfirmation);
        Button btnContinue = findViewById(R.id.btnContinueLearning);
        tvConfirmation.setText(getString(R.string.payment_success_message, courseTitle));

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenteeDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }
}

