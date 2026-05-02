package com.library;

import com.library.dto.*;
import com.library.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration smoke test — requires MySQL running on localhost:3306/library_db.
 * Exercises the full borrow-return lifecycle through the live HTTP layer and
 * verifies Bean Validation responses and pagination shape.
 * All created data is cleaned up via repositories after each test.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    private String base;
    private String token;

    private static final String SMOKE_MEMBER_ID = "SMOKE001";
    private static final String SMOKE_BOOK_ID   = "SMOKE001";

    @BeforeEach
    void authenticate() {
        base = "http://localhost:" + port + "/api";
        ResponseEntity<AuthResponse> loginResp = rest.postForEntity(
                base + "/auth/login",
                new AuthRequest("admin", "admin123"),
                AuthResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = loginResp.getBody();
        assertThat(body).isNotNull();
        token = body.getToken();
        assertThat(token).isNotBlank();
    }

    @AfterEach
    void cleanup() {
        // Delete in FK-safe order: transactions → member → book
        memberRepository.findByMemberId(SMOKE_MEMBER_ID).ifPresent(member -> {
            transactionRepository.findByMember(member).forEach(transactionRepository::delete);
            memberRepository.delete(member);
        });
        bookRepository.findByBookId(SMOKE_BOOK_ID).ifPresent(bookRepository::delete);
    }

    // ── 1. Full borrow-return lifecycle ──────────────────────────────────────

    @Test
    @Order(1)
    void fullBorrowReturnFlow() {
        // Register member
        ResponseEntity<MemberDTO> memberResp = exchange(
                HttpMethod.POST, "/members",
                new MemberDTO(null, SMOKE_MEMBER_ID, "Smoke User", "smoke@test.com", "9000000001", 0),
                MemberDTO.class);
        assertThat(memberResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        MemberDTO member = memberResp.getBody();
        assertThat(member).isNotNull();
        Long memberDbId = member.getId();
        assertThat(memberDbId).isNotNull();

        // Add book
        ResponseEntity<BookDTO> bookResp = exchange(
                HttpMethod.POST, "/books",
                new BookDTO(null, SMOKE_BOOK_ID, "Smoke Test Book", "Test Author", "978-SMOKE-001", true),
                BookDTO.class);
        assertThat(bookResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        BookDTO book = bookResp.getBody();
        assertThat(book).isNotNull();
        assertThat(book.getBookId()).isEqualTo(SMOKE_BOOK_ID);

        // Borrow
        ResponseEntity<TransactionDTO> borrowResp = exchange(
                HttpMethod.POST, "/borrow",
                new BorrowRequest(SMOKE_MEMBER_ID, SMOKE_BOOK_ID),
                TransactionDTO.class);
        assertThat(borrowResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TransactionDTO borrowed = borrowResp.getBody();
        assertThat(borrowed).isNotNull();
        Long txId = borrowed.getId();
        assertThat(borrowed.getStatus()).isEqualTo("BORROWED");
        assertThat(borrowed.getReturnDate()).isNull();

        // History shows the BORROWED record
        ResponseEntity<TransactionDTO[]> histResp = exchange(
                HttpMethod.GET, "/members/" + memberDbId + "/history",
                null, TransactionDTO[].class);
        assertThat(histResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TransactionDTO[] history = histResp.getBody();
        assertThat(history).isNotNull().hasSize(1);
        assertThat(history[0].getStatus()).isEqualTo("BORROWED");

        // Return
        ResponseEntity<TransactionDTO> returnResp = exchange(
                HttpMethod.POST, "/return/" + txId,
                null, TransactionDTO.class);
        assertThat(returnResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        TransactionDTO returned = returnResp.getBody();
        assertThat(returned).isNotNull();
        assertThat(returned.getStatus()).isEqualTo("RETURNED");
        assertThat(returned.getReturnDate()).isNotNull();

        // History now shows RETURNED
        ResponseEntity<TransactionDTO[]> histAfter = exchange(
                HttpMethod.GET, "/members/" + memberDbId + "/history",
                null, TransactionDTO[].class);
        TransactionDTO[] historyAfter = histAfter.getBody();
        assertThat(historyAfter).isNotNull().hasSize(1);
        assertThat(historyAfter[0].getStatus()).isEqualTo("RETURNED");
        assertThat(historyAfter[0].getReturnDate()).isNotNull();
    }

    // ── 2. Bean Validation → 422 Unprocessable Entity ────────────────────────

    @Test
    @Order(2)
    void validation_rejectsMissingBookFields() {
        // Only isbn — bookId, title, author are @NotBlank
        ResponseEntity<Map<String, Object>> resp = exchangeMap(
                HttpMethod.POST, "/books",
                new BookDTO(null, null, null, null, "978-X", true));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("error")).isEqualTo("Validation Failed");
        String msg = (String) body.get("message");
        assertThat(msg).contains("bookId").contains("title").contains("author");
    }

    @Test
    @Order(3)
    void validation_rejectsInvalidEmail() {
        ResponseEntity<Map<String, Object>> resp = exchangeMap(
                HttpMethod.POST, "/members",
                new MemberDTO(null, "X1", "Test", "not-an-email", "123", 0));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        String msg = (String) body.get("message");
        assertThat(msg).containsIgnoringCase("email");
    }

    @Test
    @Order(4)
    void validation_rejectsEmptyBorrowRequest() {
        // Both memberId and bookId are @NotBlank
        ResponseEntity<Map<String, Object>> resp = exchangeMap(
                HttpMethod.POST, "/borrow",
                new BorrowRequest("", ""));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ── 3. Pagination shape ───────────────────────────────────────────────────

    @Test
    @Order(5)
    void pagination_getBooksDefaultPageSize() {
        ResponseEntity<Map<String, Object>> resp = exchangeMap(HttpMethod.GET, "/books", null);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull()
                .containsKeys("content", "totalElements", "totalPages", "size");
        assertThat(body.get("size")).isEqualTo(20);
    }

    @Test
    @Order(6)
    void pagination_getMembersCustomPageSize() {
        ResponseEntity<Map<String, Object>> resp = exchangeMap(
                HttpMethod.GET, "/members?page=0&size=1", null);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("size")).isEqualTo(1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private <T> ResponseEntity<T> exchange(HttpMethod method, String path, Object body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(base + path, method, new HttpEntity<>(body, headers), type);
    }

    private ResponseEntity<Map<String, Object>> exchangeMap(HttpMethod method, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(
                base + path, method, new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
