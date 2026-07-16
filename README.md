# Professional E-commerce Web App (profEcomWebApp)

A production-grade, RESTful e-commerce backend built with **Spring Boot 3** and **Java 21**. It provides a complete set of APIs for authentication, product/category management, shopping carts, addresses, and order placement, all secured with **JWT (JSON Web Token)** authentication and role-based access control.

> Project: `com.springproject:profEcomWebApp` — _Professional Ecommerce Website_  
> Author: Md Ashraful Alam — [md.ashraful.alam1@g.bracu.ac.bd](mailto:md.ashraful.alam1@g.bracu.ac.bd) · [GitHub](https://github.com/alam265)

---

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Authentication & Security](#authentication--security)
- [API Reference](#api-reference)
  - [Auth](#auth-api)
  - [Categories](#categories-api)
  - [Products](#products-api)
  - [Cart](#cart-api)
  - [Addresses](#addresses-api)
  - [Orders](#orders-api)
- [Pagination](#pagination)
- [Error Handling](#error-handling)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Testing](#testing)
- [License](#license)

---

## Features

- **JWT-based authentication** with HTTP-only cookies and bearer-token support.
- **Role-based access control** — `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`.
- **User registration & login** with BCrypt password hashing.
- **Category management** — create, list (paginated), update, and delete.
- **Product management** — CRUD operations, image upload, category-based and keyword search, pagination and sorting.
- **Shopping cart** — add/remove/update items, per-user cart, automatic total recalculation, stock validation.
- **Address management** — users can manage multiple shipping addresses.
- **Order placement** — converts a cart into an order, creates order items, records payment details, decrements stock, and clears the cart.
- **Global exception handling** with consistent error responses and bean-validation messages.
- **OpenAPI / Swagger UI** documentation.
- **ModelMapper** integration for clean entity ↔ DTO mapping.

---

## Technology Stack

| Category        | Technology |
|-----------------|------------|
| Language        | Java 21 |
| Framework       | Spring Boot 3.5.6 |
| Web             | Spring MVC (`spring-boot-starter-web`) |
| Persistence     | Spring Data JPA (`spring-boot-starter-data-jpa`) |
| Database        | PostgreSQL (H2 available but commented out) |
| Security        | Spring Security + JJWT (`io.jsonwebtoken` 0.12.6) |
| Validation      | `spring-boot-starter-validation` (Jakarta Bean Validation) |
| Mapping         | ModelMapper 3.2.4 |
| API Docs        | springdoc-openapi-starter-webmvc-ui 2.8.14 (Swagger UI) |
| Build Tool      | Apache Maven (with Maven Wrapper) |
| Utilities       | Lombok, UUID-based file naming |

---

## Architecture

The application follows a standard **layered Spring Boot architecture**:

```
Client (HTTP/JSON)
        │
        ▼
┌──────────────────┐
│   Controller     │   (@RestController) — REST endpoints, request validation
└────────┬─────────┘
         ▼
┌──────────────────┐
│     Service      │   (@Service) — business logic, transactions
└────────┬─────────┘
         ▼
┌──────────────────┐
│    Repository    │   (Spring Data JPA) — data access
└────────┬─────────┘
         ▼
┌──────────────────┐
│     Database     │   PostgreSQL
└─────────────────┘

Cross-cutting:
  • Security Filter Chain → JWT AuthTokenFilter → JwtUtils
  • Global @RestControllerAdvice exception handling
  • ModelMapper for DTO conversion
```

State is **stateless** (`SessionCreationPolicy.STATELESS`); every request is authenticated via a JWT carried in a cookie or the `Authorization: Bearer` header.

---

## Project Structure

```
src/main/java/com/springproject/profEcomWebApp/
├── ProfEcomWebAppApplication.java      # Spring Boot entry point
├── config/
│   ├── AppConfig.java                 # ModelMapper bean
│   ├── AppConstants.java              # Default pagination/sort constants
│   └── SwaggerConfig.java             # OpenAPI bean (JWT security scheme)
├── controller/
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ProductController.java
│   ├── CartController.java
│   ├── AddressController.java
│   └── OrderController.java
├── service/
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   ├── AddressService.java
│   ├── OrderService.java
│   └── FileService.java
├── repository/                         # Spring Data JPA repositories
│   ├── UserRepository, RoleRepository
│   ├── CategoryRepo, ProductRepository
│   ├── CartRepository, CartItemRepository
│   ├── OrderRepository, OrderItemRepository
│   ├── PaymentRepository, AddressRepository
├── model/                              # JPA entities
│   ├── User, Role, AppRole
│   ├── Category, Product
│   ├── Cart, CartItem
│   ├── Order, OrderItem, Payment, Address
├── payload/                            # DTOs and response wrappers
├── security/
│   ├── WebSecurityConfig.java
│   ├── jwt/  (JwtUtils, AuthTokenFilter, AuthEntryPointJwt)
│   ├── request/ (LoginRequest, SignupRequest)
│   ├── response/ (UserInfoResponse, MessageResponse)
│   └── services/ (UserDetailsImpl, UserDetailsServiceImpl)
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── APIExceptionHandler.java
│   └── MyGlobalExceptionHandler.java
└── util/
    └── AuthUtil.java                   # Helpers to resolve the logged-in user

src/main/resources/
└── application.properties

images/                                 # Uploaded product images (configurable)
```

---

## Domain Model

Entities (JPA) and their key relationships:

| Entity | Description | Key Relations |
|--------|-------------|---------------|
| `User` | Application user | Many-to-Many `Role`; One-to-One `Cart`; One-to-Many `Address`, `Product` |
| `Role` | Role (enum-backed: `ROLE_USER`, `ROLE_SELLER`, `ROLE_ADMIN`) | Many-to-Many `User` |
| `Category` | Product category | One-to-Many `Product` |
| `Product` | Sellable item (price, discount, special price, image) | Many-to-One `Category`, `User` (seller); One-to-Many `CartItem` |
| `Cart` | Per-user shopping cart | One-to-One `User`; One-to-Many `CartItem`; `totalPrice` |
| `CartItem` | Line item in a cart | Many-to-One `Cart`, `Product` |
| `Order` | Placed order | One-to-One `Payment`; Many-to-One `Address`; One-to-Many `OrderItem` |
| `OrderItem` | Line item in an order | Many-to-One `Order`, `Product` |
| `Payment` | Payment details (method, gateway info) | One-to-One `Order` |
| `Address` | Shipping address | Many-to-One `User` |

The schema is generated/updated automatically via `spring.jpa.hibernate.ddl-auto=update`.

---

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+** (or use the included Maven Wrapper `mvnw`)
- **PostgreSQL** server (default database `ecommerce` on `localhost:5432`)

---

## Getting Started

1. **Clone the repository**

   ```bash
   git clone <your-repo-url>
   cd profEcomWebApp
   ```

2. **Create the PostgreSQL database**

   ```sql
   CREATE DATABASE ecommerce;
   ```

   Ensure the credentials in `application.properties` match your setup
   (default user `postgres`, password `1111`).

3. **Build the project**

   ```bash
   ./mvnw clean install
   # or, if Maven is installed globally:
   mvn clean install
   ```

4. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   # or run the packaged jar:
   java -jar target/profEcomWebApp-0.0.1-SNAPSHOT.jar
   ```

5. On startup the app prints `Server is Running....` and **seeds default data** (see below).

---

## Configuration

All configuration lives in `src/main/resources/application.properties`. The key
properties (do **not** commit real secrets — use environment variables or a
secrets manager in production):

| Property | Purpose |
|----------|---------|
| `spring.application.name` | Application name (`profEcomWebApp`) |
| `spring.datasource.url` | JDBC URL for the PostgreSQL database |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `spring.jpa.hibernate.ddl-auto` | Schema management mode (e.g. `update`) |
| `spring.jpa.database-platform` | Hibernate dialect (PostgreSQL) |
| `project.image` | Directory for uploaded product images |
| `spring.app.jwtSecret` | Secret key used to sign/verify JWTs |
| `spring.app.jwtExpirationMs` | JWT expiration time in milliseconds |
| `spring.app.jwtCookieName` | Name of the JWT cookie |

> ⚠️ **Security note:** The datasource password and JWT secret must NOT be
> hardcoded in `application.properties` for production. Move them to environment
> variables or a secrets manager and use a strong, unique JWT secret.

### Seeded Data (CommandLineRunner)

On first start, `WebSecurityConfig.initData` creates the three roles and three demo users:

| Username | Password     | Roles |
|----------|--------------|-------|
| `user1`  | `password1`  | ROLE_USER |
| `seller1`| `password2`  | ROLE_SELLER |
| `admin`  | `adminPass`  | ROLE_USER, ROLE_SELLER, ROLE_ADMIN |

---

## Authentication & Security

- **Stateless JWT security** — CSRF disabled, no HTTP sessions.
- **Token transport** — JWT is issued as an HTTP-only (path `/api`) cookie on sign-in, and is also accepted via the `Authorization: Bearer <token>` header.
- **Password storage** — BCrypt via `BCryptPasswordEncoder`.
- **Filter chain** — `AuthTokenFilter` runs before `UsernamePasswordAuthenticationFilter`, validates the JWT, and populates the `SecurityContext`.
- **Public endpoints** (permitAll):
  - `/api/auth/**` (signin, signup, signout, user info)
  - `/api/admin/**` (note: currently permitAll — tighten for production)
  - `/images/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/h2-console/**`
- All other endpoints require a valid JWT.

---

## API Reference

Base URL: `http://localhost:8080/api`

### Auth API (`/api/auth`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/signin` | Public | Authenticate user; returns JWT cookie + user info |
| POST | `/api/auth/signup` | Public | Register a new user (optional roles: `admin`, `seller`) |
| GET  | `/api/auth/username` | Public | Return the current authenticated username |
| GET  | `/api/auth/user` | Auth | Return the current user's id, username, and roles |
| POST | `/api/auth/signout` | Public | Clear the JWT cookie (sign out) |

**Signin request body**
```json
{ "username": "user1", "password": "password1" }
```

**Signup request body**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret",
  "roles": ["user"]
}
```

### Categories API (`/api`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET    | `/api/public/categories` | Public | List categories (paginated, sorted) |
| POST   | `/api/public/categories` | Public | Create a category |
| PUT    | `/api/public/categories/{categoryId}` | Public | Update a category |
| DELETE | `/api/admin/categories/{categoryId}` | Public | Delete a category |

### Products API (`/api`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST   | `/api/admin/categories/{categoryId}/product` | Public | Add a product to a category |
| GET    | `/api/public/products` | Public | List all products (paginated, sorted) |
| GET    | `/api/public/categories/{categoryId}/product` | Public | List products by category |
| GET    | `/api/public/products/keyword/{keyword}` | Public | Search products by keyword |
| PUT    | `/api/public/products/{productId}` | Public | Update a product |
| DELETE | `/api/public/products/{productId}` | Public | Delete a product |
| PUT    | `/api/products/{productId}/image` | Public | Upload a product image (`multipart/form-data`, field `image`) |

### Cart API (`/api`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST   | `/api/carts/products/{productId}/quantity/{quantity}` | Auth | Add a product to the user's cart |
| GET    | `/api/carts` | Auth | List all carts |
| GET    | `/api/carts/user/cart` | Auth | Get the current user's cart |
| PUT    | `/api/cart/product/{productId}/quantity/{operation}` | Auth | Update quantity (`operation=delete` decreases) |
| DELETE | `/api/carts/{cartId}/product/{productId}` | Auth | Remove a product from a cart |

### Addresses API (`/api`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST   | `/api/addresses` | Auth | Create an address for the current user |
| GET    | `/api/addresses` | Auth | List all addresses |
| GET    | `/api/addresses/{addressId}` | Auth | Get a specific address |
| GET    | `/api/users/addresses` | Auth | List the current user's addresses |
| PUT    | `/api/addresses/{addressId}` | Auth | Update an address |
| DELETE | `/api/addresses/{addressId}` | Auth | Delete an address |

### Orders API (`/api`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST   | `/api/order/users/payments/{paymentMethod}` | Auth | Place an order from the user's cart |

**Order request body (`OrderRequestDTO`)**
```json
{
  "addressId": 1,
  "pgName": "Stripe",
  "pgPaymentId": "pay_123",
  "pgStatus": "SUCCESS",
  "pgResponseMessage": "Payment completed"
}
```

Placing an order will: validate the cart, create the `Order` and `Payment`, copy cart items into `OrderItem`s, decrement product stock, and clear the cart.

---

## Pagination

List endpoints accept these optional query parameters (defaults defined in `AppConstants`):

| Param | Default | Description |
|-------|---------|-------------|
| `pageNumber` | `0` | Zero-based page index |
| `pageSize`   | `50` | Number of items per page |
| `sortBy`     | `categoryId` (categories) / `productId` (products) | Field to sort by |
| `sortDirection` / `sortOrder` | `asc` | Sort direction (`asc`/`desc`) |

**Example**
```
GET /api/public/products?pageNumber=0&pageSize=10&sortBy=price&sortOrder=desc
```

Responses are wrapped in `CategoryResponse` / `ProductResponse` containing `content`, `pageNumber`, `pageSize`, `totalElements`, and `totalPages`.

---

## Error Handling

A global `@RestControllerAdvice` (`MyGlobalExceptionHandler`) handles:

- **Bean validation errors** → `400 Bad Request` with a map of field → message.
- **`ResourceNotFoundException`** → `404 Not Found` with `{ "message": "...", "status": false }`.
- **`APIExceptionHandler`** (business rule violations, e.g. duplicate product, empty cart) → `400 Bad Request`.

Example error body:
```json
{ "message": "Product Already Exists", "status": false }
```

---

## API Documentation (Swagger)

Interactive API docs are served via Swagger UI (springdoc-openapi):

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

The OpenAPI definition declares a **Bearer JWT** security scheme so you can authorize requests directly from the Swagger UI.

---

## Testing

The project includes a Spring Boot test context:

```bash
./mvnw test
```

`ProfEcomWebAppApplicationTests` verifies the application context loads successfully. `spring-security-test` is available for securing controller tests.

---

## License

This project is provided as-is for educational/demo purposes.
