package com.vrnitsolution.librarymanagement.model;

import java.io.Serializable;

public class IssueModel implements Serializable {
    private String userId;
    private String librarycardid;
    private String issuedDate;
    private String expiryIssuingDate;
    private String bookReturnDate;
    private boolean bookReturnStatus;
    private String bookStatus;
    private String bookid;
    private  String bookname;
    private int fineamount;

    public IssueModel() {
    }

    public IssueModel(String userId, String librarycardid, String issuedDate, String expiryIssuingDate, String bookReturnDate, boolean bookReturnStatus, String bookStatus, String bookid, String bookname, int fineamount) {
        this.userId = userId;
        this.librarycardid = librarycardid;
        this.issuedDate = issuedDate;
        this.expiryIssuingDate = expiryIssuingDate;
        this.bookReturnDate = bookReturnDate;
        this.bookReturnStatus = bookReturnStatus;
        this.bookStatus = bookStatus;
        this.bookid = bookid;
        this.bookname = bookname;
        this.fineamount = fineamount;
    }

    public int getFineamount() {
        return fineamount;
    }

    public void setFineamount(int fineamount) {
        this.fineamount = fineamount;
    }

    public String getBookid() {
        return bookid;
    }

    public void setBookid(String bookid) {
        this.bookid = bookid;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLibrarycardid() {
        return librarycardid;
    }

    public void setLibrarycardid(String librarycardid) {
        this.librarycardid = librarycardid;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getExpiryIssuingDate() {
        return expiryIssuingDate;
    }

    public void setExpiryIssuingDate(String expiryIssuingDate) {
        this.expiryIssuingDate = expiryIssuingDate;
    }

    public String getBookReturnDate() {
        return bookReturnDate;
    }

    public void setBookReturnDate(String bookReturnDate) {
        this.bookReturnDate = bookReturnDate;
    }

    public boolean isBookReturnStatus() {
        return bookReturnStatus;
    }

    public void setBookReturnStatus(boolean bookReturnStatus) {
        this.bookReturnStatus = bookReturnStatus;
    }

    public String getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(String bookStatus) {
        this.bookStatus = bookStatus;
    }
}
