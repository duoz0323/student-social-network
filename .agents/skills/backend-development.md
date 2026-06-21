# Kỹ năng phát triển Backend

## 1. Mục tiêu

Hướng dẫn Agent xây dựng API Spring Boot theo layered architecture và package theo feature.

## 2. Cấu trúc feature

```text
post/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
└── exception/
```

## 3. Quy trình triển khai

1. Đọc PRD và rule nghiệp vụ.
2. Xác định Entity liên quan.
3. Xác định request DTO.
4. Xác định response DTO.
5. Viết Repository.
6. Viết Service.
7. Viết Mapper.
8. Viết Controller.
9. Bổ sung validation.
10. Bổ sung phân quyền.
11. Viết test.

## 4. Nguyên tắc

- Không trả Entity.
- Không để Controller chứa logic.
- Không bỏ qua transaction khi có nhiều thao tác dữ liệu.
- Không bỏ qua kiểm tra quyền.
- Exception phải rõ nghĩa.
- Response không chứa dữ liệu nhạy cảm.

## 5. Kiểm tra

- Compile thành công.
- Test liên quan chạy thành công.
- Validation hoạt động.
- Phân quyền hoạt động.
- HTTP status đúng.
- Không lộ stack trace.
