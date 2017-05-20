package com.example.android.booklisting;

/**
 * Created by nalin on 20-May-17.
 */

public class Book {
    /**
     * Title of the Book
     */
    private final String title;

    /**
     * Author of the Book
     */
    private final String author;

    /**
     * Constructs a new {@link Book}.
     *
     * @param bookTitle  is the title of the book
     * @param bookAuthor is the author of the book
     */
    public Book(String bookTitle, String bookAuthor) {
        title = bookTitle;
        author = bookAuthor;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }


}
