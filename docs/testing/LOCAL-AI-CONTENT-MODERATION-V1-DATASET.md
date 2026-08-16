# Local AI Content Moderation V1 — Manual E2E Dataset

Tài liệu này bổ sung dữ liệu kiểm thử cho [`AI-CONTENT-MODERATION-V1-E2E-CHECKLIST.md`](AI-CONTENT-MODERATION-V1-E2E-CHECKLIST.md). Chạy trên dữ liệu test, không dùng nội dung nhắm vào người thật và không coi một vài câu mẫu là đánh giá chất lượng model đầy đủ.

## Chuẩn bị

1. Khởi động `ai-service`, đợi `GET http://127.0.0.1:8001/ready` trả 200.
2. Khởi động Backend với `AI_MODERATION_LOCAL_BASE_URL=http://127.0.0.1:8001`.
3. Đăng nhập USER `ACTIVE` đã hoàn tất profile; mở Post composer và một Post detail có thể bình luận.
4. Với từng case, thử lần lượt ở Post create, Post update text, Comment và Reply; ghi lại label trực tiếp từ FastAPI trong test kỹ thuật, nhưng chỉ kiểm tra error contract an toàn trên giao diện.

## Bộ dữ liệu tối thiểu

| ID | Nội dung mẫu | Kỳ vọng policy | Ghi chú |
|---|---|---|---|
| CLEAN-01 | `Hôm nay lớp mình học rất vui.` | ALLOW | Câu trung tính tiếng Việt. |
| CLEAN-02 | `Bạn gửi mình tài liệu môn Java nhé.` | ALLOW | Hội thoại sinh viên thông thường. |
| OFFENSIVE-01 | `Đồ ngu, nói chuyện chán thật.` | WARNING hoặc ghi nhận sai số model | Xúc phạm trực tiếp; checkpoint thực tế có thể nhầm sang HATE, cần ghi nhận thay vì sửa threshold tùy tiện. |
| OFFENSIVE-02 | `Bài này tệ quá, làm ăn kiểu gì vậy.` | WARNING | Ngôn ngữ công kích nhẹ. |
| HATE-01 | `Tất cả bọn thuộc nhóm đó đều đáng bị loại bỏ.` | BLOCK | Câu tổng quát chỉ dùng fixture, không nhắm nhóm/người thật. |
| LONG-TAIL-01 | Lặp nội dung sạch đủ hơn 254 token rồi nối một fixture HATE ở cuối | BLOCK | Xác minh chunk cuối không bị truncation. |
| INVALID-01 | Chuỗi chỉ có khoảng trắng | Bị validation nghiệp vụ chặn | Không gọi model với text rỗng. |

Kết quả model có thể khác kỳ vọng do sai số và domain shift. Nếu một fixture không ổn định, ghi lại model revision, label/scores và thay fixture bằng dữ liệu test đã được review; không tự thêm threshold hoặc hard-code từ khóa trong Backend.

## Case lỗi vận hành

- Dừng FastAPI rồi submit: Backend trả `CONTENT_MODERATION_UNAVAILABLE`, UI giữ draft, không có side effect database/realtime.
- Đặt `AI_MODERATION_LOCAL_BASE_URL` sai port: kết quả tương tự connection refused.
- Đặt read timeout rất thấp trong môi trường test: kết quả tương tự timeout.
- Gửi response giả thiếu `scores`, confidence ngoài `[0,1]` hoặc label lạ: adapter từ chối và fail closed.

Chỉ đổi trạng thái sang `INTEGRATED` sau khi checklist tổng và dataset này pass trên UI thật, đồng thời kiểm tra row/counter/notification/realtime của mọi case rejected.
