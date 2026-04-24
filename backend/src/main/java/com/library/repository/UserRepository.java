package com.library.repository;

import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for User entity.
 * Provides authentication and user lookup queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (used by Spring Security during login).
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);
}
