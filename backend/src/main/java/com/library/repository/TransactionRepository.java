package com.library.repository;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Transaction entity.
 * Provides CRUD operations and custom queries for transaction management.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transactions by member.
     */
    List<Transaction> findByMember(Member member);

    /**
     * Find transactions by book.
     */
    List<Transaction> findByBook(Book book);

    /**
     * Find transactions by status.
     */
    List<Transaction> findByStatus(Transaction.Status status);

    /**
     * Count active (BORROWED) transactions for a member.
     * Used to enforce the 3-book borrow limit.
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.member = :member AND t.status = 'BORROWED'")
    long countByMemberAndStatus(@Param("member") Member member);

    /**
     * Find active transaction for member and book.
     */
    @Query("SELECT t FROM Transaction t WHERE t.member = :member AND t.book = :book AND t.status = 'BORROWED'")
    java.util.Optional<Transaction> findActiveBorrow(@Param("member") Member member, @Param("book") Book book);
}
