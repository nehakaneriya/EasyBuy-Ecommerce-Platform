<<<<<<< HEAD
# 🛒 EasyBuy — Full Stack E-Commerce Platform

> A production-ready, microservices-based e-commerce platform built with **Java Spring Boot** (backend) and **React + TypeScript** (frontend).

---

## 📌 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Frontend](#frontend)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Environment Setup](#environment-setup)
- [Screenshots](#screenshots)

---

## Overview

**EasyBuy** is a complete e-commerce solution covering the full shopping lifecycle:

- User registration, login, and profile management
- Product catalog with categories, images, and reviews
- Shopping cart with real-time total calculation
- Order placement, tracking, and cancellation
- Inventory management with low-stock alerts
- Payment processing (COD + Online)
- Event-driven notifications via Apache Kafka
- AI-powered assistant layer (Spring AI + OpenAI)
- Admin dashboard for managing products, orders, inventory, and users

---

## Tech Stack

### Backend
| Technology | Version | Usage |
|---|---|---|
| Java | 21 (LTS) | All microservices |
| Spring Boot | 4.0.6 | Core framework |
| Spring Cloud | 2025.1.1 | Eureka, Config, Gateway, OpenFeign |
| Spring AI | 2.0.0-M5 | AI Service (OpenAI) |
| Apache Kafka | Latest (KRaft) | Async event streaming |
| Spring Cloud Gateway | WebFlux | API Gateway & routing |
| Netflix Eureka | — | Service discovery |
| Resilience4j | — | Circuit breaker, retry |
| Redis | — | Rate limiting |
| MySQL | — | USER, CART, INVENTORY, PAYMENT, NOTIFICATION |
| PostgreSQL | — | PRODUCT Service |
| RabbitMQ | — | Spring Cloud Bus config refresh |
| ImageKit SDK | 3.0.0 | Product image CDN uploads |
| Docker / Docker Compose | — | Kafka, Kafka UI |

### Frontend
| Technology | Usage |
|---|---|
| React 18 + TypeScript | Core framework |
| Vite 8 | Build tool |
| TailwindCSS v4 | Styling |
| React Router v7 | Routing |
| TanStack React Query | Server state management |
| Zustand | Global state (auth, cart) |
| React Hook Form + Zod | Forms & validation |
| Axios | HTTP client |
| Lucide React | Icons |
| React Hot Toast | Notifications |

---

## Architecture

```
Client (React App — port 3000)
         │
         ▼
┌─────────────────────────────────────┐
│     API Gateway  (port 1010)        │
│  Rate Limiting · Circuit Breaker    │
│  Retry Logic · Route Matching       │
└────────────┬────────────────────────┘
             │ Eureka Service Discovery
             ▼
┌────────────────────────────────────────────────────┐
│         SERVICE REGISTRY (Eureka — port 8761)      │
└────────────────────────────────────────────────────┘
             │ Centralized Config
             ▼
┌────────────────────────────────────────────────────┐
│       CONFIG SERVER (port 8888)                    │
│  Git-backed · AES Encrypted · Live Refresh         │
└────────────────────────────────────────────────────┘

Microservices:
  USER_SERVICE · PRODUCT_SERVICE · CART-ORDER_SERVICE
  INVENTORY_SERVICE · PAYMENT_SERVICE
  NOTIFICATION_SERVICE · AI_SERVICE

Event Bus: Apache Kafka (port 9092)
  Topic: "order-topic"
  Kafka UI: port 7575
```

---

## Microservices

### 1. USER_SERVICE
- User registration, login, profile management
- Role management (ROLE_USER / ROLE_ADMIN)
- Password change, profile image update
- **Database:** MySQL

### 2. PRODUCT_SERVICE
- Product CRUD with categories and reviews
- Multi-image upload via ImageKit CDN
- Live config refresh via Spring Cloud Bus + RabbitMQ
- **Database:** PostgreSQL

### 3. CART-ORDER_SERVICE
- Shopping cart management (add, update, remove, clear)
- Order checkout, order history, order cancellation
- Publishes `OrderEvent` to Kafka on checkout
- Calls INVENTORY_SERVICE to reserve/release stock
- **Database:** MySQL

### 4. INVENTORY_SERVICE
- Warehouse stock tracking (available + reserved)
- Low-stock detection
- Pessimistic locking for concurrent reserve/release
- **Database:** MySQL

### 5. PAYMENT_SERVICE
- Planned: Kafka consumer for payment processing
- **Database:** MySQL

### 6. NOTIFICATION_SERVICE
- Planned: Kafka consumer for order notifications (email/SMS)
- **Database:** MySQL

### 7. AI_SERVICE
- Spring AI + OpenAI integration
- Planned: Product recommendations, smart search, chatbot
- **Database:** MySQL

### 8. API-GATEWAY
- Single entry point (port 1010)
- Redis rate limiting (1 req/sec per user)
- Resilience4j circuit breaker
- Routes: `/user-service/**`, `/product-service/**`, `/cart-order-service/**`

### 9. SERVICE_REGISTRY
- Netflix Eureka Server (port 8761)

### 10. CONFIG-SERVER
- Git-backed centralized config (port 8888)
- AES property encryption

---

## Frontend

Located in `FRONTEND/easybuy/`

### Pages
| Route | Page | Access |
|---|---|---|
| `/` | Home — Hero, Categories, Deals, Featured Products | Public |
| `/products` | Product listing with filters & sort | Public |
| `/products/:id` | Product detail with reviews | Public |
| `/login` | Login | Public |
| `/register` | Register | Public |
| `/cart` | Shopping cart | Logged in |
| `/checkout` | Checkout & payment | Logged in |
| `/orders` | Order history | Logged in |
| `/orders/:id` | Order detail & tracking | Logged in |
| `/profile` | User profile & password | Logged in |
| `/admin` | Admin dashboard | Admin only |
| `/admin/products` | Product management (CRUD) | Admin only |
| `/admin/categories` | Category management | Admin only |
| `/admin/inventory` | Inventory & stock control | Admin only |
| `/admin/orders` | Order management | Admin only |
| `/admin/users` | User management | Admin only |

---

## Getting Started

### Prerequisites
- Java 21+
- Node.js 18+
- Docker Desktop
- MySQL & PostgreSQL running
- Redis running (port 6379)
- RabbitMQ running (for PRODUCT_SERVICE config refresh)

### 1. Start Infrastructure (Docker)
```bash
cd BACKEND/docker
docker-compose up -d
```
This starts Kafka (port 9092) and Kafka UI (port 7575).

### 2. Start Backend Services (in order)

```bash
# 1. Service Registry (Eureka)
cd BACKEND/SERVICE_REGISTRY
./mvnw spring-boot:run

# 2. Config Server
cd BACKEND/CONFIG-SERVER
./mvnw spring-boot:run

# 3. All microservices (in any order)
cd BACKEND/USER_SERVICE && ./mvnw spring-boot:run
cd BACKEND/PRODUCT_SERVICE && ./mvnw spring-boot:run
cd BACKEND/CART-ORDER_SERVICE && ./mvnw spring-boot:run
cd BACKEND/INVENTORY_SERVICE && ./mvnw spring-boot:run

# 4. API Gateway (last)
cd BACKEND/API-GATEWAY
./mvnw spring-boot:run
```

### 3. Start Frontend

```bash
cd FRONTEND/easybuy
npm install
npm run dev
```

App runs at: **http://localhost:3000**

---

## API Endpoints

### User Service (`/user-service/api/users`)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/register` | Register new user |
| GET | `/` | Get all users (paginated) |
| GET | `/{userId}` | Get user by ID |
| GET | `/email/{email}` | Get user by email |
| PUT | `/{userId}` | Update user |
| DELETE | `/{userId}` | Delete user |
| PATCH | `/{userId}/change-password` | Change password |
| PATCH | `/{userId}/role` | Change role |

### Product Service (`/product-service/api/products`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Get all products (paginated) |
| POST | `/` | Create product |
| GET | `/{productId}` | Get product by ID |
| PUT | `/{productId}` | Update product |
| DELETE | `/{productId}` | Delete product |
| POST | `/{productId}/images` | Upload images |
| POST | `/{productId}/reviews` | Add review |

### Cart-Order Service
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/carts/{userId}` | Get user's cart |
| POST | `/api/carts/{userId}/items` | Add to cart |
| PUT | `/api/carts/{userId}/items/{productId}` | Update cart item |
| DELETE | `/api/carts/{userId}/items/{productId}` | Remove cart item |
| POST | `/api/orders/checkout/{userId}` | Place order |
| GET | `/api/orders/user/{userId}` | Get user orders |
| DELETE | `/api/orders/{orderId}` | Cancel order |

---

## Environment Setup

### Make yourself Admin
After registering, run in MySQL (USER_SERVICE database):
```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'your@email.com';
```
Then login → click your name → **Admin Panel**

---

## Project Structure

```
EASY_BUY/
├── BACKEND/
│   ├── API-GATEWAY/
│   ├── SERVICE_REGISTRY/
│   ├── CONFIG-SERVER/
│   ├── COMMON_SERVICE/
│   ├── USER_SERVICE/
│   ├── PRODUCT_SERVICE/
│   ├── CART-ORDER_SERVICE/
│   ├── INVENTORY_SERVICE/
│   ├── PAYMENT_SERVICE/
│   ├── NOTIFICATION_SERVICE/
│   ├── AI_SERVICE/
│   └── docker/
└── FRONTEND/
    └── easybuy/
        ├── src/
        │   ├── pages/
        │   ├── components/
        │   ├── services/
        │   ├── store/
        │   ├── types/
        │   └── utils/
        └── package.json
```

---

## Author

**Neha Kaneriya**
- GitHub: [@nehakaneriya](https://github.com/nehakaneriya)

---

## License

This project is for educational purposes.
=======
# EasyBuy-Ecommerce-Platform
A scalable E-Commerce backend built using Spring Boot Microservices architecture with API Gateway, Eureka Service Discovery, Config Server, Kafka, Redis, RabbitMQ, JWT Authentication, and MySQL.
>>>>>>> 6b94722f86abaf484a4271e8f7d645ea93078f6b
