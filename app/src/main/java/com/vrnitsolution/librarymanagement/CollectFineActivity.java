package com.vrnitsolution.librarymanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vrnitsolution.librarymanagement.adapter.IssuesAdapter;
import com.vrnitsolution.librarymanagement.model.IssueModel;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollectFineActivity extends AppCompatActivity implements IssuesAdapter.OnItemClickListener {
    EditText librarycardid;
    RecyclerView finedbookdata;
    IssuesAdapter issuesAdapter;
    private static final int YOUR_FINE_RATE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_fine);

        librarycardid = findViewById(R.id.librarycardid);
        finedbookdata = findViewById(R.id.finedbookdata);

        issuesAdapter = new IssuesAdapter(new ArrayList<>(), this);
        finedbookdata.setAdapter(issuesAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        finedbookdata.setLayoutManager(layoutManager);
    }

    public void backPressed(View view) {
        onBackPressed();
    }

    public void checkFineForThatCardId(View view) {
        String cardid = librarycardid.getText().toString().trim();

        if (cardid.isEmpty()) {
            displaySnackbar("please enter library card id");
        } else {
            searchForThatUser(cardid);
        }
    }

    private void searchForThatUser(String cardId) {
        DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference("IssuesdBooks");
        issueRef.orderByChild("librarycardid").equalTo(cardId).get().addOnSuccessListener(dataSnapshot -> {
            List<IssueModel> issuesList = new ArrayList<>();
            int totalFine = 0;

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                IssueModel issue = snapshot.getValue(IssueModel.class);

                if (issue != null && !issue.isBookReturnStatus()) {
                    double fineForBook = calculateFine(issue.getExpiryIssuingDate());

                    totalFine += fineForBook;
                    issue.setFineamount((int) fineForBook);

                    // Update fine amount in the Firebase database
                    updateFineAmount(snapshot.getKey(), (int) fineForBook);
                    issuesList.add(issue);
                }
            }

            issuesAdapter.setIssuesList(issuesList);
            if (issuesList.isEmpty()) {
                displaySnackbar("No records found for the given library card id.");
            } else {
                showTotalFineDialog(totalFine, cardId, issuesList);
            }
        }).addOnFailureListener(e -> displaySnackbar("Failed to fetch data. Please try again."));
    }

    public static double calculateFine(String argumentDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            // Parse argument date
            Date dueDate = sdf.parse(argumentDate);

            // Get current date
            Date currentDate = new Date();

            // Calculate difference in days
            long differenceInMilliseconds = currentDate.getTime() - dueDate.getTime();
            long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000);

            // Check if the argument date is overdue
            if (differenceInDays > 0) {
                // Fine rate per day
                double fineRate = 10.0;

                // Calculate fine
                double fineAmount = differenceInDays * fineRate;

                return fineAmount;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Return 0 if the argument date is not overdue
        return 0;
    }

    private void showTotalFineDialog(int totalFine, String cardId, List<IssueModel> issuesList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Collect Total Fine")
                .setMessage("Collect Total Fine: " + totalFine + " from " + cardId)
                .setPositiveButton("Pay Fine", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(CollectFineActivity.this, AdminIssuedBooks.class);
                    intent.putExtra("issuesList", (Serializable) issuesList);
                    startActivity(intent);
                    issuesList.clear();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    private void updateFineAmount(String issueId, int totalFine ) {
        DatabaseReference issueRef = FirebaseDatabase.getInstance().getReference("IssuesdBooks").child(issueId);
        issueRef.child("fineamount").setValue(totalFine)
                .addOnSuccessListener(aVoid -> {
                    displaySnackbar("Fine amount updated successfully.");
                })
                .addOnFailureListener(e -> displaySnackbar("Failed to update fine amount."));
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(IssueModel issue) {
        displaySnackbar(issue.getBookname());
    }
}
