# PulseTech Backend

> Backend system for the **PulseTech** e-commerce platform — built with a **Microservices** architecture using Java 21 and Spring Boot.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Local Development](#local-development)
- [Environment Variables](#environment-variables)
- [API Reference](#api-reference)
- [Deploying to Render](#deploying-to-render)
- [Security Notes](#security-notes)

---

## Architecture Overview

```
                    ┌───────────────────────────────────────────────┐
                    │             Vercel (Frontend)                  │
                    │         pulse-tech-beryl.vercel.app            │
                    └──────────────────┬────────────────────────────┘
                                       │ /backend-api/*
                                       ▼
                    ┌───────────────────────────────────────────────┐
                    │          API Gateway  :8080                    │
                    │         (Spring Cloud Gateway)                 │
                    └──────┬───────────────┬───────────────┬────────┘
                           │               │               │
            /api/products  │   /api/auth   │  /api/orders  │
            /api/content   │   /api/users  │  /api/cart    │
                           ▼               ▼               ▼
             ┌─────────────────┐  ┌──────────────┐  ┌──────────────┐
             │ product-service │  │ auth-service │  │ order-service│
             │     :8081       │  │    :8082     │  │    :8083     │
             └────────┬────────┘  └──────┬───────┘  └──────┬───────┘
                      │                  │                  │
                      └──────────────────┴──────────────────┘
                                         │
                             ┌───────────▼──────────┐
                             │     MongoDB Atlas     │
                             │  Database: PulseTech  │
                             └──────────────────────┘
```

All client traffic enters through a single **API Gateway**, which routes requests to the appropriate downstream service. The frontend never communicates directly with any microservice.

---

## Tech Stack

| Component        | Technology                              |
|------------------|-----------------------------------------|
| Language         | Java 21                                 |
| Framework        | Spring Boot 4.1, Spring Cloud Gateway   |
| Database         | MongoDB Atlas                           |
| Cache            | Redis 7                                 |
| Build Tool       | Apache Maven (multi-module)             |
| Containerization | Docker + Docker Compose                 |
| Cloud Hosting    | Render (Docker-based deployment)        |

---

## Project Structure

```
backend/
├── api-gateway/          # Single entry point — routes all requests to downstream services
├── auth-service/         # Registration, login, email verification, wishlist, addresses
├── product-service/      # Products, categories, site content (banners, footer, navigation)
├── order-service/        # Orders, cart, coupons, and payment processing
├── Dockerfile            # Shared multi-stage Dockerfile (service selected via ARG)
├── docker-compose.yml    # Orchestrates all services + frontend for local development
├── pom.xml               # Parent POM (Maven multi-module aggregator)
├── .env.example          # Environment variable template
└── scripts/              # Utility scripts (MongoDB migration, etc.)
```

### Gateway Route Mapping

| Request Path        | Routed To        |
|---------------------|------------------|
| `/api/products/**`  | product-service  |
| `/api/content/**`   | product-service  |
| `/api/auth/**`      | auth-service     |
| `/api/users/**`     | auth-service     |
| `/api/orders/**`    | order-service    |
| `/api/cart/**`      | order-service    |

---

## Prerequisites

- **Docker Desktop** ≥ 24 (with Docker Compose v2)
- **JDK 21** *(only required for building/running without Docker)*
- **Maven 3.9+** *(only required for building/running without Docker)*
- A **MongoDB Atlas** account (free tier is sufficient)
- An email account with SMTP support (Gmail with App Password recommended)

---

## Local Development

### 1. Clone the repository

```bash
git clone https://github.com/VuIceTea/PulseTech-Backend.git
cd PulseTech-Backend
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Open `.env` and fill in your values:

```env
# MongoDB Atlas connection string
MONGODB_URI=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/PulseTech?retryWrites=true&w=majority

# SMTP configuration for email verification
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-google-app-password
MAIL_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=your-email@gmail.com

FRONTEND_URL=http://localhost:3000
```

> **Gmail App Password:** Generate one at *Google Account → Security → 2-Step Verification → App Passwords*.

### 3. Start the entire system

```bash
docker compose up --build
```

| Service     | URL                       |
|-------------|---------------------------|
| Website     | http://localhost:3000      |
| API Gateway | http://localhost:8080      |

### 4. Stop all services

```bash
docker compose down
```

---

## Environment Variables

| Variable              | Service                        | Required          | Description                                         |
|-----------------------|--------------------------------|-------------------|-----------------------------------------------------|
| `MONGODB_URI`         | auth, product, order           | ✅                | MongoDB Atlas connection string                     |
| `MAIL_HOST`           | auth-service                   | ✅                | SMTP host (e.g., `smtp.gmail.com`)                  |
| `MAIL_PORT`           | auth-service                   | ✅                | SMTP port (e.g., `587`)                             |
| `MAIL_USERNAME`       | auth-service                   | ✅                | SMTP email address                                  |
| `MAIL_PASSWORD`       | auth-service                   | ✅                | SMTP App Password                                   |
| `MAIL_AUTH`           | auth-service                   | ✅                | Enable SMTP authentication (`true`)                 |
| `MAIL_STARTTLS`       | auth-service                   | ✅                | Enable STARTTLS encryption (`true`)                 |
| `MAIL_FROM`           | auth-service                   | ✅                | Sender address displayed in outgoing emails         |
| `FRONTEND_URL`        | auth-service                   | ✅                | Frontend URL used to generate verification links    |
| `AUTH_SERVICE_URL`    | api-gateway                    | ✅ *(Render)*     | Internal URL of the auth-service                    |
| `PRODUCT_SERVICE_URL` | api-gateway, order-service     | ✅ *(Render)*     | Internal URL of the product-service                 |
| `ORDER_SERVICE_URL`   | api-gateway                    | ✅ *(Render)*     | Internal URL of the order-service                   |

> ⚠️ **Critical for Render deployments:** All services must use `PORT=8080` (or omit the `PORT` variable entirely).
> The Dockerfile exposes port `8080` — setting any other port (e.g., `8081`, `8082`) will cause a **502 Bad Gateway** error.

---

## API Reference

### Authentication — `/api/auth`

| Method | Endpoint             | Request Body                         | Description                      |
|--------|----------------------|--------------------------------------|----------------------------------|
| POST   | `/api/auth/register` | `{ email, password, name, phone }`   | Create a new user account        |
| POST   | `/api/auth/login`    | `{ email, password }`                | Authenticate and retrieve user   |
| GET    | `/api/auth/verify`   | `?token=<verification_token>`        | Verify email address via link    |

> Login is only permitted after the user's email has been verified.

### Users — `/api/users`

| Method | Endpoint                | Params / Body              | Description              |
|--------|-------------------------|----------------------------|--------------------------|
| GET    | `/api/users/wishlist`   | `?userId=<email>`          | Retrieve wishlist        |
| POST   | `/api/users/wishlist`   | `{ userId, productIds }`   | Save wishlist            |
| GET    | `/api/users/addresses`  | `?userId=<email>`          | Retrieve saved addresses |
| POST   | `/api/users/addresses`  | `{ userId, ... }`          | Add a new address        |

### Products — `/api/products` & `/api/content`

| Method | Endpoint                      | Params              | Description                      |
|--------|-------------------------------|---------------------|----------------------------------|
| GET    | `/api/products`               | `?category=&...`    | List products (with filtering)   |
| GET    | `/api/products/{id}`          | —                   | Get product details              |
| GET    | `/api/products/{id}/reviews`  | —                   | Get product reviews              |
| GET    | `/api/content/navigation`     | —                   | Navigation menu data             |
| GET    | `/api/content/footer-links`   | —                   | Footer links data                |

### Orders — `/api/orders`

| Method | Endpoint                        | Params / Body                       | Description                  |
|--------|---------------------------------|-------------------------------------|------------------------------|
| POST   | `/api/orders`                   | `{ items, customerInfo, ... }`      | Place a new order            |
| GET    | `/api/orders/track`             | `?orderId=&phone=`                  | Track an order               |
| GET    | `/api/orders/history`           | `?email=`                           | Get order history            |
| POST   | `/api/orders/cancel`            | `{ orderId, phone }`                | Cancel an order              |
| GET    | `/api/orders/coupons/validate`  | `?code=`                            | Validate a coupon code       |
| GET    | `/api/orders/payment/vnpay`     | `?orderId=&amount=`                 | Generate VNPay payment link  |

### Cart — `/api/orders/cart`

| Method | Endpoint                    | Params / Body                       | Description              |
|--------|-----------------------------|-------------------------------------|--------------------------|
| GET    | `/api/orders/cart`          | `?userId=`                          | Get cart contents        |
| POST   | `/api/orders/cart/add`      | `{ userId, productId, quantity }`   | Add item to cart         |
| DELETE | `/api/orders/cart/remove`   | `?userId=&productId=`               | Remove item from cart    |

---

## Deploying to Render

Each microservice is deployed as a separate **Web Service** on Render, all built from the same shared `Dockerfile` using a different `SERVICE` build argument.

### Docker Build Argument

| Service         | Build Arg Value        |
|-----------------|------------------------|
| api-gateway     | `SERVICE=api-gateway`     |
| auth-service    | `SERVICE=auth-service`    |
| product-service | `SERVICE=product-service` |
| order-service   | `SERVICE=order-service`   |

### Sample Environment Variables per Service

**`api-gateway`:**
```
PORT=8080
SERVICE=api-gateway
AUTH_SERVICE_URL=https://<auth-service>.onrender.com
PRODUCT_SERVICE_URL=https://<product-service>.onrender.com
ORDER_SERVICE_URL=https://<order-service>.onrender.com
FRONTEND_URL=https://pulse-tech-beryl.vercel.app
```

**`auth-service`:**
```
PORT=8080
SERVICE=auth-service
MONGODB_URI=<your-atlas-uri>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<your-email>
MAIL_PASSWORD=<your-app-password>
MAIL_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=<your-email>
FRONTEND_URL=https://pulse-tech-beryl.vercel.app
```

**`product-service`:**
```
PORT=8080
SERVICE=product-service
MONGODB_URI=<your-atlas-uri>
```

**`order-service`:**
```
PORT=8080
SERVICE=order-service
MONGODB_URI=<your-atlas-uri>
PRODUCT_SERVICE_URL=https://<product-service>.onrender.com
```

> ℹ️ **Render Free Tier — Cold Starts:** Services automatically spin down after 15 minutes of inactivity. The first request after a cold start may take **30–50 seconds** to respond. This is expected behavior on the free tier.

---

## Security Notes

- **Never commit the `.env` file** — it is already listed in `.gitignore`.
- Do not expose `MONGODB_URI` or `MAIL_PASSWORD` in public repositories or logs.
- Mark sensitive variables as **Sensitive** on the Render dashboard to prevent them from being displayed.
- User accounts can only log in after completing email verification.
