package com.vrnitsolution.librarymanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.vrnitsolution.librarymanagement.adapter.IssuesAdapter;
import com.vrnitsolution.librarymanagement.model.IssueModel;

import java.util.ArrayList;

public class AllReturnedBookUser extends AppCompatActivity implements IssuesAdapter.OnItemClickListener{
    private RecyclerView issuedBookRecyclerView;
    private EditText searchEditText;
    private ImageView search;
    private IssuesAdapter issuesAdapter;
    private DatabaseReference databaseReference;
    AlertDialog updateDialog;
    CustomProgressDialog loadingdailog;
    FirebaseAuth mauth;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_returned_book_user);

        mauth=FirebaseAuth.getInstance();
        firebaseUser= mauth.getCurrentUser();


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


        search.setOnClickListener(view -> {
            String bookid = searchEditText.getText().toString().trim();

            if (bookid.isEmpty()) {
                displaySnackbar("Please book id");
            } else {
                if (firebaseUser!=null)
                {
                    searchFetchData(bookid,firebaseUser.getUid());
                }

            }
        });

        if (firebaseUser!=null)
        {
            fetchDataFromFirebase(firebaseUser.getUid());
        }

    }

    private void searchFetchData(String bookid,String uid) {
        loadingdailog.show();

        // Assuming the 'bookReturnStatus' field is a boolean
        Query searchQuery = databaseReference.orderByChild("bookid").startAt(bookid).endAt(bookid + "\uf8ff");

        searchQuery.get().addOnCompleteListener(task -> {
            loadingdailog.dismiss();
            if (task.isSuccessful()) {
                ArrayList<IssueModel> searchResults = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    IssueModel issue = snapshot.getValue(IssueModel.class);

                    if (issue != null && issue.isBookReturnStatus() && issue.getUserId().equals(uid)) {
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



    private void fetchDataFromFirebase(String uid) {
        loadingdailog.show();
        Query query = databaseReference.orderByChild("bookReturnStatus").equalTo(true);

        query.get().addOnCompleteListener(task -> {
            loadingdailog.dismiss();

            if (task.isSuccessful()) {
                ArrayList<IssueModel> issuesList = new ArrayList<>();

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    IssueModel issue = snapshot.getValue(IssueModel.class);

                    if (issue != null && issue.getUserId().equals(uid)) {
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

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(IssueModel issue) {

    }
}