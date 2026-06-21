# Quy tắc chung của dự án

## 1. Tổng quan

Dự án xây dựng một website mạng xã hội tinh gọn hướng đến sinh viên.

Mục tiêu của MVP là hoàn thiện luồng mạng xã hội cốt lõi, ổn định, có thể kiểm thử và trình bày trong luận văn.

## 2. Luồng nghiệp vụ chính

Đăng ký
→ Đăng nhập
→ Quản lý hồ sơ
→ Follow
→ Đăng bài
→ Xem Feed
→ Like
→ Bình luận
→ Lưu bài
→ Tìm kiếm
→ Báo cáo
→ Quản trị.

## 3. Nguyên tắc làm việc

Agent phải:

- Đọc tài liệu trước khi code.
- Không suy đoán nghiệp vụ khi tài liệu đã quy định.
- Không bổ sung chức năng ngoài MVP.
- Không thay đổi kiến trúc nếu chưa có lý do rõ ràng.
- Không sửa nhiều module không liên quan trong cùng một nhiệm vụ.
- Luôn ưu tiên thay đổi nhỏ, an toàn và có thể kiểm thử.
- Giữ code và tài liệu đồng nhất.

## 4. Nguyên tắc đặt tên

- Tên file và thư mục: tiếng Anh, dạng kebab-case hoặc theo convention framework.
- Java class: PascalCase.
- Java method và field: camelCase.
- React component: PascalCase.
- Hook React: bắt đầu bằng `use`.
- Constant: UPPER_SNAKE_CASE.
- Endpoint REST: danh từ số nhiều, chữ thường, dùng dấu gạch ngang khi cần.

## 5. Quy tắc comment

- Comment bằng tiếng Việt.
- Giải thích mục đích, quy tắc nghiệp vụ hoặc lý do kỹ thuật.
- Không comment lại chính xác điều câu lệnh đã thể hiện.
- Không để comment sai lệch với code.

## 6. Quy tắc Git

- Không commit secret.
- Không commit file build.
- Không commit `node_modules`.
- Không commit cấu hình cá nhân IDE.
- Mỗi commit chỉ nên phục vụ một mục tiêu rõ ràng.
- Không đổi tên hoặc xóa hàng loạt file nếu không thật sự cần thiết.

## 7. Quy tắc tài liệu

Khi thay đổi nghiệp vụ hoặc API:

1. Cập nhật tài liệu liên quan.
2. Cập nhật mock data nếu cần.
3. Cập nhật Frontend.
4. Cập nhật Backend.
5. Cập nhật test.
