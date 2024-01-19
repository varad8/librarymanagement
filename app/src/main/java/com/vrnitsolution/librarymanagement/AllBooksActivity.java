package com.vrnitsolution.librarymanagement;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.Query;
import com.vrnitsolution.librarymanagement.adapter.BooksAdapter;
import com.vrnitsolution.librarymanagement.model.IssueModel;
import com.vrnitsolution.librarymanagement.model.NewBookModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vrnitsolution.librarymanagement.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllBooksActivity extends AppCompatActivity implements BooksAdapter.OnItemClickListener {
    private RecyclerView bookRecyclerView;
    private EditText searchEditText;
    private ImageView search;
    private BooksAdapter booksAdapter;
    private DatabaseReference databaseReference;
    AlertDialog updateDialog;
    CustomProgressDialog loadingdailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_books);

        loadingdailog = new CustomProgressDialog(this);
        loadingdailog.setMessage("Loading...");
        loadingdailog.setCancelable(false);

        bookRecyclerView = findViewById(R.id.bookRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        search = findViewById(R.id.search);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Books");


        booksAdapter = new BooksAdapter(new ArrayList<>(), this);
        bookRecyclerView.setAdapter(booksAdapter);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        bookRecyclerView.setLayoutManager(layoutManager);

        // Fetch data from Firebase and update the adapter
        fetchDataFromFirebase();

        search.setOnClickListener(view -> {
            String BookTitle = searchEditText.getText().toString().trim();

            if (BookTitle.isEmpty()) {
                displaySnackbar("Please Enter Book Name");
            } else {
                searchFetchData(BookTitle);
            }
        });


    }

    private void searchFetchData(String bookTitle) {
        Query searchQuery = databaseReference.orderByChild("bookTitle").startAt(bookTitle).endAt(bookTitle + "\uf8ff");
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<NewBookModel> bookList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NewBookModel book = snapshot.getValue(NewBookModel.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }
                booksAdapter.setBookList(bookList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                displaySnackbar("" + databaseError.getMessage());
            }
        });
    }

    private void fetchDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<NewBookModel> bookList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    NewBookModel book = snapshot.getValue(NewBookModel.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }


                booksAdapter.setBookList(bookList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                displaySnackbar("" + databaseError.getMessage());
            }
        });
    }

    public void backPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(NewBookModel book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Action")
                .setItems(new CharSequence[]{"Update", "Delete", "Issue Book"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            updateThatBookData(book);
                            break;
                        case 1:
                            deleteThatBookData(book);
                            break;
                        case 2:
                            issueBookForThatUser(book);
                            break;
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void issueBookForThatUser(NewBookModel book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Issue Book");
        builder.setMessage("Enter Library Card ID:");
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String libraryCardId = input.getText().toString().trim();
                if (!libraryCardId.isEmpty()) {
                    searchUserInCollection(libraryCardId, book);
                } else {
                    displaySnackbar("Please enter Library Card ID");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void searchUserInCollection(String libraryCardId, NewBookModel book) {
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.orderByChild("librarycardid").equalTo(libraryCardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            showUserInformationDialog(user, book);
                        }
                    }
                } else {
                    displaySnackbar("User not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                displaySnackbar(""+databaseError.getMessage());
            }
        });
    }

    private void showUserInformationDialog(User user, NewBookModel book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.user_information_dialog, null);
        builder.setView(dialogView);

        ImageView profileImage = dialogView.findViewById(R.id.profileImage);
        TextView usernameTextView = dialogView.findViewById(R.id.usernameTextView);
        TextView rollNoTextView = dialogView.findViewById(R.id.rollNoTextView);
        TextView addressTextView = dialogView.findViewById(R.id.addressTextView);
        TextView mobileNoTextView = dialogView.findViewById(R.id.mobileNoTextView);
        TextView libraryCardIdTextView = dialogView.findViewById(R.id.libraryCardIdTextView);
        TextView emailTextView = dialogView.findViewById(R.id.emailTextView);


        usernameTextView.setText("Username: " + user.getUsername());
        rollNoTextView.setText("Roll No: " + user.getRollNo());
        addressTextView.setText("Address: " + user.getAddress());
        mobileNoTextView.setText("Mobile No: " + user.getMobileno());
        libraryCardIdTextView.setText("Library Card ID: " + user.getLibrarycardid());
        emailTextView.setText("Email: " + user.getUseremail());


        Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);

        builder.setPositiveButton("Issue Book", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openExpiryDatePickerDialog(user, book);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


//    private void openExpiryDatePickerDialog(User user, NewBookModel book) {
//        Calendar calendar = Calendar.getInstance();
//        int currentYear = calendar.get(Calendar.YEAR);
//        int currentMonth = calendar.get(Calendar.MONTH);
//        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss aa", Locale.getDefault());
//        String currentDateAndTime = sdf.format(new Date());
//
//        // Set the minimum date to today
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                this,
//                (view, year, month, dayOfMonth) -> {
//                    // Format the day with two digits, adding a leading zero if necessary
//                    String formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth);
//
//                    String expirationDate = year + "-" + (month + 1) + "-" + formattedDay;
//
//                    IssueModel issueModel = new IssueModel();
//                    issueModel.setUserId(user.getUserid());
//                    issueModel.setLibrarycardid(user.getLibrarycardid());
//                    issueModel.setIssuedDate(currentDateAndTime);
//                    issueModel.setBookReturnDate("");
//                    issueModel.setExpiryIssuingDate(expirationDate);
//                    issueModel.setBookReturnStatus(false);
//                    issueModel.setBookStatus("Issued Book");
//                    issueModel.setBookid(book.getBookISBNNO());
//                    issueModel.setBookname(book.getBookTitle());
//
//                    saveIssueToFirebase(issueModel);
//                },
//                currentYear,
//                currentMonth,
//                currentDay
//        );
//
//        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
//
//        datePickerDialog.setCanceledOnTouchOutside(false);
//        datePickerDialog.show();
//    }

    private void openExpiryDatePickerDialog(User user, NewBookModel book) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss aa", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Set the minimum date to today
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Format the day with two digits, adding a leading zero if necessary
                    String formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth);

                    // Format the month with two digits, adding a leading zero if necessary
                    String formattedMonth = String.format(Locale.getDefault(), "%02d", (month + 1));

                    String expirationDate = formattedDay  + "-" + formattedMonth + "-" + year;

                    IssueModel issueModel = new IssueModel();
                    issueModel.setUserId(user.getUserid());
                    issueModel.setLibrarycardid(user.getLibrarycardid());
                    issueModel.setIssuedDate(currentDateAndTime);
                    issueModel.setBookReturnDate("");
                    issueModel.setExpiryIssuingDate(expirationDate);
                    issueModel.setBookReturnStatus(false);
                    issueModel.setBookStatus("Issued Book");
                    issueModel.setBookid(book.getBookISBNNO());
                    issueModel.setBookname(book.getBookTitle());

                    saveIssueToFirebase(issueModel);
                },
                currentYear,
                currentMonth,
                currentDay
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.show();
    }


    private void saveIssueToFirebase(IssueModel issueModel) {
        loadingdailog.setMessage("Saving data..");
        loadingdailog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("IssuesdBooks");
        String issueKey = databaseReference.push().getKey();
        issueModel.setBookid(issueKey);

        databaseReference.child(issueKey).setValue(issueModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadingdailog.dismiss();
                        displaySnackbar("Book issued successfully!");
                    } else {
                        loadingdailog.dismiss();
                        displaySnackbar("Failed to issue the book. Please try again.");
                    }
                });
    }



    private void deleteThatBookData(NewBookModel book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete " + book.getBookTitle() + " book ?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            loadingdailog.setMessage("deleting " + book.getBookTitle() + " book");
            loadingdailog.show();
            performDelete(book);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            loadingdailog.dismiss();
            dialog.dismiss();
        });
        builder.show();
    }

    private void performDelete(NewBookModel book) {
        DatabaseReference bookRef = databaseReference.child(book.getBookKey());

        bookRef.removeValue().addOnSuccessListener(unused -> {
            loadingdailog.dismiss();
            displaySnackbar("Book deleted successfully");
        }).addOnFailureListener(e -> {
            loadingdailog.dismiss();
            displaySnackbar("Failed to delete book: " + e.getMessage());
        });
    }


    private void updateThatBookData(NewBookModel book) {
        View dialogView = getLayoutInflater().inflate(R.layout.add_book_custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        EditText bookNameEditText = dialogView.findViewById(R.id.bookNaame);
        EditText isbnEditText = dialogView.findViewById(R.id.bookISBNNO);
        EditText quantityEditText = dialogView.findViewById(R.id.bookQuantity);
        EditText rackNoEditText = dialogView.findViewById(R.id.bookRackNo);
        EditText categoryEditText = dialogView.findViewById(R.id.bookCategory);
        AppCompatButton saveButton = dialogView.findViewById(R.id.savedBooks);
        TextView textview1 = dialogView.findViewById(R.id.textview1);
        textview1.setText("Update " + book.getBookTitle() + " Book");

        bookNameEditText.setText(book.getBookTitle());
        isbnEditText.setText(book.getBookISBNNO());
        quantityEditText.setText(book.getBookQuantity());
        rackNoEditText.setText(book.getBookRackNo());
        categoryEditText.setText(book.getBookCategory());

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
                    NewBookModel bookModel = new NewBookModel(bookName, category, isbn, quantity, rackNo, book.getBookKey());
                    loadingdailog.setMessage("Updating Data..please wait");
                    loadingdailog.show();
                    updateToFirebase(bookModel);
                }

            }


        });
        updateDialog = builder.create();
        updateDialog.show();
    }

    private void updateToFirebase(NewBookModel bookModel) {
        DatabaseReference bookRef = databaseReference.child(bookModel.getBookKey());

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("bookTitle", bookModel.getBookTitle());
        updateData.put("bookCategory", bookModel.getBookCategory());
        updateData.put("bookISBNNO", bookModel.getBookISBNNO());
        updateData.put("bookQuantity", bookModel.getBookQuantity());
        updateData.put("bookRackNo", bookModel.getBookRackNo());


        bookRef.updateChildren(updateData).addOnSuccessListener(unused -> {
            updateDialog.dismiss();
            loadingdailog.dismiss();
            displaySnackbar("Update Book Successfully");
        }).addOnFailureListener(e -> {
            updateDialog.dismiss();
            loadingdailog.dismiss();
            displaySnackbar("Failed to update" + e.getMessage());
        });
    }


    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }
}
