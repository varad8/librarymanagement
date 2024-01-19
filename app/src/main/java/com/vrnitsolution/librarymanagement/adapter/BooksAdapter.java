package com.vrnitsolution.librarymanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vrnitsolution.librarymanagement.R;
import com.vrnitsolution.librarymanagement.model.NewBookModel;
import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookViewHolder> {

    private List<NewBookModel> booksList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(NewBookModel book);
    }

    public BooksAdapter(List<NewBookModel> booksList, OnItemClickListener listener) {
        this.booksList = booksList;
        this.onItemClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setBookList(List<NewBookModel> booksList) {
        this.booksList = booksList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_books, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        NewBookModel book = booksList.get(position);

        holder.titleTextView.setText(book.getBookTitle());
        holder.categoryTextView.setText("Category :"+book.getBookCategory());
        holder.isbnTextView.setText("ISBN No :"+book.getBookISBNNO());
        holder.quantityTextView.setText("QTY :"+book.getBookQuantity());
        holder.rackNoTextView.setText("Rack No :"+book.getBookRackNo());

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, categoryTextView, isbnTextView, quantityTextView, rackNoTextView;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            isbnTextView = itemView.findViewById(R.id.isbnTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            rackNoTextView = itemView.findViewById(R.id.rackNoTextView);
        }
    }
}
