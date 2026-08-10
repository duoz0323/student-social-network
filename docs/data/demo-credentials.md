# Tài khoản demo

Bộ dữ liệu được tạo bởi `database/seeds/seed_1000_website_cases.sql`.

## Mật khẩu dùng chung

- Các tài khoản local dùng mật khẩu test: `TestUser01@2026`.
- Chỉ dùng cho local/test; không dùng credential này trong production.

## Tài khoản chính

| Trường hợp | Email | Username | Trạng thái |
| --- | --- | --- | --- |
| Admin | `demo.user0001@example.test` | `student_0001` | `ADMIN / ACTIVE` |
| User thường | `demo.user0002@example.test` | `student_0002` | `USER / ACTIVE` |
| Local + Google | `demo.user0005@example.test` | `student_0005` | `USER / ACTIVE` |
| User bị khóa | `demo.user0006@example.test` | `student_0006` | `USER / BLOCKED` |
| Chưa onboarding | `demo.user0991@example.test` | `NULL` | `USER / ACTIVE` |

## Tài khoản social-only

- `demo.user0003@example.test`: Google-only, không có mật khẩu local.
- `demo.user0004@example.test`: Facebook-only, không có mật khẩu local.

## Quy luật dữ liệu

- Email từ `demo.user0001@example.test` đến `demo.user1000@example.test`.
- Username từ `student_0001` đến `student_0990`.
- User 991–1000 chưa hoàn tất onboarding và chưa có username.
- User 6 và các user có ID chia hết cho 100 ở trạng thái `BLOCKED`.
