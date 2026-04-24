# Library Management System

A comprehensive Java-based library management solution with a Spring Boot REST API backend and JavaFX desktop frontend.

## Project Structure

```
library-management-system/
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
│
├── javafx-client/             # JavaFX Desktop Frontend
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
│
└── README.md (this file)
```

## Technology Stack

### Backend
- **Framework**: Spring Boot 3
- **Language**: Java 21
- **Database**: MySQL 8
- **ORM**: Hibernate (JPA)
- **Authentication**: JWT (JJWT)
- **Build**: Maven
- **Schema Management**: Flyway

### Frontend
- **Framework**: JavaFX 21
- **Layout**: FXML
- **HTTP Client**: java.net.http.HttpClient
- **Build**: Maven

## Features

### Core Functionality
- **Book Management**: Add, update, delete, search books
- **Member Management**: Register members, manage member records
- **Borrow/Return**: Handle book borrowing with 3-book limit per member
- **Transaction History**: Full audit trail of all borrowing activities
- **Authentication**: JWT-based login for role-based access

### Business Rules
1. Members can borrow maximum 3 books at a time
2. Books must be available to borrow
3. Return operations only for active borrows
4. Unique constraints on IDs and emails
5. Role-based access control (Admin/Librarian)

## Quick Start

### Prerequisites
- Java 21 (JDK)
- MySQL 8.0+
- Maven 3.6+
- Git

### Backend Setup

```bash
cd backend

# Create MySQL database
mysql -u root -p
```

Then in MySQL:
```sql
CREATE DATABASE library_db;
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON library_db.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
```

Build and run:
```bash
mvn spring-boot:run
```

API runs on: http://localhost:8080

**Default Admin Account**:
- Username: `admin`
- Password: `admin123`

### Frontend Setup

```bash
cd javafx-client

# Run the JavaFX application
mvn javafx:run
```

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication
```
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Books Endpoints
```
GET    /books              - List all books
POST   /books              - Add new book
GET    /books/{id}         - Get book by ID
GET    /books/search?q=    - Search books
PUT    /books/{id}         - Update book
DELETE /books/{id}         - Delete book
```

### Members Endpoints
```
GET    /members            - List all members
POST   /members            - Register member
GET    /members/{id}       - Get member by ID
PUT    /members/{id}       - Update member
DELETE /members/{id}       - Delete member
GET    /members/{id}/history - Get borrow history
```

### Transactions Endpoints
```
POST   /borrow             - Borrow book
POST   /return/{id}        - Return book
GET    /transactions       - List all transactions
```

## Database Schema

### books table
```sql
- id (PK)
- book_id (unique)
- title
- author
- isbn
- available (boolean)
- created_at
```

### members table
```sql
- id (PK)
- member_id (unique)
- name
- email (unique)
- phone
- borrowed_count
- registered_at
```

### transactions table
```sql
- id (PK)
- record_id (unique)
- book_id (FK)
- member_id (FK)
- borrow_date
- return_date
- status (BORROWED/RETURNED)
```

### users table
```sql
- id (PK)
- username (unique)
- password_hash
- role (ADMIN/LIBRARIAN)
- created_at
```

## Project Structure Details

### Backend (`backend/`)
- **model/**: JPA entities (Book, Member, Transaction, User)
- **repository/**: Spring Data JPA interfaces
- **service/**: Business logic layer
- **controller/**: REST API endpoints
- **security/**: JWT authentication (JwtUtil, JwtFilter, SecurityConfig)
- **dto/**: Request/response data transfer objects
- **exception/**: Custom exception handling
- **resources/**: Configuration and database migrations

### Frontend (`javafx-client/`)
- **controller/**: JavaFX screen controllers
- **api/**: HTTP API client
- **session/**: JWT token management
- **model/**: Client-side data models
- **resources/fxml/**: FXML layout files
- **resources/css/**: Styling

## Git Workflow

```
main (upstream)
├── backend module (Spring Boot API)
├── javafx-client module (JavaFX frontend)
└── Supporting files
```

## License

MIT

## Support

For setup issues:
1. Ensure Java 21 is installed: `java -version`
2. Ensure MySQL is running: `mysql -u root -p`
3. Check backend README: `backend/README.md`
4. Check frontend README: `javafx-client/README.md`

## Next Steps

1. Set up MySQL database (see Backend Setup)
2. Build and run backend: `cd backend && mvn spring-boot:run`
3. Build and run frontend: `cd javafx-client && mvn javafx:run`
4. Login with admin credentials
5. Test core features (add books, register members, borrow books)
