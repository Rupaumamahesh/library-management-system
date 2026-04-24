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

