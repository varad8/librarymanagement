package com.vrnitsolution.librarymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText email, password, username;
    FirebaseAuth mauth;
    CustomProgressDialog registerdialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);

        mauth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        registerdialog = new CustomProgressDialog(this);
        registerdialog.setCancelable(false);
        registerdialog.setMessage("loading..");
    }


    public void goToLoginActivity(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

    public void registerUser(View view) {
        String useremail, userpassword, userfullname;

        useremail = email.getText().toString().trim();
        userpassword = password.getText().toString().trim();
        userfullname = username.getText().toString().trim();

        if (useremail.isEmpty()) {
            displaySnackbar("Email cannot be empty");
        } else if (userpassword.isEmpty()) {
            displaySnackbar("Password can't be empty");
        } else if (userpassword.length() <= 5) {
            displaySnackbar("Password must be 6 character long");
        } else if (userfullname.isEmpty()) {
            displaySnackbar("Username can't be empty");
        } else {
            savedUserInAuth(useremail, userpassword, userfullname);
            registerdialog.setMessage("Registering the user");
            registerdialog.show();
        }
    }

    private void savedUserInAuth(String useremail, String userpassword, String userfullname) {
        mauth.createUserWithEmailAndPassword(useremail, userpassword).addOnSuccessListener(authResult -> {
            FirebaseUser user = mauth.getCurrentUser();
            if (user != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(userfullname)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendVerificationEmail();
                        saveUserDataInFirebase(userfullname,useremail,user);

                    } else {
                        displaySnackbar("Failed to update display name: " + task.getException().getMessage());
                        registerdialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            displaySnackbar("" + e.getMessage());
            registerdialog.dismiss();
        });
    }

    private void saveUserDataInFirebase(String userfullname, String useremail, FirebaseUser user) {
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("username", userfullname);
        userdata.put("useremail", useremail);
        userdata.put("accountType","user");

        databaseReference.child(user.getUid()).setValue(userdata).addOnSuccessListener(unused -> {
            registerdialog.dismiss();
            displaySnackbar("User Registration Successfull");
            openActivity();
        }).addOnFailureListener(e -> {
            registerdialog.dismiss();
            displaySnackbar(""+e.getMessage());
        });
    }

    private void openActivity() {
        FirebaseUser firebaseUser=mauth.getCurrentUser();
        if (firebaseUser!=null)
        {
            startActivity(new Intent(RegisterActivity.this, UserDashboard.class));
            finish();
            finishAffinity();
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mauth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    displaySnackbar("Verification email sent");
                } else {
                    displaySnackbar("Failed to send verification email: " + task.getException().getMessage());
                }
            });
        }
    }








}