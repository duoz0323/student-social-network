# Workflow review code

## 1. Phạm vi review

Kiểm tra:

- Đúng nghiệp vụ.
- Đúng kiến trúc.
- Bảo mật.
- Hiệu năng.
- Validation.
- Phân quyền.
- Khả năng bảo trì.
- Test.
- Comment.
- Tài liệu.

Riêng Auth/Profile MVP cần kiểm tra thêm:

- Register không nhận `username` hoặc `displayName`.
- Register chỉ nhận đúng một định danh email hoặc số điện thoại.
- `users` và `user_profiles` được tạo trong cùng transaction.
- `display_name` và `profile_completed_at` ban đầu là `NULL`.
- Người chưa hoàn tất hồ sơ bị chặn khỏi API mạng xã hội chính bằng `PROFILE_NOT_COMPLETED`.
- Route guard không cho người đã đăng nhập nhưng chưa hoàn tất hồ sơ vào Feed.
- API hồ sơ công khai không lộ email hoặc số điện thoại.

## 2. Mức độ vấn đề

- Critical: lỗ hổng, mất dữ liệu, sai quyền nghiêm trọng.
- High: lỗi nghiệp vụ chính hoặc crash.
- Medium: hiệu năng, maintainability hoặc edge case.
- Low: naming, style hoặc cải thiện nhỏ.

## 3. Cách báo cáo

Mỗi vấn đề gồm:

- Mức độ.
- File/dòng.
- Mô tả.
- Nguyên nhân.
- Ảnh hưởng.
- Hướng sửa.

Không chỉ đưa nhận xét chung chung.
