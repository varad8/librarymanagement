package com.vrnitsolution.librarymanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {
    EditText email, password;
    FirebaseAuth mauth;
    CustomProgressDialog loadingdailog;
    AppCompatButton loginbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginbtn=findViewById(R.id.loginbtn);

        mauth = FirebaseAuth.getInstance();

        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Checking Account");
        loadingdailog.setCancelable(false);
    }

    public void loginUser(View view) {
        String useremail, userpassword;

        useremail = email.getText().toString().trim();
        userpassword = password.getText().toString().trim();

        if (useremail.isEmpty()) {
            displaySnackbar("Email cannot be empty");
        } else if (userpassword.isEmpty()) {
            displaySnackbar("Password can't be empty");
        } else {
            checkAdminDocument(useremail, userpassword);
            loadingdailog.show();
        }
    }

    private void checkAdminDocument(String useremail, String userpassword) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Admin");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                        String accountType = adminSnapshot.child("accountType").getValue(String.class);
                        String adminemail = adminSnapshot.child("email").getValue(String.class);

                        if (accountType != null && adminemail != null) {
                            if (accountType.equals("admin") && useremail.equals(adminemail)) {
                                loadingdailog.setMessage("Authorized Account");
                                checkloginCredentials(adminemail, userpassword);
                                return;
                            }
                        }
                    }
                    loadingdailog.dismiss();
                    displaySnackbar("Unauthorized admin login");
                } else {
                    loadingdailog.dismiss();
                    displaySnackbar("Unauthorized admin login");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingdailog.dismiss();
                displaySnackbar(databaseError.getMessage().toString());
            }
        });
    }

    private void checkloginCredentials(String useremail, String userpassword) {
        mauth.signInWithEmailAndPassword(useremail, userpassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                loginbtn.setClickable(false);
                loadingdailog.dismiss();
                displaySnackbar("Login Successful");
                startActivity(new Intent(AdminLoginActivity.this, MainActivity.class));
                finish();
                finishAffinity();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displaySnackbar(e.getMessage().toString());
                loadingdailog.dismiss();
            }
        });
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }
}
