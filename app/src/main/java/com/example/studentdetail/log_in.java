package com.example.studentdetail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class log_in extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private TextView registerText,textView;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI elements
        emailInput = findViewById(R.id.mail);
        textView = findViewById(R.id.textView2);
        passwordInput = findViewById(R.id.pass);
        loginButton = findViewById(R.id.b1);
        progressBar = findViewById(R.id.pb);
        registerText = findViewById(R.id.t3);

        // Redirect to registration activity
        registerText.setOnClickListener(view -> {
            startActivity(new Intent(log_in.this, Create_account.class));
            finish();
        });

        // Login on button click
        loginButton.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate input
        if (!validateInputs(email, password)) {
            return;
        }

        // Show progress bar
        showProgress(true);

        // Sign in with email and password
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Fetch and save user roll number from Realtime Database
                            fetchUserRollNo(user.getEmail());
                        } else {
                            // Send verification email if not verified
                            if (user != null) {
                                user.sendEmailVerification();
                                auth.signOut();
                            }
                            Toast.makeText(log_in.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(log_in.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("Password should be at least 6 characters");
            passwordInput.requestFocus();
            return false;
        }
        return true;
    }

    private void fetchUserRollNo(String email) {
        // Query the database for the student's roll number using their email
        databaseReference.orderByChild("Email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String rollNo = userSnapshot.child("Roll No").getValue(String.class);

                                if (rollNo != null) {
                                    // Save roll number to SharedPreferences
                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("roll no", rollNo);
                                    editor.apply();

                                    Toast.makeText(log_in.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(log_in.this, MainActivity.class)); // Navigate to home page or dashboard
                                    finish();
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(log_in.this, "Roll number not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(log_in.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        loginButton.setVisibility(inProgress ? View.GONE : View.VISIBLE);
    }
}
