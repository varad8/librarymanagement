package com.vrnitsolution.librarymanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vrnitsolution.librarymanagement.adapter.BooksAdapter;
import com.vrnitsolution.librarymanagement.adapter.IssuesAdapter;
import com.vrnitsolution.librarymanagement.model.IssueModel;
import com.vrnitsolution.librarymanagement.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AllReturnedBookAdmin extends AppCompatActivity implements IssuesAdapter.OnItemClickListener {
    private RecyclerView issuedBookRecyclerView;
    private EditText searchEditText;
    private ImageView search;
    private IssuesAdapter issuesAdapter;
    private DatabaseReference databaseReference;
    AlertDialog updateDialog;
    CustomProgressDialog loadingdailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_returned_book_admin);


        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Loading...");
        loadingdailog.setCancelable(false);

        issuedBookRecyclerView = findViewById(R.id.bookRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        search = findViewById(R.id.search);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("IssuesdBooks");


        issuesAdapter = new IssuesAdapter(new ArrayList<>(), this);
        issuedBookRecyclerView.setAdapter(issuesAdapter);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        issuedBookRecyclerView.setLayoutManager(layoutManager);


        fetchDataFromFirebase();

        search.setOnClickListener(view -> {
            String bookid = searchEditText.getText().toString().trim();

            if (bookid.isEmpty()) {
                displaySnackbar("Please book id");
            } else {
                searchFetchData(bookid);
            }
        });

    }

    private void searchFetchData(String bookid) {
        loadingdailog.show();

        // Assuming the 'bookReturnStatus' field is a boolean
        Query searchQuery = databaseReference.orderByChild("bookid").startAt(bookid).endAt(bookid + "\uf8ff");

        searchQuery.get().addOnCompleteListener(task -> {
            loadingdailog.dismiss();
            if (task.isSuccessful()) {
                ArrayList<IssueModel> searchResults = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    IssueModel issue = snapshot.getValue(IssueModel.class);

                    if (issue != null && issue.isBookReturnStatus()) {
                        searchResults.add(issue);
                    }
                }

                issuesAdapter.setIssuesList(searchResults);
                if (searchResults.isEmpty()) {
                    displaySnackbar("No records found for the given bookid.");
                }
            } else {
                Log.d("INDEXERROR", task.getException().getMessage());
                displaySnackbar("Failed to fetch data. Please try again.");
            }
        });
    }


    private void fetchDataFromFirebase() {
        loadingdailog.show();
        databaseReference.orderByChild("bookReturnStatus").equalTo(true).get().addOnCompleteListener(task -> {
            loadingdailog.dismiss();

            if (task.isSuccessful()) {

                ArrayList<IssueModel> issuesList = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    IssueModel issue = snapshot.getValue(IssueModel.class);

                    if (issue != null) {
                        issuesList.add(issue);
                    }
                }
                issuesAdapter.setIssuesList(issuesList);
                if (issuesList.isEmpty()) {
                    displaySnackbar("No records found.");
                }

            } else {
                displaySnackbar("Failed to fetch data. Please try again." + task.getException().getMessage());
                Log.d("INDEXERROR", task.getException().getMessage());
            }
        });
    }

    public void backPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onItemClick(IssueModel issue) {
        openViewProfileDialog(issue.getUserId());
    }

    private void openViewProfileDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AllReturnedBookAdmin.this);
        View view = getLayoutInflater().inflate(R.layout.user_information_dialog, null);
        builder.setView(view);

        ImageView profileImage = view.findViewById(R.id.profileImage);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView rollNoTextView = view.findViewById(R.id.rollNoTextView);
        TextView addressTextView = view.findViewById(R.id.addressTextView);
        TextView mobileNoTextView = view.findViewById(R.id.mobileNoTextView);
        TextView libraryCardIdTextView = view.findViewById(R.id.libraryCardIdTextView);
        TextView emailTextView = view.findViewById(R.id.emailTextView);

        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        usersReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Glide.with(AllReturnedBookAdmin.this).load(user.getProfileImageUrl()).into(profileImage);
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


        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }
}