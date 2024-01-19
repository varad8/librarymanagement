package com.vrnitsolution.librarymanagement.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vrnitsolution.librarymanagement.R;
import com.vrnitsolution.librarymanagement.model.IssueModel;

import java.util.List;

public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssueViewHolder> {

    private List<IssueModel> issuesList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(IssueModel issue);
    }

    public IssuesAdapter(List<IssueModel> issuesList, OnItemClickListener listener) {
        this.issuesList = issuesList;
        this.onItemClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setIssuesList(List<IssueModel> issuesList) {
        this.issuesList = issuesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issues, parent, false);
        return new IssueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        IssueModel issue = issuesList.get(position);

        holder.bookNameTextView.setText("Book Name: " + issue.getBookname());
        holder.issuedDateTextView.setText("Issued Date: " + issue.getIssuedDate());
        holder.expiryDateTextView.setText("Expiry Date: " + issue.getExpiryIssuingDate());

        holder.bookIdTextView.setText("Book ID: " + issue.getBookid());
        holder.libraryCardIdTextView.setText("Library Card ID: " + issue.getLibrarycardid());
        holder.bookReturnDateTextView.setText("Book Return Date: " + issue.getBookReturnDate());
        holder.bookReturnStatusTextView.setText("Book Return Status: " + issue.isBookReturnStatus());
        holder.bookStatusTextView.setText("Book Status: " + issue.getBookStatus());
        holder.userIdTextView.setText("User ID: " + issue.getUserId());
        holder.fineammount.setText("Fine Amount: " + issue.getFineamount());

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(issue);
            }
        });
    }

    @Override
    public int getItemCount() {
        return issuesList.size();
    }

    public static class IssueViewHolder extends RecyclerView.ViewHolder {
        TextView bookNameTextView, issuedDateTextView, expiryDateTextView,
                bookIdTextView, libraryCardIdTextView, bookReturnDateTextView,
                bookReturnStatusTextView, bookStatusTextView, userIdTextView,fineammount;

        public IssueViewHolder(@NonNull View itemView) {
            super(itemView);

            bookNameTextView = itemView.findViewById(R.id.bookNameTextView);
            issuedDateTextView = itemView.findViewById(R.id.issuedDateTextView);
            expiryDateTextView = itemView.findViewById(R.id.expiryDateTextView);

            bookIdTextView = itemView.findViewById(R.id.bookIdTextView);
            libraryCardIdTextView = itemView.findViewById(R.id.libraryCardIdTextView);
            bookReturnDateTextView = itemView.findViewById(R.id.bookReturnDateTextView);
            bookReturnStatusTextView = itemView.findViewById(R.id.bookReturnStatusTextView);
            bookStatusTextView = itemView.findViewById(R.id.bookStatusTextView);
            userIdTextView = itemView.findViewById(R.id.userIdTextView);
            fineammount=itemView.findViewById(R.id.fineammount);
        }
    }
}
