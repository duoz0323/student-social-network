# Quy tắc chung của dự án

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Mọi phân tích và thay đổi phải đối chiếu README trước; không dùng rule này để thay đổi hoặc diễn giải lại nghiệp vụ đã chốt.

## 1. Cách xác định phạm vi

1. Đọc toàn bộ `README.md` trước khi phân tích hoặc sửa đổi.
2. Xác định module, actor, luồng, mức ưu tiên và tiêu chí nghiệm thu tương ứng trong README.
3. Đối chiếu tài liệu hỗ trợ, SQL, DBML, source và test với trạng thái đích đó.
4. Xem mọi khác biệt là khoảng chênh lệch cần báo cáo; không suy luận implementation cũ là nghiệp vụ đúng.
5. Không mở rộng nghiệp vụ hoặc kiến trúc ngoài phạm vi README và yêu cầu trực tiếp của người dùng.

## 2. Quy tắc xử lý mâu thuẫn

- Ghi rõ file, mục, loại mâu thuẫn và ảnh hưởng.
- README luôn có mức ưu tiên cao hơn SQL, DBML, source, test và tài liệu hỗ trợ.
- Chỉ sửa các file nằm trong phạm vi được phép.
- Khi contract chưa đủ chi tiết để triển khai an toàn, phải báo cáo quyết định cần xác nhận.

## 3. Kiểm tra riêng với Auth và onboarding

- Đọc các mục Auth, API, Security, Database và tiêu chí nghiệm thu trong README.
- Kiểm tra đầy đủ vòng đời đăng ký, xác minh, social authentication, liên kết phương thức, JWT và onboarding theo README.
- Không sao chép contract Auth vào rule này; test case và implementation phải được dẫn xuất từ README cùng tài liệu API đã đồng bộ.
- Khi task liên quan username, phải tách đúng phạm vi: registration/login không nhận username; onboarding bắt buộc username; profile chỉ hiển thị `@username`; route và quan hệ nội bộ vẫn dùng `users.id`.

## 4. Nguyên tắc làm việc

Agent phải:

- Đọc tài liệu trước khi code.
- Không suy đoán nghiệp vụ khi tài liệu đã quy định.
- Không bổ sung chức năng ngoài MVP.
- Không thay đổi kiến trúc nếu chưa có lý do rõ ràng.
- Không sửa nhiều module không liên quan trong cùng một nhiệm vụ.
- Luôn ưu tiên thay đổi nhỏ, an toàn và có thể kiểm thử.
- Giữ code và tài liệu đồng nhất.

## 5. Nguyên tắc đặt tên

- Tên file và thư mục: tiếng Anh, dạng kebab-case hoặc theo convention framework.
- Java class: PascalCase.
- Java method và field: camelCase.
- React component: PascalCase.
- Hook React: bắt đầu bằng `use`.
- Constant: UPPER_SNAKE_CASE.
- Endpoint REST: danh từ số nhiều, chữ thường, dùng dấu gạch ngang khi cần.

## 6. Quy tắc comment

- Comment bằng tiếng Việt.
- Giải thích mục đích, quy tắc nghiệp vụ hoặc lý do kỹ thuật.
- Không comment lại chính xác điều câu lệnh đã thể hiện.
- Không để comment sai lệch với code.

## 7. Quy tắc Git

- Không commit secret.
- Không commit file build.
- Không commit `node_modules`.
- Không commit cấu hình cá nhân IDE.
- Mỗi commit chỉ nên phục vụ một mục tiêu rõ ràng.
- Không đổi tên hoặc xóa hàng loạt file nếu không thật sự cần thiết.

## 8. Quy tắc tài liệu

Khi thay đổi nghiệp vụ hoặc API:

1. Cập nhật tài liệu liên quan.
2. Cập nhật mock data nếu cần.
3. Cập nhật Frontend.
4. Cập nhật Backend.
5. Cập nhật test.
