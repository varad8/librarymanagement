package com.vrnitsolution.librarymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    FirebaseAuth mauth;
    CustomProgressDialog loadingdailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);


        mauth = FirebaseAuth.getInstance();

        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Loading...");
        loadingdailog.setCancelable(false);


    }

    public void gotoRegisterActivity(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    public void loginUser(View view) {
        String useremail, userpassword;

        useremail = email.getText().toString().trim();
        userpassword = password.getText().toString().trim();

        if (useremail.isEmpty()) {
            displaySnackbar("Email cannot be empty");
        } else if (userpassword.isEmpty()) {
            displaySnackbar("Password can't be empty");
        } else if (userpassword.length() <= 5) {
            displaySnackbar("Password must be 6 character long");
        } else {
            checkloginCredentials(useremail, userpassword);
            loadingdailog.show();
        }
    }

    private void checkloginCredentials(String useremail, String userpassword) {
        mauth.signInWithEmailAndPassword(useremail, userpassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                displaySnackbar("Login Successfully");
                loadingdailog.dismiss();

                openMainActivity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displaySnackbar("Login Failed");
                loadingdailog.dismiss();
            }
        });
    }

    private void openMainActivity() {
        startActivity(new Intent(LoginActivity.this, UserDashboard.class));
        finish();
        finishAffinity();
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}