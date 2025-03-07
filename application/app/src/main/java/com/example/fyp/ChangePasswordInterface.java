package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ChangePasswordInterface extends AppCompatActivity {


    //Widget
    EditText edt_Change_Email_Address;
    Button btn_Send_Email;
    ImageView imageView12;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edt_Change_Email_Address = findViewById(R.id.edt_Change_Email_Address);
        btn_Send_Email = findViewById(R.id.btn_Send_Email);
        imageView12 = findViewById(R.id.imageView12);

        btn_Send_Email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edt_Change_Email_Address.getText().toString().trim();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePasswordInterface.this, "Email has been sent. Please use the link inside the email to change your password", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ChangePasswordInterface.this, UserLogin.class));
                        } else {
                            Toast.makeText(ChangePasswordInterface.this, "Failed to send email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
