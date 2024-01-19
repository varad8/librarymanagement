package com.vrnitsolution.librarymanagement.model;


public class NewBookModel {
    private String BookTitle;
    private String BookCategory;
    private String BookISBNNO;
    private String BookQuantity;
    private String BookRackNo;
    private String bookKey;

    public NewBookModel() {
    }

    public NewBookModel(String bookTitle, String bookCategory, String bookISBNNO, String bookQuantity, String bookRackNo, String bookKey) {
        BookTitle = bookTitle;
        BookCategory = bookCategory;
        BookISBNNO = bookISBNNO;
        BookQuantity = bookQuantity;
        BookRackNo = bookRackNo;
        this.bookKey = bookKey;
    }

    public String getBookKey() {
        return bookKey;
    }

    public void setBookKey(String bookKey) {
        this.bookKey = bookKey;
    }

    public String getBookTitle() {
        return BookTitle;
    }

    public void setBookTitle(String bookTitle) {
        BookTitle = bookTitle;
    }

    public String getBookCategory() {
        return BookCategory;
    }

    public void setBookCategory(String bookCategory) {
        BookCategory = bookCategory;
    }

    public String getBookISBNNO() {
        return BookISBNNO;
    }

    public void setBookISBNNO(String bookISBNNO) {
        BookISBNNO = bookISBNNO;
    }

    public String getBookQuantity() {
        return BookQuantity;
    }

    public void setBookQuantity(String bookQuantity) {
        BookQuantity = bookQuantity;
    }

    public String getBookRackNo() {
        return BookRackNo;
    }

    public void setBookRackNo(String bookRackNo) {
        BookRackNo = bookRackNo;
    }
}
