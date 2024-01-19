package com.vrnitsolution.librarymanagement;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.Query;
import com.vrnitsolution.librarymanagement.adapter.BooksAdapter;
import com.vrnitsolution.librarymanagement.model.NewBookModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllBooksUser extends AppCompatActivity implements BooksAdapter.OnItemClickListener {
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
        setContentView(R.layout.activity_all_books_user);

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
        displaySnackbar("Book Title :"+book.getBookTitle());
    }

    public void displaySnackbar(String txt) {
        Snackbar.make(findViewById(android.R.id.content), txt, Snackbar.LENGTH_LONG).show();
    }

}


