package com.library.client.model;

/**
 * Client-side Member model for JavaFX TableView display.
 * Simple POJO deserialized from backend JSON responses.
 */
public class Member {
    private long id;              // Database primary key
    private String memberId;      // Business identifier (e.g., M001)
    private String name;
    private String email;
    private String phone;
    private int borrowedCount;

    /**
     * Default constructor for JSON deserialization.
     */
    public Member() {
    }

    /**
     * Full constructor.
     */
    public Member(long id, String memberId, String name, String email, String phone, int borrowedCount) {
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.borrowedCount = borrowedCount;
    }

    // ===== GETTERS & SETTERS =====

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", memberId='" + memberId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", borrowedCount=" + borrowedCount +
                '}';
    }
}
