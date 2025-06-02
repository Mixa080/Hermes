package com.example.aiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.aiapp.database.DatabaseHelper;
import android.util.Patterns;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty()) {
                usernameInput.setError("Введіть ім'я користувача");
                return;
            }
            if (username.length() < 6) {
                usernameInput.setError("Ім'я користувача повинно містити мінімум 6 символів");
                return;
            }

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

            long userId = dbHelper.registerUser(username, email, password);
            if (userId != -1) {
                Toast.makeText(this, "Реєстрація успішна!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Помилка реєстрації", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.loginLink).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
} 