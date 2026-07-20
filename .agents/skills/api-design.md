# Kỹ năng thiết kế API

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Skill này chỉ hướng dẫn cách chốt contract; endpoint và nghiệp vụ phải được lấy từ README cùng tài liệu API đã đồng bộ.

## 1. Nguyên tắc REST

- Endpoint dùng danh từ.
- Dùng số nhiều.
- Dùng HTTP method đúng mục đích.
- Dùng HTTP status phù hợp.
- API danh sách có phân trang.
- Response thống nhất.

## 2. Quy trình chốt endpoint và contract

1. Đọc phạm vi, actor, luồng, API tham chiếu và tiêu chí nghiệm thu trong README.
2. Kiểm tra tài liệu API hiện tại đã đồng bộ README hay chưa; không dùng endpoint cũ làm chuẩn.
3. Xác định method, path, authentication, authorization, request, response, validation và error code.
4. Xác định idempotency, concurrency, transaction boundary và dữ liệu nhạy cảm.
5. Đối chiếu contract với SQL/DBML, source, Frontend và test; báo cáo breaking change.
6. Chỉ cập nhật tài liệu API và triển khai sau khi phạm vi được chốt.

## 3. Kiểm tra API Auth và onboarding

- Bao phủ mọi trạng thái và nhánh xung đột được README quy định.
- Phân biệt provider credential, JWT Access Token, Refresh Token và flow token.
- Không để request từ Frontend quyết định danh tính hoặc quyền mà Backend phải xác minh.
- Xác định rõ API public, API yêu cầu JWT và API bị giới hạn bởi onboarding.
- Thiết kế test từ tiêu chí nghiệm thu trong README, không từ hành vi implementation cũ.

## 4. Response lỗi

Response lỗi nên gồm:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors` nếu có validation.

Không trả stack trace.
