package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Book request/response bodies.
 * Used for API input validation and JSON serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotBlank(message = "Book ID is required")
    @Size(max = 20, message = "Book ID must not exceed 20 characters")
    private String bookId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    private String isbn;

    private boolean available;
}
