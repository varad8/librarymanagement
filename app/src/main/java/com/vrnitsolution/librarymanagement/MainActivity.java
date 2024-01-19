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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.vrnitsolution.librarymanagement.model.IssueModel;
import com.vrnitsolution.librarymanagement.model.NewBookModel;
import com.vrnitsolution.librarymanagement.model.User;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mauth;
    FirebaseUser user;
    private DrawerLayout drawerLayout;
    CircleImageView profile_image;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    CustomProgressDialog loadingdailog;
    AlertDialog dialog;
    NavigationView navigationView;
    CircleImageView profilepic;
    String token;
    AlertDialog bookdialog, updatedialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            checkAdminDocument(user);
            getProfile();
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finishAffinity();
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Handle Home click
                } else if (id == R.id.profile) {
                    openViewProfileDialog(user.getUid());
                } else if (id == R.id.nav_logout) {
                    mauth.signOut();
                    startActivity(new Intent(MainActivity.this, MainScreen.class));
                    finish();
                    finishAffinity();
                }


                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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


    private void checkAdminDocument(FirebaseUser user) {
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String accountType = dataSnapshot.child("accountType").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    if (dataSnapshot.getChildrenCount() == 2 && dataSnapshot.hasChild("accountType") && accountType.equals("admin") && email.equals(user.getEmail())) {
                        loadingdailog.dismiss();
                        openProfileInfoDialog();
                    } else {
                        updateOnlyFCMToken();

                    }
                } else {
                    loadingdailog.dismiss();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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

    private void updateOnlyFCMToken() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Admin").child(user.getUid());
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


    private void openProfileInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_adminprofile_dialog, null);
        builder.setView(dialogView);

        EditText fullNameEditText = dialogView.findViewById(R.id.textUsername);
        profilepic = dialogView.findViewById(R.id.profile_image);
        EditText textMobileNo = dialogView.findViewById(R.id.textMobileNo);
        EditText textAddress = dialogView.findViewById(R.id.textAddress);
        EditText textDesignation = dialogView.findViewById(R.id.textDesignation);
        EditText textStaffId = dialogView.findViewById(R.id.textStaffId);

        AppCompatButton saveButton = dialogView.findViewById(R.id.saveButton);

        profilepic.setOnClickListener(view -> openGallery());
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname = fullNameEditText.getText().toString().trim();
                String mobileno = textMobileNo.getText().toString().trim();
                String address = textAddress.getText().toString().trim();
                String designation = textDesignation.getText().toString().trim();
                String staffid = textStaffId.getText().toString().trim();

                if (fullname.isEmpty()) {
                    displaySnackbar("full name cant't be blank");
                } else if (mobileno.isEmpty()) {
                    displaySnackbar("phone no can't be blank");
                } else if (mobileno.length() != 10) {
                    displaySnackbar("Phone number must be 10 digits");
                } else if (address.isEmpty()) {
                    displaySnackbar("address can't be blank");
                } else if (designation.isEmpty()) {
                    displaySnackbar("designation can't be blank");
                } else if (staffid.isEmpty()) {
                    displaySnackbar("staff id can't be blank");
                } else if (selectedImageUri == null) {
                    displaySnackbar("please select profile image");
                } else {
                    loadingdailog.setMessage("Wait While Uploading data");
                    loadingdailog.show();
                    saveToFirebase(fullname, mobileno, address, designation, staffid, selectedImageUri, user.getEmail());
                }
            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


    }

    private void saveToFirebase(String fullname, String mobileno, String address, String designation, String staffid, Uri selectedImageUri, String email) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profileAdmin/" + UUID.randomUUID().toString());

        storageRef.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileData(fullname, mobileno, address, designation, staffid, email, uri.toString());
            }).addOnFailureListener(e -> {
                displaySnackbar("Failed to get image download URL.");
                loadingdailog.dismiss();
            });
        }).addOnFailureListener(e -> {
            displaySnackbar("Failed to upload profile image.");
            loadingdailog.dismiss();
        });
    }

    private void updateProfileData(String fullname, String mobileno, String address, String designation, String staffid, String email, String imageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Admin").child(user.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Admin admin = dataSnapshot.getValue(Admin.class);

                    admin.setFullname(fullname);
                    admin.setMobileno(mobileno);
                    admin.setAddress(address);
                    admin.setDesignation(designation);
                    admin.setStaffid(staffid);
                    admin.setProfileImageUrl(imageUrl);
                    admin.setFcmToken(token);

                    databaseReference.setValue(admin).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            updateFirebaseAuthUserProfile(fullname, imageUrl);
                        } else {
                            loadingdailog.dismiss();
                            displaySnackbar("Failed to update profile information.");
                        }
                    });
                } else {
                    loadingdailog.dismiss();
                    displaySnackbar("Admin data not found.");
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

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
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

    public void addNewBook(View view) {
        View dialogView = getLayoutInflater().inflate(R.layout.add_book_custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        EditText bookNameEditText = dialogView.findViewById(R.id.bookNaame);
        EditText isbnEditText = dialogView.findViewById(R.id.bookISBNNO);
        EditText quantityEditText = dialogView.findViewById(R.id.bookQuantity);
        EditText rackNoEditText = dialogView.findViewById(R.id.bookRackNo);
        EditText categoryEditText = dialogView.findViewById(R.id.bookCategory);
        AppCompatButton saveButton = dialogView.findViewById(R.id.savedBooks);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String bookName = bookNameEditText.getText().toString();
                String isbn = isbnEditText.getText().toString();
                String quantity = quantityEditText.getText().toString();
                String rackNo = rackNoEditText.getText().toString();
                String category = categoryEditText.getText().toString();

                if (bookName.isEmpty()) {
                    displaySnackbar("Enter book name");
                } else if (isbn.isEmpty()) {
                    displaySnackbar("Enter isbn no.");
                } else if (quantity.isEmpty()) {
                    displaySnackbar("book quantity");
                } else if (rackNo.isEmpty()) {
                    displaySnackbar("Enter book rack no. ex(R7:B2)");
                } else if (category.isEmpty()) {
                    displaySnackbar("Enter book category");
                } else {
                    NewBookModel bookModel = new NewBookModel(bookName, category, isbn, quantity, rackNo, "");
                    loadingdailog.setMessage("Saving Data..please wait");
                    loadingdailog.show();
                    saveDataToFirebase(bookModel);
                }

            }


        });
        bookdialog = builder.create();
        bookdialog.show();
    }

    private void saveDataToFirebase(NewBookModel bookModel) {
        DatabaseReference booksRef = FirebaseDatabase.getInstance().getReference().child("Books");
        String bookKey = booksRef.push().getKey();
        bookModel.setBookKey(bookKey);

        booksRef.child(bookKey).setValue(bookModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            bookdialog.dismiss();
                            loadingdailog.dismiss();
                            displaySnackbar("Book added successfully");
                        } else {
                            // Failed to save data
                            displaySnackbar("Failed to add book");
                        }
                    }
                });
    }

    public void allBooks(View view) {
        startActivity(new Intent(this, AllBooksActivity.class));
    }

    public void allIssuedBooks(View view) {
        startActivity(new Intent(MainActivity.this, AdminIssuedBooks.class));
    }


    public void returnBooks(View view) {
        startActivity(new Intent(MainActivity.this, AllReturnedBookAdmin.class));
    }

    private void openViewProfileDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.admin_profile_dialog, null);
        builder.setView(view);

        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView designationTextView = view.findViewById(R.id.designationTextView);
        TextView addressTextView = view.findViewById(R.id.addressTextView);
        TextView mobileNoTextView = view.findViewById(R.id.mobileNoTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);
        TextView staffIdTextView = view.findViewById(R.id.staffIdTextView);

        DatabaseReference adminReference = FirebaseDatabase.getInstance().getReference().child("Admin").child(userId);
        adminReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                if (admin != null) {
                    Glide.with(MainActivity.this).load(admin.getProfileImageUrl()).into(profileImage);
                    usernameTextView.setText("Username: " + (admin.getFullname() != null ? admin.getFullname() : ""));
                    designationTextView.setText("Designation: " + (admin.getDesignation() != null ? admin.getDesignation() : ""));
                    addressTextView.setText("Address: " + (admin.getAddress() != null ? admin.getAddress() : ""));
                    mobileNoTextView.setText("Mobile No: " + (admin.getMobileno() != null ? admin.getMobileno() : ""));
                    emailTextView.setText("Email: " + (admin.getEmail() != null ? admin.getEmail() : ""));
                    staffIdTextView.setText("Staff ID: " + (admin.getStaffid() != null ? admin.getStaffid() : ""));
                }
            }
        }).addOnFailureListener(e -> displaySnackbar(e.getMessage()));

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });


        builder.setPositiveButton("Update Details", (dialog, which) -> {
            openUpdateProfileDetails(userId);
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openUpdateProfileDetails(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.update_profile_dialog, null); // Replace with the layout for updating profile details
        builder.setView(view);

        EditText fullnameEditText = view.findViewById(R.id.fullnameEditText);
        EditText designationEditText = view.findViewById(R.id.designationEditText);
        EditText addressEditText = view.findViewById(R.id.addressEditText);
        EditText mobileNoEditText = view.findViewById(R.id.mobileNoEditText);
        EditText emailEditText = view.findViewById(R.id.emailEditText);
        emailEditText.setEnabled(false);
        emailEditText.setFocusable(false);
        emailEditText.setClickable(false);
        EditText staffIdEditText = view.findViewById(R.id.staffIdEditText);




        AppCompatButton updateButton = view.findViewById(R.id.updateButton);


        DatabaseReference adminReference = FirebaseDatabase.getInstance().getReference().child("Admin").child(userId);
        adminReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                if (admin != null) {

                    fullnameEditText.setText(admin.getFullname());
                    designationEditText.setText(admin.getDesignation());
                    addressEditText.setText(admin.getAddress());
                    mobileNoEditText.setText(admin.getMobileno());
                    emailEditText.setText(admin.getEmail());
                    staffIdEditText.setText(admin.getStaffid());
                }
            }
        }).addOnFailureListener(e -> displaySnackbar(e.getMessage()));

        updateButton.setOnClickListener(v -> {

            String updatedFullname = fullnameEditText.getText().toString().trim();
            String updatedDesignation = designationEditText.getText().toString().trim();
            String updatedAddress = addressEditText.getText().toString().trim();
            String updatedMobileNo = mobileNoEditText.getText().toString().trim();
            String updatedEmail = emailEditText.getText().toString().trim();
            String updatedStaffId = staffIdEditText.getText().toString().trim();


            if (updatedFullname.isEmpty()) {
                displaySnackbar("Name can't be blank");
            } else if (updatedDesignation.isEmpty()) {
                displaySnackbar("Designation can't be blank");
            } else if (updatedAddress.isEmpty()) {
                displaySnackbar("Address can't be blank");
            } else if (updatedMobileNo.isEmpty()) {
                displaySnackbar("mobile no can't be blank");
            } else if (updatedMobileNo.length() != 10) {
                displaySnackbar("mobile no must be 10 digit");
            } else if (updatedStaffId.isEmpty()) {
                displaySnackbar("staff id can't be blank");
            } else {

                DatabaseReference updatedAdminReference = FirebaseDatabase.getInstance().getReference().child("Admin").child(userId);
                updatedAdminReference.child("fullname").setValue(updatedFullname);
                updatedAdminReference.child("designation").setValue(updatedDesignation);
                updatedAdminReference.child("address").setValue(updatedAddress);
                updatedAdminReference.child("mobileno").setValue(updatedMobileNo);
                updatedAdminReference.child("email").setValue(updatedEmail);
                updatedAdminReference.child("staffid").setValue(updatedStaffId);


                loadingdailog.setMessage("Updating Details..");
                loadingdailog.show();
                updatefirebaseAuthName(updatedFullname);
                updatedialog.dismiss();
            }


        });


        builder.setNegativeButton("Cancel", (d, which) -> {
            d.dismiss();
        });

        updatedialog = builder.create();
        updatedialog.show();
    }


    public void collectFine(View view) {
        startActivity(new Intent(this, CollectFineActivity.class));
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
}