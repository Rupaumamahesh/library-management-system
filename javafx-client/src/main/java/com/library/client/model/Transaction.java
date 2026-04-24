package com.library.client.model;

/**
 * Client-side Transaction model for JavaFX TableView display.
 * Simple POJO deserialized from backend JSON responses.
 */
public class Transaction {
    private long id;              // Database primary key
    private String recordId;      // Business identifier (e.g., R001)
    private String bookId;        // Business identifier
    private String memberId;      // Business identifier
    private String borrowDate;    // ISO format (yyyy-MM-dd)
    private String returnDate;    // ISO format or null
    private String status;        // BORROWED or RETURNED

    /**
     * Default constructor for JSON deserialization.
     */
    public Transaction() {
    }

    /**
     * Full constructor.
     */
    public Transaction(long id, String recordId, String bookId, String memberId,
                      String borrowDate, String returnDate, String status) {
        this.id = id;
        this.recordId = recordId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // ===== GETTERS & SETTERS =====

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", recordId='" + recordId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", borrowDate='" + borrowDate + '\'' +
                ", returnDate='" + returnDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
