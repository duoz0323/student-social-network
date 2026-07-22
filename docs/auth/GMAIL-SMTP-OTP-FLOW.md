# Luồng OTP qua Gmail SMTP

## 1. Phạm vi refactor

Auth chỉ còn một implementation gửi OTP email là `GmailSmtpRegistrationOtpSender`, sử dụng
Spring `JavaMailSender`. Việc thay provider không đổi API contract, cách sinh/hash OTP, pending
registration, password recovery challenge, TTL, cooldown, số lần thử, resend hoặc invalidation.

Các thay đổi chính:

- Thêm `spring-boot-starter-mail` trong `BackEnd/pom.xml`.
- Thêm `GmailSmtpRegistrationOtpSender` và test mock `JavaMailSender`.
- Xóa HTTP email provider cũ cùng test phụ thuộc provider.
- Bỏ toàn bộ cấu hình email provider cũ khỏi `OtpDeliveryProperties` và `application.yaml`.
- Cập nhật `README.md`, `docs/data/API-CONTRACT.md` và comment liên quan đến provider.
- Không thay đổi SQL, DBML, entity, migration hoặc endpoint.

## 2. Luồng gửi OTP

```text
Auth API
  -> service tạo/rotate OTP
  -> transaction lưu duy nhất OTP hash và trạng thái challenge
  -> transaction hoàn tất
  -> RegistrationOtpSender
  -> GmailSmtpRegistrationOtpSender
  -> JavaMailSender
  -> smtp.gmail.com:587 (STARTTLS)
  -> cập nhật delivery status bằng transaction ngắn riêng
```

Password recovery phát event trong transaction và listener bất đồng bộ chỉ chạy ở phase
`AFTER_COMMIT`. Decoy challenge không phát event. Các luồng đăng ký và liên kết email cũng gọi
SMTP sau khi transaction service đã trả về, nên không giữ transaction database trong lúc chờ Gmail.

Email dùng MIME HTML UTF-8, hiển thị tên UniShare, mã OTP, thời hạn 10 phút và cảnh báo bỏ qua
nếu người nhận không yêu cầu. OTP và App Password không được ghi log. Lỗi SMTP được chuẩn hóa
thành mã nội bộ, sau đó cơ chế exception hiện tại trả lỗi nghiệp vụ mà không lộ stack trace.

## 3. Biến môi trường

Local và Render đều cần cấu hình:

```env
MAIL_USERNAME=your_gmail_address@gmail.com
MAIL_APP_PASSWORD=your_16_character_google_app_password
MAIL_SENDER_NAME=UniShare
```

- `MAIL_USERNAME`: địa chỉ Gmail dùng làm người gửi.
- `MAIL_APP_PASSWORD`: App Password của chính tài khoản trên, lưu dưới dạng secret.
- `MAIL_SENDER_NAME`: tên hiển thị; nếu bỏ trống cấu hình thì ứng dụng dùng `UniShare`.

Trên Render, mở service Backend, vào **Environment**, thêm ba biến trên và redeploy. Không commit
App Password vào `.env`, YAML, source code, log hoặc Git.

## 4. Tạo Google App Password

1. Bật xác minh hai bước cho Google Account dùng để gửi mail.
2. Mở trang Google Account, vào **Security** và tìm **App passwords**.
3. Tạo App Password mới cho ứng dụng UniShare.
4. Sao chép mật khẩu được Google cấp và lưu vào `MAIL_APP_PASSWORD`.
5. Nếu mục App passwords không xuất hiện, kiểm tra chính sách Workspace, Advanced Protection hoặc
   yêu cầu quản trị của tài khoản.

Không dùng mật khẩu Google thông thường. Khi App Password bị lộ, thu hồi ngay và tạo mã mới.

## 5. Kiểm thử

Test tự động mặc định mock `JavaMailSender`, không kết nối Gmail:

```bash
cd BackEnd
mvn test
mvn clean package
```

Kiểm thử thủ công:

1. Cấu hình ba biến môi trường và khởi động Backend.
2. Gọi API bắt đầu đăng ký bằng một email nhận được thư.
3. Kiểm tra email có subject `Mã xác minh UniShare`, HTML tiếng Việt đúng và OTP hết hạn theo policy.
4. Xác minh OTP qua API hiện tại; kiểm tra OTP không xuất hiện trong database hoặc log.
5. Chờ hết cooldown rồi gọi resend; xác nhận OTP mới hoạt động và OTP cũ mất hiệu lực.
6. Lặp lại với Forgot Password và Link Email.
7. Thử App Password sai; client chỉ được nhận lỗi nghiệp vụ chuẩn, không có stack trace hay thông tin SMTP.

## 6. Giới hạn Gmail SMTP trong môi trường demo

- Gmail áp dụng hạn mức gửi và chống spam; hạn mức có thể khác giữa Gmail cá nhân và Google Workspace.
- Gửi nhanh hoặc gửi nhiều email giống nhau có thể bị trì hoãn, từ chối hoặc đưa vào Spam.
- App Password phụ thuộc trạng thái bảo mật và chính sách của Google Account, có thể bị thu hồi.
- Gmail SMTP phù hợp demo và lưu lượng nhỏ, không bảo đảm throughput, SLA, analytics hoặc quản lý bounce
  như dịch vụ transactional email chuyên dụng.
- Không nên dùng chung tài khoản Gmail cá nhân; nên dành riêng một tài khoản cho môi trường demo.
