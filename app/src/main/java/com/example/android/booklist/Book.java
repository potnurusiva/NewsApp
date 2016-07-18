package com.example.android.booklist;

/**
 * Created by PotnuruSiva on 11-07-2016.
 */
public class Book {

    private String mBookName;
    private String mBookAuthor;

    public Book() {
    }

    public Book(String bookName, String bookAuthor) {
        mBookName = bookName;
        mBookAuthor = bookAuthor;
    }

    public String getBookName() {
        return mBookName;
    }

    public String getBookAuthor() {
        return mBookAuthor;
    }

    @Override
    public String toString() {
        return "Book{" +
                "mBookName='" + mBookName + '\'' +
                ", mBookAuthor='" + mBookAuthor + '\'' +
                '}';
    }
}
