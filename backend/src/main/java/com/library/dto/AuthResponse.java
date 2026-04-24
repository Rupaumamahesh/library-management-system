package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for authentication response body (login response).
 */
@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;
}
