package com.library.client.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.library.client.model.Book;
import com.library.client.model.Member;
import com.library.client.model.Transaction;
import com.library.client.session.SessionManager;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Singleton API client for all HTTP communication with the Spring Boot backend.
 * Uses java.net.http.HttpClient (no third-party HTTP library).
 * All methods return results via JavaFX Task<T> for background thread execution.
 * Automatically attaches JWT token from SessionManager as Authorization: Bearer header.
 *
 * Backend API base URL: http://localhost:8080/api
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final ApiClient INSTANCE = new ApiClient();

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final Gson gson;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.gson = new Gson();
    }

    /**
     * Gets the singleton instance.
     */
    public static ApiClient getInstance() {
        return INSTANCE;
    }

    // ===== AUTHENTICATION =====

    /**
     * Login endpoint. Returns JWT token on success.
     *
     * @param username User's username
     * @param password User's password
     * @return Task<String> JWT token
     */
    public Task<String> login(String username, String password) {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                JsonObject body = new JsonObject();
                body.addProperty("username", username);
                body.addProperty("password", password);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject responseBody = gson.fromJson(response.body(), JsonObject.class);
                    String token = responseBody.get("token").getAsString();
                    logger.info("Login successful for user: {}", username);
                    return token;
                } else {
                    throw new RuntimeException("Login failed: " + response.statusCode() + " " + response.body());
                }
            }
        };
    }

    // ===== BOOKS =====

    /**
     * Fetches all books from the backend.
     *
     * @return Task<List<Book>>
     */
    public Task<List<Book>> getBooks() {
        return new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/books", "GET", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.debug("Fetched all books");
                    return gson.fromJson(response.body(), new TypeToken<List<Book>>(){}.getType());
                } else {
                    throw new RuntimeException("Failed to fetch books: " + response.statusCode());
                }
            }
        };
    }

    /**
     * Adds a new book.
     *
     * @param book Book to add
     * @return Task<Book> created book
     */
    public Task<Book> addBook(Book book) {
        return new Task<Book>() {
            @Override
            protected Book call() throws Exception {
                String body = gson.toJson(book);
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/books", "POST", body);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    logger.info("Book added: {}", book.getTitle());
                    return gson.fromJson(response.body(), Book.class);
                } else {
                    throw new RuntimeException("Failed to add book: " + response.statusCode() + " " + response.body());
                }
            }
        };
    }

    /**
     * Updates a book.
     *
     * @param bookId Book ID
     * @param book Updated book data
     * @return Task<Book>
     */
    public Task<Book> updateBook(long bookId, Book book) {
        return new Task<Book>() {
            @Override
            protected Book call() throws Exception {
                String body = gson.toJson(book);
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/books/" + bookId, "PUT", body);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.info("Book updated: {}", bookId);
                    return gson.fromJson(response.body(), Book.class);
                } else {
                    throw new RuntimeException("Failed to update book: " + response.statusCode());
                }
            }
        };
    }

    /**
     * Deletes a book.
     *
     * @param bookId Book ID
     * @return Task<Void>
     */
    public Task<Void> deleteBook(long bookId) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/books/" + bookId, "DELETE", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 204 || response.statusCode() == 200) {
                    logger.info("Book deleted: {}", bookId);
                    return null;
                } else {
                    throw new RuntimeException("Failed to delete book: " + response.statusCode());
                }
            }
        };
    }

    // ===== MEMBERS =====

    /**
     * Fetches all members from the backend.
     *
     * @return Task<List<Member>>
     */
    public Task<List<Member>> getMembers() {
        return new Task<List<Member>>() {
            @Override
            protected List<Member> call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/members", "GET", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.debug("Fetched all members");
                    return gson.fromJson(response.body(), new TypeToken<List<Member>>(){}.getType());
                } else {
                    throw new RuntimeException("Failed to fetch members: " + response.statusCode());
                }
            }
        };
    }

    /**
     * Registers a new member.
     *
     * @param member Member to register
     * @return Task<Member> registered member
     */
    public Task<Member> addMember(Member member) {
        return new Task<Member>() {
            @Override
            protected Member call() throws Exception {
                String body = gson.toJson(member);
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/members", "POST", body);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    logger.info("Member registered: {}", member.getName());
                    return gson.fromJson(response.body(), Member.class);
                } else {
                    throw new RuntimeException("Failed to register member: " + response.statusCode() + " " + response.body());
                }
            }
        };
    }

    /**
     * Deletes a member.
     *
     * @param memberId Member ID
     * @return Task<Void>
     */
    public Task<Void> deleteMember(long memberId) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/members/" + memberId, "DELETE", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 204 || response.statusCode() == 200) {
                    logger.info("Member deleted: {}", memberId);
                    return null;
                } else {
                    throw new RuntimeException("Failed to delete member: " + response.statusCode());
                }
            }
        };
    }

    // ===== TRANSACTIONS =====

    /**
     * Borrows a book for a member.
     *
     * @param memberId Member ID
     * @param bookId Book ID
     * @return Task<Transaction>
     */
    public Task<Transaction> borrowBook(String memberId, String bookId) {
        return new Task<Transaction>() {
            @Override
            protected Transaction call() throws Exception {
                JsonObject body = new JsonObject();
                body.addProperty("memberId", memberId);
                body.addProperty("bookId", bookId);

                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/borrow", "POST", body.toString());
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201 || response.statusCode() == 200) {
                    logger.info("Book borrowed: {} by member {}", bookId, memberId);
                    return gson.fromJson(response.body(), Transaction.class);
                } else {
                    throw new RuntimeException("Failed to borrow book: " + response.statusCode() + " " + response.body());
                }
            }
        };
    }

    /**
     * Returns a borrowed book.
     *
     * @param transactionId Transaction ID
     * @return Task<Transaction>
     */
    public Task<Transaction> returnBook(long transactionId) {
        return new Task<Transaction>() {
            @Override
            protected Transaction call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/return/" + transactionId, "POST", "{}");
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.info("Book returned: transaction {}", transactionId);
                    return gson.fromJson(response.body(), Transaction.class);
                } else {
                    throw new RuntimeException("Failed to return book: " + response.statusCode() + " " + response.body());
                }
            }
        };
    }

    /**
     * Gets all transactions (borrow history).
     *
     * @return Task<List<Transaction>>
     */
    public Task<List<Transaction>> getTransactions() {
        return new Task<List<Transaction>>() {
            @Override
            protected List<Transaction> call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/transactions", "GET", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.debug("Fetched all transactions");
                    return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
                } else {
                    throw new RuntimeException("Failed to fetch transactions: " + response.statusCode());
                }
            }
        };
    }

    /**
     * Gets borrow history for a member.
     *
     * @param memberId Member ID
     * @return Task<List<Transaction>>
     */
    public Task<List<Transaction>> getHistory(long memberId) {
        return new Task<List<Transaction>>() {
            @Override
            protected List<Transaction> call() throws Exception {
                HttpRequest request = createAuthenticatedRequest(BASE_URL + "/members/" + memberId + "/history", "GET", null);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    logger.debug("Fetched history for member: {}", memberId);
                    return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
                } else {
                    throw new RuntimeException("Failed to fetch history: " + response.statusCode());
                }
            }
        };
    }

    // ===== HELPER METHODS =====

    /**
     * Creates an authenticated HTTP request with Bearer token from SessionManager.
     *
     * @param url Request URL
     * @param method HTTP method (GET, POST, PUT, DELETE)
     * @param body Request body (null for GET/DELETE)
     * @return HttpRequest
     */
    private HttpRequest createAuthenticatedRequest(String url, String method, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        String token = SessionManager.getInstance().getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        switch (method) {
            case "GET":
                builder.GET();
                break;
            case "POST":
                builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                break;
            case "PUT":
                builder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
                break;
            case "DELETE":
                builder.DELETE();
                break;
        }

        return builder.build();
    }
}
