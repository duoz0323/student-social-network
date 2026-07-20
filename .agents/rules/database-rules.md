# Quy tắc Database

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. SQL và DBML là baseline kỹ thuật hiện tại để đối chiếu, không có mức ưu tiên cao hơn README.

## 1. Database chính

- Sử dụng MySQL.
- MySQL là nguồn dữ liệu chuẩn trong MVP.
- Không dùng Elasticsearch trong MVP.
- Không lưu file ảnh dưới dạng BLOB.

## 2. Quy tắc bảng

- Tên bảng dùng snake_case và số nhiều.
- Khóa chính dùng `id`.
- Khóa ngoại có tên rõ nghĩa.
- Bảng chính có `created_at` và `updated_at`.
- Dữ liệu quan trọng ưu tiên xóa mềm.
- Trạng thái phải dùng tập giá trị rõ ràng.

## 3. Ràng buộc duy nhất

Phải có unique constraint cho:

- `users.email`.
- `users.phone_number`.
- `follows(follower_id, following_id)`.
- `post_likes(user_id, post_id)`.
- `saved_posts(user_id, post_id)`.

Báo cáo cần chống trùng báo cáo `PENDING` cùng người dùng và cùng bài viết bằng logic nghiệp vụ hoặc cấu trúc phù hợp.

## 4. Ràng buộc nghiệp vụ

- Trích xuất invariant từ README trước khi đánh giá schema.
- Đối chiếu từng invariant với SQL, DBML, Entity, Repository và test database.
- Phân biệt constraint có thể cưỡng chế tại database với quy tắc phải bảo đảm bằng transaction/service.
- Với Auth, phải kiểm tra tính duy nhất, vòng đời pending, dữ liệu hash, nullable, foreign key, concurrency và cleanup theo README.
- Với onboarding và các module khác, kiểm tra constraint theo đúng trạng thái đích trong README.
- Không thay đổi SQL/DBML hoặc đề xuất migration chỉ để giữ tương thích với source cũ.

## 5. Index đề xuất

- `users(email)`.
- `users(phone_number)`.
- `users(status)`.
- `posts(author_id)`.
- `posts(status, created_at)`.
- `comments(post_id, created_at)`.
- `follows(follower_id)`.
- `follows(following_id)`.
- `post_likes(post_id)`.
- `saved_posts(user_id, created_at)`.
- `reports(status, created_at)`.

## 6. Migration

- Mọi thay đổi schema phải có migration.
- Không sửa trực tiếp database production.
- Migration phải có thứ tự rõ ràng.
- Không xóa cột hoặc bảng nếu chưa đánh giá dữ liệu.
- Seed data tách riêng khỏi migration cấu trúc khi phù hợp.
