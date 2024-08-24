package com.example.studentdetail;

import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class Create_account extends AppCompatActivity {
    TextView t3;
    Button create;
    EditText mail, pass;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        t3 = findViewById(R.id.t3);
        create = findViewById(R.id.b1);
        mail = findViewById(R.id.mail);
        pass = findViewById(R.id.pass);
        progressBar = findViewById(R.id.pb);
        t3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Create_account.this, log_in.class));
                finish();
            }
        });
        create.setOnClickListener(view -> create());
    }

        void create () {
            String email = mail.getText().toString();
            String password = pass.getText().toString();
            boolean isvalid = valid(email, password);
            if(!isvalid){
                return;
            }
            Cacc(email, password);
    }
        void Cacc(String email, String password){
        pbar(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(Create_account.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pbar(false);
                if(task.isSuccessful()){
                    Toast.makeText(Create_account.this, "Account Created", Toast.LENGTH_SHORT).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                    firebaseAuth.signOut();
                    startActivity(new Intent(Create_account.this, log_in.class));
                    finish();

                }
                else{
                    Toast.makeText(Create_account.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });


        }
        void pbar(boolean inprogress){
        if(inprogress){
            progressBar.setVisibility(View.VISIBLE);
            create.setVisibility(View.GONE);
        }else
        {
            progressBar.setVisibility(View.GONE);
            create.setVisibility(View.VISIBLE);
        }
        }

        boolean valid(String email,String password){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mail.setError("Invalid Email");
            return false;

        }
        if(password.length()<6){
            pass.setError("Password is Invalid");
            return false;
        }
        return true;
        }

    }










