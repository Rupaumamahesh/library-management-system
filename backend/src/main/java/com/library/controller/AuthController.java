package com.library.controller;

import com.library.dto.AuthRequest;
import com.library.dto.AuthResponse;
import com.library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for authentication.
 * Refactored to remove redundant DB hits and improve error handling.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil = new JwtUtil();

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // 1. Perform Authentication
            // This internally calls your UserDetailsService and checks the password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // 2. Extract User Details from the authentication object
            // This avoids calling userRepository.findByUsername() again (saving a DB trip)
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Extract Role (Assuming one role for simplicity)
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");

            // 4. Generate Token
            String token = jwtUtil.generateToken(userDetails.getUsername(), role);
            
            log.info("User authenticated successfully: {}", userDetails.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid username or password for {}", authRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("System error during login for {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}