package com.library.service;

import com.library.dto.BookDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for book management.
 * Handles all business logic for books.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;

    /**
     * Adds a new book to the catalogue.
     *
     * @param bookDTO Book data
     * @return Saved book
     * @throws IllegalArgumentException if bookId already exists
     */
    @Transactional
    public BookDTO addBook(BookDTO bookDTO) {
        if (bookRepository.existsByBookId(bookDTO.getBookId())) {
            throw new IllegalArgumentException("Book ID already exists: " + bookDTO.getBookId());
        }

        Book book = new Book();
        book.setBookId(bookDTO.getBookId());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());
        book.setAvailable(true);

        Book saved = bookRepository.save(book);
        log.info("Book added: {}", saved.getBookId());
        return mapToDTO(saved);
    }

    /**
     * Gets all books in the catalogue.
     */
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets a single book by ID.
     *
     * @throws ResourceNotFoundException if book not found
     */
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        return mapToDTO(book);
    }

    /**
     * Searches books by title.
     */
    public List<BookDTO> searchByTitle(String query) {
        return bookRepository.searchByTitle(query).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Searches books by author.
     */
    public List<BookDTO> searchByAuthor(String query) {
        return bookRepository.searchByAuthor(query).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates a book.
     */
    @Transactional
    public BookDTO updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());

        Book updated = bookRepository.save(book);
        log.info("Book updated: {}", updated.getBookId());
        return mapToDTO(updated);
    }

    /**
     * Deletes a book (only if not currently borrowed).
     */
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        if (!book.isAvailable()) {
            throw new IllegalArgumentException("Cannot delete a borrowed book");
        }

        bookRepository.deleteById(id);
        log.info("Book deleted: {}", id);
    }

    /**
     * Gets a book by business identifier.
     * Used internally by other services.
     */
    public Book getBookByIdInternal(String bookId) {
        return bookRepository.findByBookId(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
    }

    /**
     * DTO mapper.
     */
    private BookDTO mapToDTO(Book book) {
        return new BookDTO(
                book.getId(),
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.isAvailable()
        );
    }
}
