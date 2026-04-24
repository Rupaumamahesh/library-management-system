package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Book entity.
 * Provides CRUD operations and custom queries for book management.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find book by business identifier.
     */
    Optional<Book> findByBookId(String bookId);

    /**
     * Find all available books.
     */
    List<Book> findByAvailableTrue();

    /**
     * Search books by title (case-insensitive).
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchByTitle(@Param("query") String query);

    /**
     * Search books by author (case-insensitive).
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchByAuthor(@Param("query") String query);

    /**
     * Check if book ID exists.
     */
    boolean existsByBookId(String bookId);
}
