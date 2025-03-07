package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserLogin extends AppCompatActivity {
    EditText edTxtlog_email, edTxtLog_password;
    Button btnLog;
    ProgressBar progressBar;
    TextView txtLog_forgot_password, txtLog_sign_up;
    ImageView imgPfp;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

        edTxtlog_email = findViewById(R.id.edTxtlog_email);
        edTxtLog_password = findViewById(R.id.edTxtlog_password);
        btnLog = findViewById(R.id.btnLog);
        progressBar = findViewById(R.id.progressBar);
        txtLog_forgot_password = findViewById(R.id.txtLog_forgot_password);
        txtLog_sign_up = findViewById(R.id.txtLog_sign_up);
        imgPfp = findViewById(R.id.imgPfp);
        mAuth = FirebaseAuth.getInstance();

        btnLog.setOnClickListener(view -> {
            String email = edTxtlog_email.getText().toString().trim();
            String password = edTxtLog_password.getText().toString().trim();

            if (email.isEmpty()) {
                edTxtlog_email.setError("Email is empty");
                edTxtlog_email.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edTxtlog_email.setError("Enter a valid email address");
                edTxtlog_email.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                edTxtLog_password.setError("Enter the password");
                edTxtLog_password.requestFocus();
                return;
            }
            if (password.length() < 6) {
                edTxtLog_password.setError("Length of the password should be more than 6");
                edTxtLog_password.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            retrieveUserId(user.getUid());
                        }
                    } else {
                        Toast.makeText(UserLogin.this, "Login failed. Please check your credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        txtLog_forgot_password.setOnClickListener(view -> startActivity(new Intent(UserLogin.this, ChangePasswordInterface.class)));

        txtLog_sign_up.setOnClickListener(view -> startActivity(new Intent(UserLogin.this, UserRegister.class)));
    }

    private void retrieveUserId(String uid) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://recommender-system-53372-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users").child(uid);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int userId = snapshot.child("userId").getValue(Integer.class);
                    Intent intent = new Intent(UserLogin.this, MovieChoice.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UserLogin.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserLogin.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
