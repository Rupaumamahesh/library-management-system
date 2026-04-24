package com.library.repository;

import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Member entity.
 * Provides CRUD operations and custom queries for member management.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Find member by business identifier.
     */
    Optional<Member> findByMemberId(String memberId);

    /**
     * Find member by email.
     */
    Optional<Member> findByEmail(String email);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if member ID exists.
     */
    boolean existsByMemberId(String memberId);
}
