# Kỹ năng phát triển Backend

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Skill này hướng dẫn cách triển khai, không thay thế đặc tả nghiệp vụ trong README.

## 1. Mục tiêu

Hướng dẫn Agent xây dựng API Spring Boot theo layered architecture và package theo feature.

## 2. Cấu trúc feature

```text
post/
├── controller/
├── service/
├── repository/
├── entity/
├── enums/
├── dto/
├── mapper/
└── exception/
```

Thư mục `enums/` chỉ tạo khi module có enum nghiệp vụ. Các trạng thái hoặc nhóm giá trị cố định của entity phải đặt tại đây, ví dụ `user/enums/UserStatus.java`, `user/enums/UserRole.java`, `post/enums/PostStatus.java`.

## 3. Quy trình triển khai

1. Đọc README, sau đó đọc PRD và rule nghiệp vụ để phát hiện khoảng chênh lệch.
2. Xác định Entity liên quan.
3. Xác định enum của entity và đặt trong `enums/` của module.
4. Xác định request DTO.
5. Xác định response DTO.
6. Viết Repository.
7. Viết Service.
8. Viết Mapper.
9. Viết Controller.
10. Bổ sung validation.
11. Bổ sung phân quyền.
12. Viết test.

## 4. Nguyên tắc

- Không trả Entity.
- Không để Controller chứa logic.
- Không bỏ qua transaction khi có nhiều thao tác dữ liệu.
- Không bỏ qua kiểm tra quyền.
- Exception phải rõ nghĩa.
- Response không chứa dữ liệu nhạy cảm.
- Enum nghiệp vụ thuộc module nào thì đặt trong `enums/` của module đó; không đặt chung ngoài module khi không cần chia sẻ.
- Với Auth/onboarding, lập state machine, transaction boundary và bảng đối chiếu README với Entity/Repository/source/test trước khi code.
- Không gọi dịch vụ bên ngoài trong transaction database.
- Không tin dữ liệu danh tính, trạng thái verified hoặc user đích do Frontend tự khai báo.
- Mọi validation, quyền, token và state transition phải truy ngược được tới README hoặc API contract đã đồng bộ.
- Khi source hiện tại khác README, xem source là implementation cũ cần thay thế; không làm ngược lại.

## 5. Kiểm tra

- Compile thành công.
- Test liên quan chạy thành công.
- Validation hoạt động.
- Phân quyền hoạt động.
- HTTP status đúng.
- Không lộ stack trace.
