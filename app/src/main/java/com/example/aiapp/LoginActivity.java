package com.example.aiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.aiapp.database.DatabaseHelper;
import com.example.aiapp.model.User;
import android.util.Log;
import android.util.Patterns;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private DatabaseHelper dbHelper;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Введіть email");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Введіть коректний email");
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Введіть пароль");
                return;
            }
            if (password.length() < 6) {
                passwordInput.setError("Пароль повинен містити мінімум 6 символів");
                return;
            }

            Log.d(TAG, "Attempting login for email: " + email);
            User user = dbHelper.loginUser(email, password);
            if (user != null) {
                Log.d(TAG, "Login successful for user ID: " + user.getId());
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Login failed for email: " + email);
                Toast.makeText(this, "Невірний email або пароль", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.registerLink).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }
} 