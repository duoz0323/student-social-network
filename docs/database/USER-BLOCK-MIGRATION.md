# Hướng dẫn migration User Block

## Trạng thái

`VERIFIED` ngày 28/07/2026 trên container cô lập dùng MySQL 8.0.36.

Kết quả thực tế:

- Schema sạch: tạo schema canonical không seed, bỏ `user_blocks`, chạy migration thành công.
- Schema có dữ liệu: giữ nguyên số lượng `users`, `follows`, `posts`, `post_likes`, `comments`, `saved_posts` và `notifications` trước/sau migration.
- `SHOW CREATE TABLE` xác nhận primary key kép, index đảo chiều, CHECK chống tự Block, hai foreign key, `ON DELETE CASCADE` và `ON UPDATE RESTRICT`.
- Insert trùng không tạo bản ghi thứ hai; self Block và foreign key không hợp lệ bị MySQL từ chối.
- Xóa user theo chính sách hiện hành xóa cascade quan hệ Block.
- Toàn bộ 599 backend test chạy với MySQL profile: 0 lỗi, 0 skipped.

Dự án đang dùng SQL canonical chạy thủ công, không có Flyway hoặc Liquibase. Migration bổ sung nằm tại:

- Repo demo đã tinh gọn, không còn lưu migration rời.
- Schema đầy đủ: `database/student_social_network.sql`
- Mô hình đối chiếu: `database/student_social_network.dbml`

## Điều kiện an toàn

- Chỉ chạy trên database test hoặc database đã sao lưu.
- Không chạy schema canonical trên database đang có dữ liệu vì file canonical có các lệnh `DROP TABLE`.
- Migration riêng chỉ tạo `user_blocks`; không xóa hay cập nhật dữ liệu hiện hữu.
- Database test dùng cho integration test phải có tên chứa `test`.

## Lệnh chạy migration

PowerShell:

```powershell
mysql.exe --host=localhost --port=3306 --user=<test_user> --password <test_database> `
  < database/student_social_network.sql
```

Sau khi schema test đã sẵn sàng, cấu hình:

```powershell
$env:AUTH_TEST_DB_URL='jdbc:mysql://localhost:3306/<database_name_contains_test>?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh'
$env:AUTH_TEST_DB_USERNAME='<test_user>'
$env:AUTH_TEST_DB_PASSWORD='<test_password>'
mvn.cmd test
```

## Các xác minh bắt buộc

```sql
SHOW CREATE TABLE user_blocks;

-- Phải thành công một lần.
INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (<user_a>, <user_b>);

-- Phải lỗi duplicate primary key.
INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (<user_a>, <user_b>);

-- Phải lỗi CHECK constraint.
INSERT INTO user_blocks(blocker_id, blocked_id) VALUES (<user_a>, <user_a>);
```

Cần xác nhận từ `SHOW CREATE TABLE`:

- Primary key kép `(blocker_id, blocked_id)`.
- Index đảo chiều `(blocked_id, blocker_id)`.
- CHECK `blocker_id <> blocked_id`.
- Hai foreign key tới `users.id`.
- `ON DELETE CASCADE` và `ON UPDATE RESTRICT` đúng convention.

Integration test `UserBlockPostQueryMySqlIntegrationTest` kiểm tra thêm việc lọc Block trước limit/cursor cho Feed, Profile, Liked, Saved và Following.
