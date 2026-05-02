# Library Management System

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen?logo=springboot&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-blue?logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.6%2B-red?logo=apachemaven&logoColor=white)

A full-stack desktop library management application. The backend exposes a secured REST API (Spring Boot + JWT) and a JavaFX desktop client communicates with it over HTTP.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Prerequisites](#prerequisites)
3. [Setup](#setup)
4. [Running the Application](#running-the-application)
5. [Features](#features)
6. [REST API Reference](#rest-api-reference)
7. [Project Structure](#project-structure)
8. [Database Schema](#database-schema)
9. [Troubleshooting](#troubleshooting)

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend framework | Spring Boot | 3.1.5 |
| Data access | Spring Data JPA + Hibernate | (via Spring Boot) |
| Security | Spring Security + JJWT | 0.12.3 |
| Validation | Jakarta Bean Validation | (via Spring Boot) |
| Database migrations | Flyway | 9.22.1 |
| Database | MySQL | 8.0+ |
| Desktop client | JavaFX | 21.0.2 |
| HTTP client (JavaFX) | Java 11 HttpClient + Gson | 2.10.1 |
| Boilerplate reduction | Lombok | (via Spring Boot) |
| Build tool | Apache Maven | 3.6+ |
| Language | Java | 21 |

---

## Prerequisites

### Java 21

Both modules are compiled with `--release 21`. Verify your installation before building.

```bash
# macOS — install via Homebrew
brew install openjdk@21

# Add to your shell profile (~/.zshrc or ~/.bash_profile)
export JAVA_HOME=$(brew --prefix openjdk@21)
export PATH="$JAVA_HOME/bin:$PATH"

# Apply changes then verify
source ~/.zshrc
java -version   # should print: openjdk version "21.x.x"
```

### Maven 3.6+

```bash
brew install maven

# Verify
mvn -version   # should print: Apache Maven 3.x.x
```

### MySQL 8.0+

```bash
brew install mysql

# Start MySQL as a background service
brew services start mysql

# Run the secure installation wizard (set a root password when prompted)
mysql_secure_installation

# Verify
mysql --version   # should print: mysql  Ver 8.x.x
```

---

## Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd library-management-system
```

### 2. Create the database and user

Connect to MySQL as root:

```bash
mysql -u root -p
```

Run the following SQL commands:

```sql
-- Create the database
CREATE DATABASE library_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create a dedicated application user
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'your_password_here';

-- Grant all privileges on the application database
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

> Replace `your_password_here` with a strong password of your choice. You will use the same value in the next step.

### 3. Configure the database password

Open `backend/src/main/resources/application.properties` and set the password you chose above:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_db
spring.datasource.username=library_user
spring.datasource.password=your_password_here   # ← update this line
```

All other properties work for local development without changes.

### 4. Build both modules

Run from the **project root**. The `-DskipTests` flag skips the integration test during the initial build (the tests require the backend to be running).

```bash
# Build the backend
cd backend
mvn clean install -DskipTests

# Build the JavaFX client
cd ../javafx-client
mvn clean install -DskipTests

cd ..
```

Flyway will automatically create all four database tables the first time the backend starts — no manual SQL import is required.

---

## Running the Application

### Start the backend

The backend must be running before you launch the desktop client.

```bash
cd backend
mvn spring-boot:run
```

You should see output ending with:

```
Started LibraryApplication in 2.x seconds (process running for x.xxx)
```

The API is now available at `http://localhost:8080`.

**Alternative — run the packaged fat JAR:**

```bash
# Build the JAR first (if you haven't already)
cd backend && mvn package -DskipTests

# Run it
java -jar backend/target/library-backend-1.0.jar
```

### Start the desktop client

Open a **second terminal** (leave the backend running in the first):

```bash
cd javafx-client
mvn javafx:run
```

The login window will appear after a few seconds.

### Default login credentials

| Username | Password | Role |
|----------|----------|------|
| `admin`  | `admin123` | ADMIN |

> The JWT token expires after **24 hours**. If you see authentication errors after a long session, simply log in again.

---

## Features

### Book Catalogue
- **Add book** — dialog form: Business ID (e.g. `B001`), title, author, optional ISBN
- **View all books** — paginated table with title, author, availability status (20 per page)
- **Search books** — by title or author (case-insensitive substring match)
- **Delete book** — only succeeds if the book is not currently borrowed

### Member Management
- **Register member** — dialog form: Member ID (e.g. `M001`), name, email, optional phone
- **View all members** — paginated table showing name, email, and current borrow count
- **View borrow history** — per-member dialog listing all borrow and return records
- **Delete member** — only succeeds if the member has no active borrows

### Borrow and Return Workflow
- **Borrow a book** — enter Member ID + Book ID; the system enforces a **3-book-per-member limit**
- **Return a book** — select any `BORROWED` transaction and click Return; sets the return date and marks the book as available
- **Transaction table** — full history with record ID, book/member business IDs, dates, and status
- **Status filter** — filter the transaction table live by All / BORROWED / RETURNED

### Dashboard
- Live counts: total books, total registered members, currently active borrows
- Recent transactions table loads automatically on login

### Security
- JWT-based authentication (HS256 algorithm, 24-hour expiry)
- All `/api/**` endpoints require a valid `Authorization: Bearer <token>` header (except `/api/auth/login`)
- Passwords stored as BCrypt hashes in the database

### Validation
- `@NotBlank`, `@Email`, and `@Size` annotations on all request DTOs
- Invalid requests return `422 Unprocessable Entity` with per-field error messages, for example:
  ```json
  {
    "status": 422,
    "error": "Validation Failed",
    "message": "bookId: Book ID is required, title: Title is required"
  }
  ```

---

## REST API Reference

All endpoints except login require:

```
Authorization: Bearer <token>
```

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/login` | Public | Authenticate; returns a JWT token |
| `GET` | `/api/books` | ADMIN | List all books (paginated; `?page=0&size=20`) |
| `POST` | `/api/books` | ADMIN | Add a new book |
| `GET` | `/api/books/{id}` | ADMIN | Get a single book by database ID |
| `PUT` | `/api/books/{id}` | ADMIN | Update book title, author, or ISBN |
| `DELETE` | `/api/books/{id}` | ADMIN | Delete a book (rejected if currently borrowed) |
| `GET` | `/api/books/search?q=` | ADMIN | Search books by title (case-insensitive) |
| `GET` | `/api/members` | ADMIN | List all members (paginated; `?page=0&size=20`) |
| `POST` | `/api/members` | ADMIN | Register a new member |
| `PUT` | `/api/members/{id}` | ADMIN | Update member name, email, or phone |
| `DELETE` | `/api/members/{id}` | ADMIN | Delete member (rejected if active borrows exist) |
| `GET` | `/api/members/{id}/history` | ADMIN | Get full borrow history for a member |
| `POST` | `/api/borrow` | ADMIN | Borrow a book (`{"memberId":"M001","bookId":"B001"}`) |
| `POST` | `/api/return/{transactionId}` | ADMIN | Return a borrowed book by transaction database ID |
| `GET` | `/api/transactions` | ADMIN | List all transactions (not paginated) |

### Example: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Response:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Example: Borrow a book

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X POST http://localhost:8080/api/borrow \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":"M001","bookId":"B001"}'
```

### Example: Paginated book list

```bash
curl "http://localhost:8080/api/books?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

Response shape:

```json
{
  "content": [
    { "id": 1, "bookId": "B001", "title": "Clean Code", "author": "Robert Martin", "available": false }
  ],
  "totalElements": 42,
  "totalPages": 9,
  "size": 5,
  "number": 0
}
```

---

## Project Structure

```
library-management-system/
│
├── backend/                                    Spring Boot REST API
│   ├── pom.xml                                 Dependencies: Spring Boot 3.1.5, Flyway, JJWT, Lombok
│   └── src/
│       ├── main/
│       │   ├── java/com/library/
│       │   │   ├── LibraryApplication.java              Entry point (@SpringBootApplication)
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java              POST /api/auth/login → JWT
│       │   │   │   ├── BookController.java              Full CRUD + search for /api/books
│       │   │   │   ├── MemberController.java            Full CRUD + history for /api/members
│       │   │   │   └── TransactionController.java       /api/borrow, /api/return, /api/transactions
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── BookService.java                 Add, search, update, delete; checks availability
│       │   │   │   ├── MemberService.java               Register, update, delete; checks active borrows
│       │   │   │   └── TransactionService.java          Borrow/return logic, 3-book limit, history query
│       │   │   │
│       │   │   ├── model/
│       │   │   │   ├── Book.java                        JPA entity → books table
│       │   │   │   ├── Member.java                      JPA entity → members table
│       │   │   │   ├── Transaction.java                 JPA entity → transactions table (EAGER fetch)
│       │   │   │   └── User.java                        JPA entity → users table
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   ├── BookRepository.java              findByBookId, searchByTitle, searchByAuthor
│       │   │   │   ├── MemberRepository.java            findByMemberId, existsByEmail
│       │   │   │   ├── TransactionRepository.java       findByMemberId (JPQL), countByMemberAndStatus
│       │   │   │   └── UserRepository.java              findByUsername
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── BookDTO.java                     @NotBlank bookId/title/author, @Size isbn
│       │   │   │   ├── MemberDTO.java                   @NotBlank memberId/name, @Email email
│       │   │   │   ├── BorrowRequest.java               @NotBlank memberId + bookId
│       │   │   │   ├── TransactionDTO.java              id, recordId, bookId, memberId, dates, status
│       │   │   │   ├── AuthRequest.java                 username + password (@NotBlank)
│       │   │   │   └── AuthResponse.java                token (String)
│       │   │   │
│       │   │   ├── security/
│       │   │   │   ├── SecurityConfig.java              Permits /api/auth/login; secures everything else
│       │   │   │   ├── JwtUtil.java                     generateToken(), validateToken(), extractUsername()
│       │   │   │   ├── JwtFilter.java                   OncePerRequestFilter — reads Bearer token
│       │   │   │   └── CustomUserDetailsService.java    Loads UserDetails from users table
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── GlobalExceptionHandler.java      @ControllerAdvice — 404 / 400 / 422 / 500
│       │   │       ├── ErrorResponse.java               { timestamp, status, error, message, path }
│       │   │       ├── ResourceNotFoundException.java   Thrown by services → 404
│       │   │       └── BorrowLimitException.java        Thrown when limit or rule violated → 400
│       │   │
│       │   └── resources/
│       │       ├── application.properties               DB URL, JPA, Flyway, JWT secret, logging
│       │       └── db/migration/
│       │           ├── V1__create_books.sql
│       │           ├── V2__create_members.sql
│       │           ├── V3__create_transactions.sql
│       │           └── V4__create_users.sql             Inserts default admin (admin / admin123)
│       │
│       └── test/java/com/library/
│           └── SmokeTest.java                           6 integration tests via TestRestTemplate
│
└── javafx-client/                              JavaFX Desktop Client
    ├── pom.xml                                 JavaFX 21.0.2, Gson 2.10.1, Logback
    └── src/main/
        ├── java/com/library/client/
        │   ├── MainApp.java                             Extends Application; loads login.fxml
        │   │
        │   ├── api/
        │   │   └── ApiClient.java                       Singleton; all HTTP calls using Java HttpClient
        │   │
        │   ├── controller/
        │   │   ├── LoginController.java                 Calls POST /auth/login; stores JWT in SessionManager
        │   │   ├── DashboardController.java             Loads counts (books, members, active borrows)
        │   │   ├── BookController.java                  Books table; Add Book dialog; Delete
        │   │   ├── MemberController.java                Members table; Register dialog; View History dialog
        │   │   └── TransactionController.java           Borrow form; Return selected; status filter combo
        │   │
        │   ├── model/
        │   │   ├── Book.java                            POJO deserialized from Page<BookDTO>.content[]
        │   │   ├── Member.java                          POJO deserialized from Page<MemberDTO>.content[]
        │   │   └── Transaction.java                     POJO deserialized from List<TransactionDTO>
        │   │
        │   └── session/
        │       └── SessionManager.java                  Stores JWT token for the current login session
        │
        └── resources/
            ├── fxml/
            │   ├── login.fxml                           Username / password fields + Login button
            │   ├── dashboard.fxml                       Stat labels + recent transactions TableView
            │   ├── books.fxml                           Books TableView + Add / Delete buttons
            │   ├── members.fxml                         Members TableView + Register / History / Delete
            │   └── transactions.fxml                    Borrow form + transactions table + filter ComboBox
            └── css/
                └── styles.css                           Application-wide JavaFX styling
```

---

## Database Schema

All tables are created automatically by Flyway on first startup — no manual schema import is needed.

### `books`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Database surrogate key |
| `book_id` | `VARCHAR(20)` | UNIQUE, NOT NULL | Business key — used in API and client (e.g. `B001`) |
| `title` | `VARCHAR(255)` | NOT NULL | |
| `author` | `VARCHAR(255)` | NOT NULL | |
| `isbn` | `VARCHAR(20)` | nullable | |
| `available` | `BOOLEAN` | NOT NULL, DEFAULT `TRUE` | Set to `FALSE` when borrowed; `TRUE` after return |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | |

### `members`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Database surrogate key |
| `member_id` | `VARCHAR(20)` | UNIQUE, NOT NULL | Business key (e.g. `M001`) |
| `name` | `VARCHAR(255)` | NOT NULL | |
| `email` | `VARCHAR(255)` | UNIQUE, NOT NULL | Validated with `@Email` at API boundary |
| `phone` | `VARCHAR(20)` | nullable | |
| `borrowed_count` | `INT` | NOT NULL, DEFAULT `0` | Incremented/decremented on borrow/return; capped at 3 |
| `registered_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | |

### `transactions`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Used as the `transactionId` in `POST /api/return/{id}` |
| `record_id` | `VARCHAR(20)` | UNIQUE, NOT NULL | Auto-generated business key (e.g. `R001`) |
| `book_id` | `BIGINT` | FK → `books.id` ON DELETE RESTRICT | Prevents deleting a book with transaction history |
| `member_id` | `BIGINT` | FK → `members.id` ON DELETE RESTRICT | Prevents deleting a member with transaction history |
| `borrow_date` | `DATE` | NOT NULL | Set on borrow |
| `return_date` | `DATE` | nullable | Set on return; `null` while book is still borrowed |
| `status` | `ENUM('BORROWED','RETURNED')` | NOT NULL, DEFAULT `BORROWED` | |

### `users`

| Column | Type | Constraints | Notes |
|--------|------|-------------|-------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `username` | `VARCHAR(100)` | UNIQUE, NOT NULL | |
| `password_hash` | `VARCHAR(255)` | NOT NULL | BCrypt hash; **never store plain text** |
| `role` | `ENUM('ADMIN','LIBRARIAN')` | NOT NULL, DEFAULT `LIBRARIAN` | |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT `NOW()` | |

The default admin row (`admin` / `admin123`) is inserted by `V4__create_users.sql`.

---

## Troubleshooting

### Port 8080 is already in use

```
Web server failed to start. Port 8080 was already in use.
```

Find and kill the process occupying the port:

```bash
# Find the PID
lsof -ti:8080

# Kill it
lsof -ti:8080 | xargs kill -9
```

Or change the backend port in `application.properties` and update `BASE_URL` in `ApiClient.java` to match:

```properties
# backend/src/main/resources/application.properties
server.port=8081
```

```java
// javafx-client/src/main/java/com/library/client/api/ApiClient.java
private static final String BASE_URL = "http://localhost:8081/api";
```

---

### Flyway checksum mismatch

```
Migration checksum mismatch for migration version X
```

This happens when a migration file is edited after it was already applied. To repair:

```sql
-- Connect as library_user or root
-- Option A — repair a single version (replace -1 with the new checksum from the error message)
UPDATE library_db.flyway_schema_history SET checksum = -1 WHERE version = 'X';

-- Option B — wipe all Flyway history and re-run every migration (loses existing data)
DELETE FROM library_db.flyway_schema_history;
```

Then restart the backend.

---

### Maven picks the wrong Java version

```
error: Source option 5 is no longer supported. Use 7 or later.
```

Maven is using a JDK older than 21. Force it explicitly:

```bash
# Set JAVA_HOME for the current shell session
export JAVA_HOME=$(brew --prefix openjdk@21)
mvn spring-boot:run

# Verify Maven sees the right JDK
mvn -version   # should show "Java version: 21"
```

To make this permanent, add the `export JAVA_HOME` line to your `~/.zshrc` (or `~/.bash_profile`).

---

### MySQL access denied

```
Access denied for user 'library_user'@'localhost' (using password: YES)
```

Test the credentials directly:

```bash
mysql -u library_user -p library_db
```

If the login fails, reset the password from the MySQL root account:

```sql
ALTER USER 'library_user'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

Then update `spring.datasource.password` in `application.properties` to match.

---

### Integration tests fail with connection errors

`SmokeTest` is a full integration test that starts a Spring context and connects to the real MySQL database. Before running `mvn test`, ensure:

1. MySQL is running (`brew services list | grep mysql` should show `started`).
2. The credentials in `application.properties` are correct.
3. The `library_db` database exists and `library_user` has access to it.

To run only the smoke test:

```bash
cd backend
mvn test -Dtest=SmokeTest
```
