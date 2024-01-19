package com.vrnitsolution.librarymanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainScreen extends AppCompatActivity {
    FirebaseAuth mauth;
    FirebaseUser firebaseUser;
    CustomProgressDialog loadingdailog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        mauth=FirebaseAuth.getInstance();
        firebaseUser=mauth.getCurrentUser();



        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Loading...");
        loadingdailog.setCancelable(false);
        loadingdailog.show();
    }

    public void UserLogin(View view) {
        startActivity(new Intent(MainScreen.this,LoginActivity.class));
    }

    public void AdminLogin(View view) {
        startActivity(new Intent(MainScreen.this, AdminLoginActivity.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseUser!=null)
        {
           checkLoginCredentials(firebaseUser.getUid());
        }else {
            loadingdailog.dismiss();
        }
    }

    private void checkLoginCredentials(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    // User exists, check accountType
                    String accountType = userSnapshot.child("accountType").getValue(String.class);
                    if ("user".equals(accountType)) {
                        loadingdailog.dismiss();
                        startActivity(new Intent(MainScreen.this,UserDashboard.class));
                        finish();
                        finishAffinity();
                    }
                } else {
                    // User does not exist, check in Admin
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot adminSnapshot) {
                            if (adminSnapshot.exists()) {
                                String accountType = adminSnapshot.child("accountType").getValue(String.class);
                                if ("admin".equals(accountType)) {
                                    loadingdailog.dismiss();
                                    startActivity(new Intent(MainScreen.this,MainActivity.class));
                                    finish();
                                    finishAffinity();
                                }
                            } else {
                                // Neither User nor Admin exists with the given UID
                                // Handle accordingly
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error in reading Admin data
                            loadingdailog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error in reading User data
                loadingdailog.dismiss();
            }
        });
    }

}