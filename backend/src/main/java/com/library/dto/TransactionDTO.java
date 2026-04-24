package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Transaction request/response bodies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;

    private String recordId;

    private String bookId;

    private String memberId;

    private String borrowDate;

    private String returnDate;

    private String status;
}
