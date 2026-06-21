# Quy tắc Backend

## 1. Công nghệ

- Java.
- Spring Boot.
- Spring Security.
- Spring Data JPA.
- MySQL.
- JWT Access Token.
- Refresh Token.

## 2. Kiến trúc xử lý

Luồng chuẩn:

Controller
→ Service
→ Repository
→ Database.

## 3. Controller

Controller chỉ:

- Nhận request.
- Gọi Service.
- Trả response.
- Áp dụng annotation validation hoặc phân quyền khi phù hợp.

Controller không:

- Chứa nghiệp vụ chính.
- Truy cập Repository trực tiếp.
- Tạo transaction nghiệp vụ phức tạp.
- Trả Entity trực tiếp.

## 4. Service

Service chịu trách nhiệm:

- Kiểm tra nghiệp vụ.
- Kiểm tra quyền.
- Điều phối Repository.
- Quản lý transaction.
- Chuyển đổi dữ liệu thông qua Mapper.
- Ném exception nghiệp vụ rõ nghĩa.

## 5. Repository

Repository chỉ chịu trách nhiệm truy cập dữ liệu.

- Tên method phải phản ánh điều kiện truy vấn.
- Tránh query N+1.
- Chỉ dùng native query khi thực sự cần.
- Truy vấn danh sách phải có phân trang.
- Dùng index phù hợp cho trường truy vấn thường xuyên.

## 6. DTO và Mapper

- Request và Response dùng DTO.
- Không dùng chung DTO cho mọi ngữ cảnh.
- Không trả password hash, refresh token hoặc dữ liệu nhạy cảm.
- Mapper chịu trách nhiệm chuyển Entity và DTO.
- Tránh mapping vòng lặp quan hệ hai chiều.

## 7. Exception

- Xử lý exception tập trung.
- Trả mã HTTP phù hợp.
- Không trả stack trace.
- Error response phải thống nhất.
- Message gửi Client phải dễ hiểu và không lộ cấu trúc nội bộ.

## 8. Transaction

Cân nhắc `@Transactional` cho:

- Tạo bài kèm media và hashtag.
- Follow/Unfollow.
- Like/Unlike.
- Save/Unsave.
- Tạo báo cáo.
- Admin xử lý báo cáo và ẩn bài.
- Thu hồi refresh token.

## 9. Phân quyền

- API người dùng yêu cầu tài khoản hợp lệ.
- API Admin yêu cầu role `ADMIN`.
- Chỉ tác giả được sửa hoặc xóa bài.
- Chỉ tác giả bình luận được xóa bình luận.
- Backend luôn kiểm tra quyền, không tin dữ liệu từ Frontend.

## 10. Hiệu năng

- API danh sách có phân trang.
- Kích thước mặc định 20.
- Không trả dữ liệu thừa.
- Tránh eager loading không cần thiết.
- Kiểm soát query khi lấy Feed.
