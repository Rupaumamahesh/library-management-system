package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for borrow request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequest {

    @NotBlank(message = "Member ID is required")
    private String memberId;

    @NotBlank(message = "Book ID is required")
    private String bookId;
}
