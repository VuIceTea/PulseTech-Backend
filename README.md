# PulseTech Backend

Backend Java 21/Spring Boot được tổ chức theo mô hình microservice và nằm độc lập tại `D:\backend`.

## Các service

- `api-gateway` (`8080`): cổng API duy nhất cho frontend.
- `product-service` (`8081`): sản phẩm, collection `products`.
- `auth-service` (`8082`): đăng ký, đăng nhập, xác thực email; collections `users`, `email_verification_tokens`.
- `order-service` (`8083`): đặt hàng và tra cứu đơn, collection `orders`.
- MongoDB Atlas: database `PulseTech`, được cấu hình bằng `MONGODB_URI` trong `.env`.
- Frontend Next.js (`3000`).

## Chạy toàn bộ hệ thống

```powershell
cd D:\backend
docker compose up --build
```

Mở website tại http://localhost:3000.

Khi đăng ký, mở email trong hộp thư thật và bấm liên kết xác thực. Trang `/verify-email` sẽ xác thực tài khoản, hiển thị kết quả rồi tự chuyển về trang đăng nhập.

Dừng hệ thống:

```powershell
docker compose down
```


## API qua Gateway

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/auth/register`
- `GET /api/auth/verify?token=...`
- `POST /api/auth/login`
- `POST /api/orders`
- `GET /api/orders/track?orderId=...&phone=...`

Đăng nhập chỉ thành công sau khi email đã được xác thực. Hệ thống gửi email qua SMTP thật được cấu hình bằng `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_AUTH`, `MAIL_STARTTLS` và `MAIL_FROM`.
## Chuyển dữ liệu sang MongoDB bên ngoài

1. Tạo cluster MongoDB Atlas/server từ xa, database user và cho phép IP máy chạy Docker truy cập.
2. Sao chép `.env.example` thành `.env`, sau đó đặt `MONGODB_URI` trỏ tới database `PulseTech`.
3. Chạy:

```powershell
cd D:\backend
.\scripts\migrate-mongodb-to-external.ps1
```

Script tạo backup BSON trong `backups/`, restore các collection `PulseTech.*` mà không dùng `--drop`, rồi khởi động lại auth/product/order service với URI mới.
## Dữ liệu sản phẩm và MongoDB Compass

Dữ liệu nghiệp vụ được lưu trong MongoDB Atlas, database `PulseTech`. Product service chỉ đọc collection `products` qua MongoDB Repository; project không còn nạp sản phẩm từ `products.json` hoặc thư mục `data`.

Để xem dữ liệu bằng MongoDB Compass:

1. Mở MongoDB Compass và chọn **New Connection**.
2. Sao chép giá trị `MONGODB_URI` trong `D:\backend\.env` vào ô connection string.
3. Chọn **Connect**, sau đó mở database `PulseTech`.
4. Các collection hiện có gồm `products`, `users`, `email_verification_tokens` và `orders`.

Không commit hoặc chia sẻ file `.env` vì connection string chứa thông tin đăng nhập database.
