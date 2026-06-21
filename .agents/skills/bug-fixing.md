# Kỹ năng sửa lỗi

## 1. Nguyên tắc bắt buộc

Khi nhận code lỗi hoặc log lỗi, không sửa ngay.

Phải thực hiện:

1. Phân tích nguyên nhân cốt lõi.
2. Đề xuất giải pháp tối ưu.
3. Sau đó mới đưa code đã sửa.
4. Kiểm thử lại trường hợp lỗi và trường hợp liên quan.

## 2. Cách phân tích

Xác định:

- Lỗi xảy ra ở tầng nào.
- Dòng hoặc module liên quan.
- Nguyên nhân trực tiếp.
- Nguyên nhân gốc.
- Dữ liệu đầu vào gây lỗi.
- Ảnh hưởng tới module khác.
- Có phải lỗi cấu hình, nghiệp vụ, transaction hay dữ liệu không.

## 3. Cách sửa

- Sửa nguyên nhân gốc.
- Không vá tạm nếu có giải pháp đúng.
- Không làm thay đổi API không cần thiết.
- Có comment tiếng Việt.
- Giữ tương thích với kiến trúc.
- Bổ sung test chống tái phát lỗi.
