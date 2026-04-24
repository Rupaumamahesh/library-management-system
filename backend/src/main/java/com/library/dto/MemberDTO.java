package com.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Member request/response bodies.
 * Used for API input validation and JSON serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

    private Long id;

    @NotBlank(message = "Member ID is required")
    @Size(max = 20, message = "Member ID must not exceed 20 characters")
    private String memberId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private int borrowedCount;
}
