package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserRegister extends AppCompatActivity {

    EditText edTxtreg_confirm_password, edTxtreg_email, edTxtreg_password, edTxtreg_username, edTxtreg_phone_number;
    TextView txtReg;
    Button btnReg;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference nextUserIdRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Widget initialization
        edTxtreg_confirm_password = findViewById(R.id.edTxtreg_confirm_password);
        edTxtreg_email = findViewById(R.id.edTxtreg_email);
        edTxtreg_password = findViewById(R.id.edTxtreg_password);
        txtReg = findViewById(R.id.txtReg);
        edTxtreg_username = findViewById(R.id.edTxtreg_username);
        edTxtreg_phone_number = findViewById(R.id.edTxtreg_phone_number);
        btnReg = findViewById(R.id.btnReg);

        // Firebase initialization
        firebaseDatabase = FirebaseDatabase.getInstance("https://recommender-system-53372-default-rtdb.asia-southeast1.firebasedatabase.app/");
        nextUserIdRef = firebaseDatabase.getReference("nextUserId");

        mAuth = FirebaseAuth.getInstance();

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = edTxtreg_username.getText().toString().trim();
        String email = edTxtreg_email.getText().toString().trim();
        String password = edTxtreg_password.getText().toString().trim();
        String confirmpassword = edTxtreg_confirm_password.getText().toString().trim();
        String phonenumber = edTxtreg_phone_number.getText().toString().trim();

        if (username.isEmpty()) {
            edTxtreg_username.setError("Username is empty");
            edTxtreg_username.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            edTxtreg_password.setError("Enter the password");
            edTxtreg_password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            edTxtreg_password.setError("Length of the password should be more than 6");
            edTxtreg_password.requestFocus();
            return;
        }
        if (!confirmpassword.matches(password)) {
            edTxtreg_confirm_password.setError("Repeat the password");
            edTxtreg_confirm_password.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            edTxtreg_email.setError("Email is empty");
            edTxtreg_email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edTxtreg_email.setError("Enter a valid email address");
            edTxtreg_email.requestFocus();
            return;
        }
        if (phonenumber.isEmpty()) {
            edTxtreg_phone_number.setError("Phone number is empty");
            edTxtreg_phone_number.requestFocus();
            return;
        }
        if (!Patterns.PHONE.matcher(phonenumber).matches()) {
            edTxtreg_phone_number.setError("Enter a valid phone number");
            edTxtreg_phone_number.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    addUserDetailsToDatabase(Objects.requireNonNull(task.getResult().getUser()));
                    Toast.makeText(UserRegister.this, "You are successfully registered", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserRegister.this, "Registration failed. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addUserDetailsToDatabase(FirebaseUser user) {
        nextUserIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Debug: Print if the snapshot exists
                System.out.println("Snapshot exists: " + snapshot.exists());

                if (snapshot.exists()) {
                    Integer nextUserId = snapshot.getValue(Integer.class);
                    // Debug: Print the retrieved nextUserId
                    System.out.println("Retrieved nextUserId: " + nextUserId);

                    if (nextUserId != null) {
                        DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("Users").child(user.getUid());

                        Users userDetails = new Users();
                        userDetails.setName(edTxtreg_username.getText().toString());
                        userDetails.setMobile(edTxtreg_phone_number.getText().toString());
                        userDetails.setEmail(edTxtreg_email.getText().toString());
                        userDetails.setType("Customer");
                        userDetails.setUserId(nextUserId);

                        databaseReferenceUsers.setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Increment nextUserId by 1 and save it back to the database
                                    nextUserIdRef.setValue(nextUserId + 1);
                                    startActivity(new Intent(UserRegister.this, UserLogin.class));
                                    finish();
                                    System.out.println("User data saved successfully.");
                                } else {
                                    System.out.println("Failed to save user data. Something went wrong!");
                                }
                            }
                        });
                    } else {
                        System.out.println("nextUserId is null");
                        Toast.makeText(UserRegister.this, "Failed to retrieve next user ID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    System.out.println("nextUserId node does not exist");
                    Toast.makeText(UserRegister.this, "nextUserId node does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Database error: " + error.getMessage());
                Toast.makeText(UserRegister.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @IgnoreExtraProperties
    public static class Users {
        private String name;
        private String mobile;
        private String email;
        private String type;
        private int userId;

        public Users() {
            // Default constructor required for calls to DataSnapshot.getValue(Users.class)
        }

        @PropertyName("name")
        public String getName() {
            return name;
        }

        @PropertyName("name")
        public void setName(String name) {
            this.name = name;
        }

        @PropertyName("mobile")
        public String getMobile() {
            return mobile;
        }

        @PropertyName("mobile")
        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        @PropertyName("email")
        public String getEmail() {
            return email;
        }

        @PropertyName("email")
        public void setEmail(String email) {
            this.email = email;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getUserId() {
            return userId;
        }
    }
}
