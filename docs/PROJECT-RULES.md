# Quy tắc dự án

## 1. Tài khoản

- Email duy nhất.
- Số điện thoại duy nhất.
- Không dùng username hoặc định danh công khai tương tự trong MVP.
- Đăng ký bằng email, số điện thoại và mật khẩu.
- Đăng nhập bằng email hoặc số điện thoại.
- Mật khẩu tối thiểu 8 ký tự, gồm chữ, số và ký tự đặc biệt.
- Trạng thái MVP: ACTIVE, BLOCKED.
- User BLOCKED không đăng nhập được.
- Role: USER, ADMIN.

## 2. Hồ sơ

- Hồ sơ công khai.
- Email không hiển thị công khai.
- Chỉ chủ tài khoản cập nhật hồ sơ.
- Avatar, display name và bio có thể cập nhật.

## 3. Follow

- Không Follow chính mình.
- Không Follow trùng.
- Follow có hiệu lực ngay.
- Không có Follow Request.

## 4. Bài viết

- Tối đa 500 ký tự.
- Tối đa 4 ảnh.
- Phải có nội dung hoặc ảnh.
- Ảnh: JPG, JPEG, PNG, WEBP.
- Chỉ tác giả sửa/xóa.
- Không sửa ảnh sau khi đăng.
- Trạng thái: PUBLISHED, HIDDEN, DELETED.

## 5. Like

- Một user Like một post một lần.
- Không Like bài HIDDEN/DELETED.

## 6. Comment

- Chỉ user đăng nhập được comment.
- Chỉ tác giả comment được xóa comment.
- Không comment bài HIDDEN/DELETED.

## 7. Save

- Một user Save một post một lần.
- Saved list chỉ chủ tài khoản xem.

## 8. Feed

- Following: bài của người đang Follow, mới nhất trước.
- For You: bài hợp lệ, điểm cơ bản.
- Không hiển thị HIDDEN/DELETED.
- Có phân trang.

## 9. Search

- MySQL.
- Tìm user theo tên hiển thị.
- Không tìm user theo username.
- Có phân trang.
- Không hiển thị user BLOCKED.
- Không hiển thị post HIDDEN/DELETED.

## 10. Report

- Chỉ report post.
- Trạng thái PENDING, RESOLVED, REJECTED.
- Không có nhiều report PENDING cùng user và post.
- Report không tự động ẩn post.

## 11. Admin

- Chỉ ADMIN truy cập API quản trị.
- Có thể khóa/mở user.
- Có thể ẩn/khôi phục post.
- Có thể xử lý report.

## 12. API

- Danh sách có phân trang.
- HTTP status đúng.
- Không trả Entity.
- Không trả dữ liệu nhạy cảm.
- Không trả stack trace.
