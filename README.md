# PulseTech Backend

> Hệ thống backend cho nền tảng thương mại điện tử **PulseTech** — xây dựng theo kiến trúc **Microservices** với Java 21 và Spring Boot.

---

## Mục lục

- [Tổng quan kiến trúc](#tổng-quan-kiến-trúc)
- [Tech Stack](#tech-stack)
- [Cấu trúc project](#cấu-trúc-project)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt và chạy local](#cài-đặt-và-chạy-local)
- [Biến môi trường](#biến-môi-trường)
- [API Reference](#api-reference)
- [Deploy lên Render](#deploy-lên-render)

---

## Tổng quan kiến trúc

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

---

## Tech Stack

| Thành phần        | Công nghệ                              |
|-------------------|----------------------------------------|
| Ngôn ngữ          | Java 21                                |
| Framework         | Spring Boot 4.1, Spring Cloud Gateway  |
| Database          | MongoDB Atlas                          |
| Cache             | Redis 7                                |
| Build tool        | Maven (multi-module)                   |
| Container         | Docker + Docker Compose                |
| Cloud Deploy      | Render (Docker-based)                  |

---

## Cấu trúc project

```
backend/
├── api-gateway/          # Cổng API duy nhất — định tuyến requests đến các service
├── auth-service/         # Đăng ký, đăng nhập, xác thực email, wishlist, địa chỉ
├── product-service/      # Sản phẩm, danh mục, nội dung trang (banner, footer, nav)
├── order-service/        # Đơn hàng, giỏ hàng, mã giảm giá, thanh toán
├── Dockerfile            # Shared Dockerfile — build bất kỳ service nào qua ARG SERVICE
├── docker-compose.yml    # Chạy toàn bộ hệ thống local (bao gồm cả frontend)
├── pom.xml               # Parent POM (Maven multi-module)
├── .env.example          # Mẫu biến môi trường
└── scripts/              # Script tiện ích (migrate MongoDB, v.v.)
```

### Ánh xạ API Gateway → Service

| Đường dẫn           | Service          |
|---------------------|------------------|
| `/api/products/**`  | product-service  |
| `/api/content/**`   | product-service  |
| `/api/auth/**`      | auth-service     |
| `/api/users/**`     | auth-service     |
| `/api/orders/**`    | order-service    |
| `/api/cart/**`      | order-service    |

---

## Yêu cầu hệ thống

- **Docker Desktop** ≥ 24 (với Docker Compose v2)
- **JDK 21** *(chỉ cần nếu muốn build/chạy không qua Docker)*
- **Maven 3.9+** *(chỉ cần nếu muốn build/chạy không qua Docker)*
- Tài khoản **MongoDB Atlas** (free tier là đủ)
- Tài khoản email hỗ trợ SMTP (Gmail với App Password)

---

## Cài đặt và chạy local

### 1. Clone project

```bash
git clone https://github.com/VuIceTea/PulseTech-Backend.git
cd PulseTech-Backend
```

### 2. Cấu hình biến môi trường

```bash
cp .env.example .env
```

Mở file `.env` và điền các giá trị thực:

```env
MONGODB_URI=mongodb+srv://USERNAME:PASSWORD@CLUSTER.mongodb.net/PulseTech?retryWrites=true&w=majority

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-google-app-password
MAIL_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=your-email@gmail.com

FRONTEND_URL=http://localhost:3000
```

> **Gmail App Password:** Tạo tại *Google Account → Security → 2-Step Verification → App Passwords*.

### 3. Khởi động toàn bộ hệ thống

```bash
docker compose up --build
```

Truy cập:
- **Website:** http://localhost:3000
- **API Gateway:** http://localhost:8080

### 4. Dừng hệ thống

```bash
docker compose down
```

---

## Biến môi trường

| Biến                  | Service                       | Bắt buộc       | Mô tả                                         |
|-----------------------|-------------------------------|----------------|-----------------------------------------------|
| `MONGODB_URI`         | auth, product, order          | ✅              | Connection string MongoDB Atlas               |
| `MAIL_HOST`           | auth-service                  | ✅              | SMTP host (vd: `smtp.gmail.com`)              |
| `MAIL_PORT`           | auth-service                  | ✅              | SMTP port (vd: `587`)                         |
| `MAIL_USERNAME`       | auth-service                  | ✅              | Địa chỉ email SMTP                            |
| `MAIL_PASSWORD`       | auth-service                  | ✅              | App Password của email                        |
| `MAIL_AUTH`           | auth-service                  | ✅              | Bật xác thực SMTP (`true`)                    |
| `MAIL_STARTTLS`       | auth-service                  | ✅              | Bật TLS (`true`)                              |
| `MAIL_FROM`           | auth-service                  | ✅              | Địa chỉ hiển thị trong email gửi đi           |
| `FRONTEND_URL`        | auth-service                  | ✅              | URL frontend để tạo link xác thực email       |
| `AUTH_SERVICE_URL`    | api-gateway                   | ✅ *(Render)*   | URL của auth-service                          |
| `PRODUCT_SERVICE_URL` | api-gateway, order-service    | ✅ *(Render)*   | URL của product-service                       |
| `ORDER_SERVICE_URL`   | api-gateway                   | ✅ *(Render)*   | URL của order-service                         |

> ⚠️ **Quan trọng khi deploy Render:** Tất cả service phải dùng `PORT=8080` (hoặc bỏ hẳn biến `PORT`).
> Dockerfile đã `EXPOSE 8080` — nếu đặt PORT khác sẽ gây lỗi **502 Bad Gateway**.

---

## API Reference

### Auth — `/api/auth`

| Method | Endpoint             | Body                               | Mô tả                   |
|--------|----------------------|------------------------------------|-------------------------|
| POST   | `/api/auth/register` | `{ email, password, name, phone }` | Đăng ký tài khoản mới   |
| POST   | `/api/auth/login`    | `{ email, password }`              | Đăng nhập               |
| GET    | `/api/auth/verify`   | `?token=<token>`                   | Xác thực email          |

### User — `/api/users`

| Method | Endpoint                | Params / Body              | Mô tả                    |
|--------|-------------------------|----------------------------|--------------------------|
| GET    | `/api/users/wishlist`   | `?userId=<email>`          | Lấy danh sách yêu thích  |
| POST   | `/api/users/wishlist`   | `{ userId, productIds }`   | Lưu danh sách yêu thích  |
| GET    | `/api/users/addresses`  | `?userId=<email>`          | Lấy sổ địa chỉ           |
| POST   | `/api/users/addresses`  | `{ userId, ... }`          | Thêm địa chỉ mới         |

### Product — `/api/products` & `/api/content`

| Method | Endpoint                      | Params         | Mô tả                         |
|--------|-------------------------------|----------------|-------------------------------|
| GET    | `/api/products`               | `?category=&…` | Danh sách sản phẩm (có lọc)  |
| GET    | `/api/products/{id}`          | —              | Chi tiết sản phẩm             |
| GET    | `/api/products/{id}/reviews`  | —              | Đánh giá sản phẩm             |
| GET    | `/api/content/navigation`     | —              | Dữ liệu menu điều hướng       |
| GET    | `/api/content/footer-links`   | —              | Dữ liệu footer                |

### Order — `/api/orders`

| Method | Endpoint                         | Params / Body                       | Mô tả                     |
|--------|----------------------------------|-------------------------------------|---------------------------|
| POST   | `/api/orders`                    | `{ items, customerInfo, … }`        | Tạo đơn hàng mới          |
| GET    | `/api/orders/track`              | `?orderId=&phone=`                  | Tra cứu đơn hàng          |
| GET    | `/api/orders/history`            | `?email=`                           | Lịch sử đơn hàng          |
| POST   | `/api/orders/cancel`             | `{ orderId, phone }`                | Hủy đơn hàng              |
| GET    | `/api/orders/coupons/validate`   | `?code=`                            | Kiểm tra mã giảm giá      |
| GET    | `/api/orders/payment/vnpay`      | `?orderId=&amount=`                 | Tạo link thanh toán VNPay |

### Cart — `/api/orders/cart`

| Method | Endpoint                    | Params / Body                      | Mô tả                  |
|--------|-----------------------------|------------------------------------|------------------------|
| GET    | `/api/orders/cart`          | `?userId=`                         | Lấy giỏ hàng           |
| POST   | `/api/orders/cart/add`      | `{ userId, productId, quantity }`  | Thêm vào giỏ hàng      |
| DELETE | `/api/orders/cart/remove`   | `?userId=&productId=`              | Xóa khỏi giỏ hàng      |

---

## Deploy lên Render

Mỗi service là một **Web Service** riêng trên Render, build từ cùng một Dockerfile với ARG `SERVICE` khác nhau.

### Build Argument (Docker Build Args)

| Service        | Giá trị ARG          |
|----------------|----------------------|
| api-gateway    | `SERVICE=api-gateway`    |
| auth-service   | `SERVICE=auth-service`   |
| product-service| `SERVICE=product-service`|
| order-service  | `SERVICE=order-service`  |

### Environment Variables mẫu cho Render

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
MONGODB_URI=<atlas-uri>
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<email>
MAIL_PASSWORD=<app-password>
MAIL_AUTH=true
MAIL_STARTTLS=true
MAIL_FROM=<email>
FRONTEND_URL=https://pulse-tech-beryl.vercel.app
```

**`product-service`:**
```
PORT=8080
SERVICE=product-service
MONGODB_URI=<atlas-uri>
```

**`order-service`:**
```
PORT=8080
SERVICE=order-service
MONGODB_URI=<atlas-uri>
PRODUCT_SERVICE_URL=https://<product-service>.onrender.com
```

> ℹ️ **Render Free Tier Cold Start:** Service sẽ sleep sau 15 phút không có request. Lần gọi đầu tiên sau khi sleep mất khoảng **30–50 giây** để khởi động lại — đây là hành vi bình thường.

---

## Bảo mật

- **Không commit file `.env`** — đã được thêm vào `.gitignore`.
- Không chia sẻ `MONGODB_URI` hay `MAIL_PASSWORD` ra bên ngoài.
- Sử dụng **Sensitive variable** trên Render để ẩn giá trị nhạy cảm.
- Đăng nhập chỉ thành công sau khi email đã được xác thực qua link trong hộp thư.
