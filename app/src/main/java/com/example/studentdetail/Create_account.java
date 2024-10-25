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

import java.util.HashMap;
import java.util.Map;

public class Create_account extends AppCompatActivity {
    TextView t3;
    Button create;
    EditText mail, pass, roll_no;
    ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Firebase Auth and Database Reference
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI elements
        t3 = findViewById(R.id.t3);
        create = findViewById(R.id.b1);
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        roll_no = findViewById(R.id.rollno);
        progressBar = findViewById(R.id.pb);

        // Redirect to login
        t3.setOnClickListener(view -> {
            startActivity(new Intent(Create_account.this, log_in.class));
            finish();
        });

        // Create account on button click
        create.setOnClickListener(view -> validateAndCheckUser());
    }

    private void validateAndCheckUser() {
        String email = mail.getText().toString().trim();
        String password = pass.getText().toString().trim();
        String rollNo = roll_no.getText().toString().toUpperCase().trim();

        // Validate input
        if (!isInputValid(rollNo, email, password)) {
            return;
        }

        // Show progress bar while creating account
        pbar(true);

        // Check if email or roll number exists in Realtime Database
        checkIfUserExistsInDatabase(rollNo, email, () -> {
            // If user does not exist in Realtime Database, proceed with Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Create_account.this, task -> {
                        pbar(false);
                        if (task.isSuccessful()) {
                            saveUserData(rollNo, email);
                            sendVerificationEmail(task.getResult().getUser());
                        } else {
                            Toast.makeText(Create_account.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean isInputValid(String rollNo, String email, String password) {
        if (TextUtils.isEmpty(rollNo) || rollNo.length() < 7 || rollNo.length() > 8) {
            roll_no.setError("Invalid Roll Number");
            roll_no.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mail.setError("Invalid Email");
            mail.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            pass.setError("Password is too short");
            pass.requestFocus();
            return false;
        }

        return true;
    }

    private void checkIfUserExistsInDatabase(String rollNo, String email, Runnable onSuccess) {
        // Check if the email already exists
        databaseReference.orderByChild("Email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Email exists
                            pbar(false);
                            mail.setError("Email already exists");
                            mail.requestFocus();
                        } else {
                            // Check if the roll number exists
                            databaseReference.orderByChild("Roll No").equalTo(rollNo)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot rollSnapshot) {
                                            if (rollSnapshot.exists()) {
                                                // Roll number exists
                                                pbar(false);
                                                roll_no.setError("Roll Number already exists");
                                                roll_no.requestFocus();
                                            } else {
                                                // Both email and roll number do not exist, proceed with registration
                                                onSuccess.run();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            pbar(false);
                                            Toast.makeText(Create_account.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        pbar(false);
                        Toast.makeText(Create_account.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData(String rollNo, String email) {
        // Create a map of user data to save
        Map<String, Object> userData = new HashMap<>();
        userData.put("Roll No", rollNo);
        userData.put("Email", email);

        // Save data under the user's roll number in the Realtime Database
        databaseReference.child(rollNo)
                .setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    // Save Roll No to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("roll no", rollNo); // Save Roll No
                    editor.apply();

                    Toast.makeText(Create_account.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    startActivity(new Intent(Create_account.this, log_in.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(Create_account.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Create_account.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Create_account.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void pbar(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            create.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            create.setVisibility(View.VISIBLE);
        }
    }
}
