package com.library.controller;

import com.library.dto.BorrowRequest;
import com.library.dto.TransactionDTO;
import com.library.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for transaction management (borrow/return).
 * Endpoint base: /api
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/borrow")
    public ResponseEntity<TransactionDTO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.borrowBook(request.getMemberId(), request.getBookId()));
    }

    @PostMapping("/return/{transactionId}")
    public ResponseEntity<TransactionDTO> returnBook(@PathVariable Long transactionId) {
        // This endpoint needs transaction lookup - will be enhanced
        return ResponseEntity.ok(new TransactionDTO());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/members/{memberId}/history")
    public ResponseEntity<List<TransactionDTO>> getMemberHistory(@PathVariable Long memberId) {
        return ResponseEntity.ok(transactionService.getHistory(memberId));
    }
}
