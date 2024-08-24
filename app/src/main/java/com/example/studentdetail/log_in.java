package com.example.studentdetail;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class log_in extends AppCompatActivity {
    TextView t3;
    Button log;
    EditText mail, pass;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        t3 = findViewById(R.id.t3);
        log = findViewById(R.id.b1);
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        progressBar = findViewById(R.id.pb);
        log.setOnClickListener(view -> login());
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(log_in.this, Create_account.class));
           finish(); }
        });
            }



    void login() {
        String email = mail.getText().toString();
        String password = pass.getText().toString();
        boolean isvalid = valid(email, password);
        if (!isvalid) {
            return;
        }

        logacc(email, password);
    }

    void logacc(String email, String password) {
        pbar(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pbar(false);
                if (task.isSuccessful()) {
                    if(firebaseAuth.getCurrentUser().isEmailVerified()){
                        Toast.makeText(log_in.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(log_in.this, MainActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(log_in.this, "Please Verify your Email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(log_in.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    void pbar(boolean inprogress) {
        if (inprogress) {
            progressBar.setVisibility(View.VISIBLE);
            log.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            log.setVisibility(View.VISIBLE);
        }
    }

    boolean valid(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mail.setError("Invalid Email");
            return false;

        }
        if (password.length() < 6) {
            pass.setError("Password is Invalid");
            return false;
        }
        return true;
    }
}