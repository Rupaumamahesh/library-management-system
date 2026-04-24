package com.library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT utility for token generation and validation.
 * Uses JJWT library for JWT operations.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for a user.
     *
     * @param username User's username
     * @param role User's role (ADMIN, LIBRARIAN)
     * @return JWT token
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extracts role from JWT token.
     *
     * @param token JWT token
     * @return Role
     */
    public String extractRole(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    /**
     * Gets all claims from token.
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
