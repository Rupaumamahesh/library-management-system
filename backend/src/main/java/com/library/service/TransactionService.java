package com.library.service;

import com.library.dto.TransactionDTO;
import com.library.exception.BorrowLimitException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Transaction;
import com.library.repository.MemberRepository;
import com.library.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for transaction management (borrow/return operations).
 * The most critical service - orchestrates all business rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final int BORROW_LIMIT = 3;
    private static final String RECORD_ID_PREFIX = "R";

    private final TransactionRepository transactionRepository;
    private final BookService bookService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    /**
     * Borrows a book for a member.
     * Enforces all business rules:
     * - Member exists
     * - Book exists
     * - Book is available
     * - Member has fewer than 3 active borrows
     *
     * @param memberId Member's business identifier
     * @param bookId Book's business identifier
     * @return Created transaction
     * @throws BorrowLimitException if any business rule violated
     */
    @Transactional
    public TransactionDTO borrowBook(String memberId, String bookId) {
        // Get member and book
        Member member = memberService.getMemberByIdInternal(memberId);
        Book book = bookService.getBookByIdInternal(bookId);

        // Validate book is available
        if (!book.isAvailable()) {
            throw new BorrowLimitException("Book is not available: " + bookId);
        }

        // Check borrow limit (max 3 books)
        long activeBorrows = transactionRepository.countByMemberAndStatus(member);
        if (activeBorrows >= BORROW_LIMIT) {
            throw new BorrowLimitException("Borrow limit reached (max 3 books per member)");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setRecordId(generateRecordId());
        transaction.setBook(book);
        transaction.setMember(member);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setStatus(Transaction.Status.BORROWED);

        // Update book and member
        book.setAvailable(false);
        member.setBorrowedCount((int) activeBorrows + 1);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Book borrowed: {} by member {}", bookId, memberId);

        return mapToDTO(saved);
    }

    /**
     * Returns a borrowed book.
     * Finds the active borrow record, marks it as returned, and restores book availability.
     *
     * @param memberId Member's business identifier
     * @param bookId Book's business identifier
     * @return Updated transaction
     * @throws BorrowLimitException if no active borrow found
     */
    @Transactional
    public TransactionDTO returnBook(String memberId, String bookId) {
        // Get member and book
        Member member = memberService.getMemberByIdInternal(memberId);
        Book book = bookService.getBookByIdInternal(bookId);

        // Find active borrow record
        Transaction transaction = transactionRepository.findActiveBorrow(member, book)
                .orElseThrow(() -> new BorrowLimitException(
                        "No active borrow found for member " + memberId + " and book " + bookId
                ));

        // Update transaction
        transaction.setReturnDate(LocalDate.now());
        transaction.setStatus(Transaction.Status.RETURNED);

        // Update book and member
        book.setAvailable(true);
        member.setBorrowedCount(member.getBorrowedCount() - 1);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Book returned: {} by member {}", bookId, memberId);

        return mapToDTO(saved);
    }

    /**
     * Gets all transactions.
     */
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets borrow history for a member.
     */
    public List<TransactionDTO> getHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
        
        return transactionRepository.findByMember(member).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets active (BORROWED status) transactions.
     */
    public List<TransactionDTO> getActiveTransactions() {
        return transactionRepository.findByStatus(Transaction.Status.BORROWED).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generates a unique record ID.
     */
    private String generateRecordId() {
        long count = transactionRepository.count();
        return RECORD_ID_PREFIX + String.format("%03d", count + 1);
    }

    /**
     * DTO mapper.
     */
    private TransactionDTO mapToDTO(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getRecordId(),
                transaction.getBook().getBookId(),
                transaction.getMember().getMemberId(),
                transaction.getBorrowDate().toString(),
                transaction.getReturnDate() != null ? transaction.getReturnDate().toString() : null,
                transaction.getStatus().name()
        );
    }
}
