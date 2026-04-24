package com.library.client.model;

/**
 * Client-side Book model for JavaFX TableView display.
 * Simple POJO deserialized from backend JSON responses.
 */
public class Book {
    private long id;              // Database primary key
    private String bookId;        // Business identifier (e.g., B001)
    private String title;
    private String author;
    private String isbn;
    private boolean available;

    /**
     * Default constructor for JSON deserialization.
     */
    public Book() {
    }

    /**
     * Full constructor.
     */
    public Book(long id, String bookId, String title, String author, String isbn, boolean available) {
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = available;
    }

    // ===== GETTERS & SETTERS =====

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", available=" + available +
                '}';
    }
}
