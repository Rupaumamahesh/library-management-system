# Library Management System - Backend

Spring Boot 3 REST API backend for Library Management System.

## Prerequisites

- Java 21 (JDK)
- MySQL 8.0 or later
- Maven 3.6+

## Setup

### 1. Create MySQL Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE library_db;
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure JWT Secret (Optional)

Edit `src/main/resources/application.properties`:

```properties
jwt.secret=your-custom-secret-key-min-32-chars
```

### 3. Build & Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The API will start on http://localhost:8080

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login (returns JWT token)

### Books
- `GET /api/books` - List all books
- `POST /api/books` - Add new book
- `GET /api/books/{id}` - Get book by ID
- `GET /api/books/search?q=query` - Search books
- `PUT /api/books/{id}` - Update book
- `DELETE /api/books/{id}` - Delete book

### Members
- `GET /api/members` - List all members
- `POST /api/members` - Register member
- `GET /api/members/{id}` - Get member by ID
- `PUT /api/members/{id}` - Update member
- `DELETE /api/members/{id}` - Delete member
- `GET /api/members/{id}/history` - Get member's borrow history

### Transactions (Borrow/Return)
- `POST /api/borrow` - Borrow a book
- `POST /api/return/{transactionId}` - Return a book
- `GET /api/transactions` - List all transactions

## Default Admin Account

- Username: `admin`
- Password: `admin123`

## Database Schema

The system uses Flyway for automatic schema migrations. Migrations are applied automatically on startup:

- V1: Creates books table
- V2: Creates members table
- V3: Creates transactions table with foreign keys
- V4: Creates users table and seeds default admin user

## Architecture

```
backend/
├── src/main/java/com/library/
│   ├── LibraryApplication.java (Entry point)
│   ├── model/ (JPA entities)
│   ├── repository/ (Spring Data JPA)
│   ├── service/ (Business logic)
│   ├── controller/ (REST endpoints)
│   ├── security/ (JWT authentication)
│   ├── dto/ (Data transfer objects)
│   └── exception/ (Exception handling)
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/ (Flyway scripts)
└── pom.xml (Maven configuration)
```

## Business Rules

1. **Borrow Limit**: Each member can borrow max 3 books
2. **Availability**: Books must be available to borrow
3. **Return**: Only active borrows can be returned
4. **Unique Constraints**: Book IDs, Member IDs, and Emails are unique

## Testing with cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Add book (use token from login)
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId":"B001",
    "title":"The Alchemist",
    "author":"Paulo Coelho",
    "isbn":"9780061120084"
  }'
```

## License

MIT
