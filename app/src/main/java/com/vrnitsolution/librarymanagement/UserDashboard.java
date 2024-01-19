package com.vrnitsolution.librarymanagement;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vrnitsolution.librarymanagement.model.Admin;
import com.vrnitsolution.librarymanagement.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDashboard extends AppCompatActivity {
    FirebaseAuth mauth;
    FirebaseUser user;
    private DrawerLayout drawerLayout;
    CircleImageView profile_image;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    CustomProgressDialog loadingdailog;
    AlertDialog dialog, updatedialog;
    NavigationView navigationView;
    CircleImageView profilepic;
    String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        profile_image = findViewById(R.id.profile_image);
        profilepic = new CircleImageView(this);
        mauth = FirebaseAuth.getInstance();
        user = mauth.getCurrentUser();


        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Loading...");
        loadingdailog.setCancelable(false);
        loadingdailog.show();


        if (user != null) {
            checkUserDocument(user);
            getProfile();
        } else {
            startActivity(new Intent(UserDashboard.this, LoginActivity.class));
            finishAffinity();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Handle Home click
                } else if (id == R.id.profile) {
                    openUserProfileDialog(user.getUid());
                } else if (id == R.id.nav_logout) {
                    mauth.signOut();
                    startActivity(new Intent(UserDashboard.this, MainScreen.class));
                    finish();
                    finishAffinity();
                }


                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    private void openUserProfileDialog(String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboard.this);
        View view = getLayoutInflater().inflate(R.layout.user_information_dialog, null);
        builder.setView(view);

        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView rollNoTextView = view.findViewById(R.id.rollNoTextView);
        TextView addressTextView = view.findViewById(R.id.addressTextView);
        TextView mobileNoTextView = view.findViewById(R.id.mobileNoTextView);
        TextView libraryCardIdTextView = view.findViewById(R.id.libraryCardIdTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);

        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        usersReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Glide.with(UserDashboard.this).load(user.getProfileImageUrl()).into(profileImage);
                    usernameTextView.setText("Username: " + (user.getUsername() != null ? user.getUsername() : ""));
                    rollNoTextView.setText("Roll No: " + (user.getRollNo() != null ? user.getRollNo() : ""));
                    addressTextView.setText("Address: " + (user.getAddress() != null ? user.getAddress() : ""));
                    mobileNoTextView.setText("Mobile No: " + (user.getMobileno() != null ? user.getMobileno() : ""));
                    libraryCardIdTextView.setText("Library Card ID: " + (user.getLibrarycardid() != null ? user.getLibrarycardid() : ""));
                    emailTextView.setText("Email: " + (user.getUseremail() != null ? user.getUseremail() : ""));
                }
            }
        }).addOnFailureListener(e -> displaySnackbar(e.getMessage()));

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setPositiveButton("Update Details", (d, which) -> {
            updateDetailsDialog(uid);
            d.dismiss();
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDetailsDialog(String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserDashboard.this);
        View view = getLayoutInflater().inflate(R.layout.update_user_details_dialog, null);
        builder.setView(view);


        EditText updatedUsernameEditText = view.findViewById(R.id.updatedUsernameEditText);
        EditText updatedRollNoEditText = view.findViewById(R.id.updatedRollNoEditText);
        EditText updatedAddressEditText = view.findViewById(R.id.updatedAddressEditText);
        EditText updatedMobileNoEditText = view.findViewById(R.id.updatedMobileNoEditText);
        EditText updatedLibraryCardIdEditText = view.findViewById(R.id.updatedLibraryCardIdEditText);
        updatedLibraryCardIdEditText.setEnabled(false);
        updatedLibraryCardIdEditText.setFocusable(false);
        updatedLibraryCardIdEditText.setClickable(false);
        EditText updatedEmailEditText = view.findViewById(R.id.updatedEmailEditText);
        updatedEmailEditText.setEnabled(false);
        updatedEmailEditText.setFocusable(false);
        updatedEmailEditText.setClickable(false);


        AppCompatButton saveDetailsButton = view.findViewById(R.id.saveDetailsButton);

        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        usersReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    updatedUsernameEditText.setText(user.getUsername());
                    updatedRollNoEditText.setText(user.getRollNo());
                    updatedAddressEditText.setText(user.getAddress());
                    updatedMobileNoEditText.setText(user.getMobileno());
                    updatedLibraryCardIdEditText.setText(user.getLibrarycardid());
                    updatedEmailEditText.setText(user.getUseremail());
                }
            }
        }).addOnFailureListener(e -> displaySnackbar(e.getMessage()));


        saveDetailsButton.setOnClickListener(v ->


        {
            String updatedUsername = updatedUsernameEditText.getText().toString().trim();
            String updatedRollNo = updatedRollNoEditText.getText().toString().trim();
            String updatedAddress = updatedAddressEditText.getText().toString().trim();
            String updatedMobileNo = updatedMobileNoEditText.getText().toString().trim();
            String updatedLibraryCardId = updatedLibraryCardIdEditText.getText().toString().trim();
            String updatedEmail = updatedEmailEditText.getText().toString().trim();

            if (updatedUsername.isEmpty()) {
                displaySnackbar("username can't be blank");
            } else if (updatedRollNo.isEmpty()) {
                displaySnackbar("roll no can't be empty");
            } else if (updatedAddress.isEmpty()) {
                displaySnackbar("address can't be empty");
            } else if (updatedMobileNo.isEmpty()) {
                displaySnackbar("mobile no can't be empty");
            } else if (updatedMobileNo.length() != 10) {
                displaySnackbar("mobile no must be 10 digit");
            } else if (updatedLibraryCardId.isEmpty()) {
                displaySnackbar("librarycard id cant'be blank");
            } else if (updatedEmail.isEmpty()) {
                displaySnackbar("email can't be blank");
            } else {
                DatabaseReference updatedUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                updatedUserReference.child("username").setValue(updatedUsername);
                updatedUserReference.child("rollNo").setValue(updatedRollNo);
                updatedUserReference.child("address").setValue(updatedAddress);
                updatedUserReference.child("mobileno").setValue(updatedMobileNo);
                updatedUserReference.child("librarycardid").setValue(updatedLibraryCardId);
                updatedUserReference.child("useremail").setValue(updatedEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        displaySnackbar("Details updated successfully");
                        loadingdailog.setMessage("Updating Details..");
                        loadingdailog.show();
                        updatefirebaseAuthName(updatedUsername);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        displaySnackbar("" + e.getMessage());
                    }
                });


                updatedialog.dismiss();

            }
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        updatedialog = builder.create();
        updatedialog.show();
    }

    private void updatefirebaseAuthName(String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        if (user != null) {
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadingdailog.dismiss();
                    displaySnackbar("Profile information updated successfully.");
                    getProfile();
                } else {
                    loadingdailog.dismiss();
                    displaySnackbar("Failed to update.");
                }
            });
        }
    }


    private void checkUserDocument(FirebaseUser user) {
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountType = dataSnapshot.child("accountType").getValue(String.class);
                    String email = dataSnapshot.child("useremail").getValue(String.class);
                    if (dataSnapshot.getChildrenCount() == 3 && dataSnapshot.hasChild("accountType") && accountType.equals("user") && email.equals(user.getEmail())) {
                        loadingdailog.dismiss();
                        openProfileInfoDialog();
                    } else {
                        updateOnlyFCMToken();

                    }
                } else {
                    loadingdailog.dismiss();
                    startActivity(new Intent(UserDashboard.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingdailog.dismiss();
                displaySnackbar(databaseError.getMessage().toString());
            }
        });
    }

    private void getProfile() {
        FirebaseUser firebaseUser = mauth.getCurrentUser();
        if (firebaseUser != null) {


            View headerView = navigationView.getHeaderView(0);
            CircleImageView navigationProfileImage = headerView.findViewById(R.id.navigation_profile_image);
            TextView navigationUsername = headerView.findViewById(R.id.navigation_username);
            TextView navigationEmail = headerView.findViewById(R.id.navigation_email);

            if (firebaseUser.getPhotoUrl() != null && firebaseUser.getDisplayName() != null) {
                loadingdailog.dismiss();
                Glide.with(this).load(firebaseUser.getPhotoUrl()).into(navigationProfileImage);
                Glide.with(this).load(firebaseUser.getPhotoUrl()).into(profile_image);
                navigationUsername.setText(firebaseUser.getDisplayName());
                navigationEmail.setText(firebaseUser.getEmail());
            }
        }
    }

    private void openProfileInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_userprofile_dialog, null);
        builder.setView(dialogView);

        EditText usernameEditText = dialogView.findViewById(R.id.editTextUsername);
        EditText rollNoEditText = dialogView.findViewById(R.id.editTextRollNo);
        EditText libraryCardIdEditText = dialogView.findViewById(R.id.editTextLibraryCardId);
        EditText addressEditText = dialogView.findViewById(R.id.editTextAddress);
        EditText mobileNoEditText = dialogView.findViewById(R.id.editTextMobileNo);
        libraryCardIdEditText.setText(generateLibraryCardId());
        libraryCardIdEditText.setFocusable(false);
        libraryCardIdEditText.setClickable(false);

        AppCompatButton saveButton = dialogView.findViewById(R.id.saveButton);

        profilepic = dialogView.findViewById(R.id.profile_image);
        profilepic.setOnClickListener(view -> openGallery());

        saveButton.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String rollNo = rollNoEditText.getText().toString().trim();
            String libraryCardId = libraryCardIdEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String mobileNo = mobileNoEditText.getText().toString().trim();

            if (username.isEmpty()) {
                displaySnackbar("Username cannot be blank");
            } else if (rollNo.isEmpty()) {
                displaySnackbar("Roll Number cannot be blank");
            } else if (libraryCardId.isEmpty()) {
                displaySnackbar("Library Card ID cannot be blank");
            } else if (address.isEmpty()) {
                displaySnackbar("Address cannot be blank");
            } else if (mobileNo.isEmpty()) {
                displaySnackbar("Mobile Number cannot be blank");
            } else if (mobileNo.length() != 10) {
                displaySnackbar("Phone number must be 10 digits");
            } else if (selectedImageUri == null) {
                displaySnackbar("Please select a profile image");
            } else {
                loadingdailog.setMessage("Wait While Uploading data");
                loadingdailog.show();

                saveToFirebase(username, rollNo, libraryCardId, address, mobileNo, selectedImageUri, user.getEmail());
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void saveToFirebase(String username, String rollNo, String libraryCardId, String address, String mobileNo, Uri selectedImageUri, String email) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profileUser/" + UUID.randomUUID().toString());

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileData(username, rollNo, libraryCardId, address, mobileNo, uri.toString(), email);
            }).addOnFailureListener(e -> {
                displaySnackbar("Failed to get image download URL.");
                loadingdailog.dismiss();
            });
        }).addOnFailureListener(e -> {
            displaySnackbar("Failed to upload profile image.");
            loadingdailog.dismiss();
        });
    }

    private void updateProfileData(String username, String rollNo, String libraryCardId, String address, String mobileNo, String imageUrl, String email) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user1 = dataSnapshot.getValue(User.class);

                    user1.setUsername(username);
                    user1.setRollNo(rollNo);
                    user1.setAddress(address);
                    user1.setLibrarycardid(libraryCardId);
                    user1.setMobileno(mobileNo);
                    user1.setProfileImageUrl(imageUrl);
                    user1.setFcmToken(token);
                    user1.setUserid(user.getUid());

                    databaseReference.setValue(user1).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            updateFirebaseAuthUserProfile(username, imageUrl);
                        } else {
                            loadingdailog.dismiss();
                            displaySnackbar("Failed to update profile information.");
                        }
                    });
                } else {
                    loadingdailog.dismiss();
                    displaySnackbar("User data not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingdailog.dismiss();
                displaySnackbar(databaseError.getMessage().toString());
            }
        });
    }

    private void updateFirebaseAuthUserProfile(String displayName, String photoUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(Uri.parse(photoUrl))
                .build();

        if (user != null) {
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    loadingdailog.dismiss();
                    displaySnackbar("Profile information and image saved successfully.");
                    getProfile();
                } else {
                    loadingdailog.dismiss();
                    displaySnackbar("Failed to update user profile.");
                }
            });
        }
    }

    private void updateOnlyFCMToken() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        userRef.child("fcmToken").setValue(token).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Handle success
                displaySnackbar("FCM Token updated successfully");
            } else {
                // Handle failure
                displaySnackbar("Failed to update FCM Token");
            }
        });
    }

    public void myIssueBooks(View view) {
        startActivity(new Intent(UserDashboard.this, AllIssuedBookUser.class));
    }

    public void myReturnBook(View view) {
        startActivity(new Intent(UserDashboard.this, AllReturnedBookUser.class));
    }

    public void allBooks(View view) {
        startActivity(new Intent(UserDashboard.this, AllBooksUser.class));
    }


    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(profilepic);
        }
    }

    public void openDrawer(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        token = task.getResult();

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        getFCMToken();
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

    public static String generateLibraryCardId() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Random random = new Random();
        int randomNumber = random.nextInt(9000) + 1000;
        String libraryCardId = timeStamp + randomNumber;
        return libraryCardId;
    }

}