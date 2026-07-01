# Quy tắc Database

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

- Mỗi tài khoản phải luôn có ít nhất email hoặc số điện thoại.
- Tại thời điểm đăng ký, người dùng chỉ cung cấp đúng một phương thức định danh: email hoặc số điện thoại.
- Nếu đăng ký bằng email thì `phone_number` được phép `NULL`; nếu đăng ký bằng số điện thoại thì `email` được phép `NULL`.
- Database cho phép một tài khoản có cả email và số điện thoại sau khi người dùng bổ sung phương thức còn thiếu trong tương lai.
- Email lưu ở dạng chữ thường đã chuẩn hóa nếu có giá trị.
- Số điện thoại lưu ở dạng chuẩn hóa thống nhất nếu có giá trị.
- `email_verified_at` và `phone_verified_at` chuẩn bị cho hướng phát triển; để `NULL` trong MVP khi chưa xác minh.
- Không dùng cột `verified` kiểu chuỗi để lưu trạng thái xác minh email hoặc số điện thoại.
- Không tạo cột `gmail`; dùng cột email chung cho mọi nhà cung cấp email.
- Không dùng `username` làm định danh công khai trong MVP.
- `display_name` thuộc `user_profiles`, không thuộc `users`.
- Sau đăng ký phải có một bản ghi `user_profiles` tương ứng với `users`.
- `user_profiles.display_name` ban đầu được phép `NULL`.
- `user_profiles.profile_completed_at` ban đầu phải `NULL`.
- `profile_completed_at` là dữ liệu xác định hồ sơ đã hoàn tất; không suy luận từ `users.status`.
- Ngày sinh thuộc hồ sơ người dùng, là thông tin tùy chọn và không được lớn hơn ngày hiện tại.
- Người dùng không được Follow chính mình.
- Bài viết phải có nội dung hoặc hình ảnh.
- Bài viết tối đa 500 ký tự.
- Bài có tối đa 4 hình ảnh.
- Chỉ lưu metadata và URL ảnh.
- Bài HIDDEN hoặc DELETED không xuất hiện trong truy vấn thông thường.

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
