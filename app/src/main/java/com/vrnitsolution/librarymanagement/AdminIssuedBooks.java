package com.vrnitsolution.librarymanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
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

public class AdminIssuedBooks extends AppCompatActivity implements IssuesAdapter.OnItemClickListener {
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
        setContentView(R.layout.activity_admin_issued_books);


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




        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<IssueModel> issuesList = (ArrayList<IssueModel>) intent.getSerializableExtra("issuesList");
            if (issuesList != null) {
                issuesAdapter.setIssuesList(issuesList);
            }else{
                fetchDataFromFirebase();
            }
        }else {
            fetchDataFromFirebase();
        }

        search.setOnClickListener(view -> {
            String librarycardid = searchEditText.getText().toString().trim();

            if (librarycardid.isEmpty()) {
                displaySnackbar("Please Enter library card id");
            } else {
                searchFetchData(librarycardid);
            }
        });

    }

    private void searchFetchData(String librarycardid) {
        loadingdailog.show();
        Query searchQuery = databaseReference.orderByChild("librarycardid").equalTo(librarycardid);
        searchQuery.get().addOnCompleteListener(task -> {
            loadingdailog.dismiss();
            if (task.isSuccessful()) {
                ArrayList<IssueModel> searchResults = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    IssueModel issue = snapshot.getValue(IssueModel.class);

                    if (issue != null && !issue.isBookReturnStatus()) {
                        searchResults.add(issue);
                    }
                }

                issuesAdapter.setIssuesList(searchResults);
                if (searchResults.isEmpty()) {
                    displaySnackbar("No records found for the given library card ID.");
                }
            } else {
                Log.d("INDEXERROR", task.getException().getMessage());
                displaySnackbar("Failed to fetch data. Please try again.");
            }
        });
    }


    private void fetchDataFromFirebase() {
        loadingdailog.show();
        databaseReference.orderByChild("bookReturnStatus").equalTo(false).get().addOnCompleteListener(task -> {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action");

        String[] actions = {"View Profile", "Update"};

        builder.setItems(actions, (dialog, which) -> {
            switch (which) {
                case 0:
                    openViewProfileDialog(issue.getUserId());
                    break;
                case 1:
                    updateBookIssuedStatusDialog(issue);
                    break;
            }
            dialog.dismiss();
        });

        builder.show();
    }

    private void updateBookIssuedStatusDialog(IssueModel issue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Book Return");
        builder.setMessage("Are you sure you want to confirm the return of this book?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateBookIssuedStatus(issue);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displaySnackbar("Cancel Updatation");
            }
        });

        builder.show();
    }

    private void updateBookIssuedStatus(IssueModel issue) {
        DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference().child("IssuesdBooks").child(issue.getBookid());

        issueRef.child("bookStatus").setValue("Returned Book");
        issueRef.child("bookReturnStatus").setValue(true);


        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Update the bookReturnDate with the current date and time
        issueRef.child("bookReturnDate").setValue(currentDateAndTime);

        issueRef.updateChildren(new HashMap<String, Object>())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        displaySnackbar("Book status updated to Returned Book.");
                        onBackPressed();
                    } else {
                        displaySnackbar("Failed to update book status. Please try again.");
                    }
                });
    }


    private void openViewProfileDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AdminIssuedBooks.this);
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
                    Glide.with(AdminIssuedBooks.this).load(user.getProfileImageUrl()).into(profileImage);
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