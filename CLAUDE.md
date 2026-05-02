---

# Library Management System — Claude.md

## Project Overview
Full-stack Java desktop app. Spring Boot 3 REST backend + JavaFX 21 frontend + MySQL 8 database.

## Tech Stack
- **Backend:** Spring Boot 3.1.5, JPA/Hibernate, Spring Security, JJWT 0.12.3, Flyway, Lombok
- **Frontend:** JavaFX 21, Gson, SLF4J/Logback
- **Database:** MySQL 8 (MySQL 9.6 on dev machine)
- **Build:** Maven (separate modules — `backend/` and `javafx-client/`)
- **Java:** 21 (Homebrew temurin@21)

## Local Dev Setup
- Backend runs on `http://localhost:8080`
- JavaFX client connects to backend via `ApiClient.java`
- MySQL: `library_db`, user: `library_user`, password: `Hanuman@1`
- Default admin login: `admin` / `admin123`
- Start backend: `cd backend && mvn spring-boot:run`
- Start frontend: `cd javafx-client && mvn javafx:run`
- Kill port: `lsof -ti:8080 | xargs kill -9`

## Architecture
```
backend/src/main/java/com/library/
├── model/          — JPA entities (Book, Member, Transaction, User)
├── repository/     — Spring Data JPA interfaces
├── service/        — Business logic (@Transactional)
├── controller/     — REST endpoints
├── security/       — JWT (JwtUtil, JwtFilter, SecurityConfig)
├── dto/            — Request/Response bodies
└── exception/      — GlobalExceptionHandler, custom exceptions

javafx-client/src/main/java/com/library/client/
├── MainApp.java            — Entry point, scene switching
├── api/ApiClient.java      — All HTTP calls (singleton, background Tasks)
├── session/SessionManager  — JWT token storage
├── controller/             — JavaFX screen controllers
├── model/                  — Client-side POJOs (Book, Member, Transaction)
└── resources/fxml/         — Screen layouts
```

## REST API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Returns JWT token |
| GET | `/api/books` | List all books |
| POST | `/api/books` | Add book |
| PUT | `/api/books/{id}` | Update book |
| DELETE | `/api/books/{id}` | Delete book |
| GET | `/api/books/search?q=` | Search books |
| GET | `/api/members` | List members |
| POST | `/api/members` | Register member |
| PUT | `/api/members/{id}` | Update member |
| DELETE | `/api/members/{id}` | Delete member |
| GET | `/api/members/{id}/history` | Borrow history |
| POST | `/api/borrow` | Borrow book |
| POST | `/api/return/{transactionId}` | Return book |
| GET | `/api/transactions` | All transactions |

## Business Rules
- Max 3 books per member at a time (`BorrowLimitException`)
- Cannot delete a book that is currently borrowed
- Cannot delete a member with active borrows
- Book availability tracked via `available` boolean on Book entity
- `Transaction.Status` enum: `BORROWED` / `RETURNED`

## Known Issues / In Progress
- `GET /api/members/{id}/history` returning empty `[]` — fix in progress (changed `findByMember()` to `findByMemberId()` JPQL query in `TransactionRepository`)
- `Transaction` relations changed from `LAZY` to `EAGER` fetch to fix serialization

## Bugs Fixed This Session
1. JavaFX Task double-creation bug — all controllers were creating Task twice, handlers never fired
2. Backend `POST /return/{transactionId}` was returning empty DTO — now calls `TransactionService.returnByTransactionId()`
3. Backend `GET /members/{id}/history` was returning `List.of()` — now wired to `TransactionService.getHistory()`
4. Duplicate `/api/members/{id}/history` endpoint in `TransactionController` — removed
5. Ambiguous handler conflict between `MemberController` and `TransactionController`
6. BCrypt hash mismatch for default admin user in V4 Flyway migration

## What's Left
- Fix member history returning empty list (JPQL query fix applied, needs verification)
- Add Book dialog ✅
- Register Member dialog ✅
- View History dialog ✅ (UI works, data fix pending)
- Transaction status filter ✅
- Commit and push all pending backend fixes

## Flyway Notes
- If migration fails: `UPDATE flyway_schema_history SET success=1 WHERE version='4';`
- If checksum mismatch: `DELETE FROM flyway_schema_history;` then restart