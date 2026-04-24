CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id VARCHAR(20) UNIQUE NOT NULL,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE,
    status ENUM('BORROWED', 'RETURNED') DEFAULT 'BORROWED' NOT NULL,
    CONSTRAINT fk_book_id FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT,
    CONSTRAINT fk_member_id FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE RESTRICT,
    INDEX idx_record_id (record_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
