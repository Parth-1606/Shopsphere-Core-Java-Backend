# 🛍️ ShopSphere E-Commerce Backend

A production-grade, highly scalable e-commerce backend built with **Java 21**, **Spring Boot 3**, and **PostgreSQL**.

ShopSphere is designed using a **Feature-Based (Package-by-Feature)** structure to follow the principles of Clean Architecture. This "Monolith First" approach ensures the codebase is easy to navigate, maintain, and eventually decompose into microservices if scaling demands it.

---

## 🚀 Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Core programming language (utilizing records & new features). |
| **Spring Boot 3.2+** | Framework for REST APIs and auto-configuration. |
| **Spring Security 6** | Authentication, authorization, and stateless session management. |
| **PostgreSQL 16** | Primary relational database for enforcing ACID properties. |
| **Spring Data JPA / Hibernate** | ORM for database interactions and optimistic locking. |
| **Redis 7** | In-memory caching and JWT blacklist management. |
| **Docker Compose** | Local environment containerization. |
| **JJWT** | Secure JWT generation and validation. |

---

## 🏗️ Architecture & Features

### 1. Feature-Based Architecture
Unlike traditional MVC layered structures, ShopSphere groups code by feature (`auth`, `product`, `category`). This keeps high cohesion and ensures that a change to a single feature doesn't cause ripple effects across the entire application.

### 2. Implemented Sprints
- ✅ **Sprint 1: Environment Setup** - Dockerized PostgreSQL & Redis, application profiles (`dev`, `prod`, `test`).
- ✅ **Sprint 2: User Entity & Authentication** - `User` entity, BCrypt hashing, JWT generation, and `JwtAuthenticationFilter`.
- ✅ **Sprint 3: Refresh Tokens & Logout** - Token rotation, Redis-backed stateless JWT blacklist, and secure logout handlers.
- ✅ **Sprint 4: Category CRUD** - Hierarchical category management, `SlugUtil` for SEO, standard `PagedResponse` format.
- ✅ **Sprint 5: Product CRUD & Search** - Product catalog, `@ManyToOne` relationships, `@Version` Optimistic Locking, and dynamic JPA Specifications for robust searching/filtering.
- ⏳ **Sprint 6: Image Upload** - (Coming Soon)
- ⏳ **Sprint 7: Cart Management** - (Coming Soon)
- ⏳ **Sprint 8: Orders & Checkout** - (Coming Soon)

---

## 🛠️ Getting Started

### Prerequisites
- **Java 21** installed on your machine.
- **Docker** and **Docker Compose** installed.
- **Maven** (Optional, the wrapper `./mvnw` is included).

### 1. Spin up the Database & Cache
To run the required PostgreSQL database and Redis instance locally, simply use Docker Compose:
```bash
docker-compose up -d
```
This will start:
- PostgreSQL on port `5432`
- Redis on port `6379`

### 2. Run the Application
Start the Spring Boot application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```
By default, the application runs on **http://localhost:8080** and uses the `dev` profile (`application-dev.yml`).

### 3. Run Tests
The test environment uses an in-memory **H2 Database** for blazing-fast execution.
```bash
./mvnw clean test
```

---

## 🔐 API Security & Authentication
- All endpoints (except public reads and `/api/v1/auth/**`) require a valid `Authorization: Bearer <token>` header.
- The `JwtAuthenticationFilter` validates the token structure, expiration, and checks the **Redis Blacklist** to ensure the token wasn't revoked via a logout event.

---

## 📄 License
This project is proprietary and intended for internal/portfolio use.
