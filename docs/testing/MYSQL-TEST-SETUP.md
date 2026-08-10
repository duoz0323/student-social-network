# Thiết lập MySQL integration test

## Yêu cầu an toàn

- Chỉ dùng database có tên chứa `test`, khuyến nghị `student_social_network_test`.
- Không dùng URL, username hoặc password production.
- Không ghi credential thật vào source, `.env` hoặc lệnh đã commit.
- Profile `mysql-test` đọc toàn bộ kết nối từ biến môi trường.

## Cách 1: MySQL đã cài trên máy

Đăng nhập bằng tài khoản quản trị MySQL rồi chạy:

```sql
CREATE DATABASE student_social_network_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER '<test_user>'@'localhost' IDENTIFIED BY '<test_password>';
GRANT ALL PRIVILEGES ON student_social_network_test.* TO '<test_user>'@'localhost';
FLUSH PRIVILEGES;
```

Tạo schema ứng dụng trên database test từ `database/student_social_network.sql`, nhưng phải:

1. Thay đúng tên database `student_social_network` thành `student_social_network_test` trong một bản tạm.
2. Nếu cần schema sạch, bỏ phần bắt đầu từ marker `-- DEV/DEMO SEED DATA`.
3. Không sửa file canonical chỉ để chạy test.
4. Dùng lại full rebuild canonical nếu schema test chưa có `user_blocks`; repo demo không giữ migration rời.

## Cách 2: Docker MySQL 8 cô lập

Ví dụ dưới đây dùng cổng `33306` để tránh xung đột MySQL local. Thay credential placeholder trước khi chạy:

```powershell
docker run -d --name student-social-network-mysql-test `
  -p 127.0.0.1:33306:3306 `
  -e MYSQL_DATABASE=student_social_network_test `
  -e MYSQL_USER=<test_user> `
  -e MYSQL_PASSWORD=<test_password> `
  -e MYSQL_ROOT_PASSWORD=<temporary_root_password> `
  mysql:8.0.36-debian `
  --character-set-server=utf8mb4 `
  --collation-server=utf8mb4_0900_ai_ci
```

Container phải chỉ phục vụ test. Không mount volume production vào container này.

## Biến môi trường

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql-test'
$env:AUTH_TEST_DB_URL='jdbc:mysql://127.0.0.1:33306/student_social_network_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh'
$env:AUTH_TEST_DB_USERNAME='<test_user>'
$env:AUTH_TEST_DB_PASSWORD='<test_password>'
$env:RUN_MYSQL_INTEGRATION_TESTS='true'
```

Các Spring Context test cần secret test giả đủ dài. Không dùng secret production:

```powershell
$env:JWT_ACCESS_TOKEN_SECRET='<test-only-secret-at-least-32-bytes>'
$env:AUTH_OTP_HMAC_SECRET='<test-only-secret>'
$env:AUTH_FLOW_TOKEN_HMAC_SECRET='<test-only-secret>'
$env:AUTH_SOCIAL_IDENTITY_FINGERPRINT_SECRET='<test-only-secret>'
```

## Kiểm tra kết nối và chạy test

```powershell
Set-Location BackEnd
mvn.cmd clean test
mvn.cmd clean package
```

Khi profile và biến môi trường đúng:

- Test dùng `AUTH_TEST_DB_URL` sẽ chạy.
- Test dùng `RUN_MYSQL_INTEGRATION_TESTS=true` sẽ dùng cùng datasource từ profile `mysql-test`.
- Hibernate chỉ `validate`; test không tự tạo hoặc sửa schema production.
