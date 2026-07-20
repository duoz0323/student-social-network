# Postman Test - Auth, Onboarding, Profile, Avatar

`README.md` là nguồn sự thật cao nhất. Request, response, header, HTTP status và error code phải lấy từ `docs/data/API-CONTRACT.md`; tài liệu này chỉ hướng dẫn chuỗi thao tác kiểm thử thủ công.

## 1. Chuẩn bị môi trường

- Chạy Backend theo hướng dẫn dự án.
- Base URL mặc định: `http://localhost:8080`.
- Tạo các biến Postman: `baseUrl`, `authFlowToken`, `otpCode`, `accessToken`, `refreshToken`.
- Không đưa Access Token, Refresh Token, flow token, OTP hoặc secret vào URL/log.
- Flow token chỉ gửi bằng header `X-Auth-Flow-Token`, không dùng query parameter.

## 2. Khởi tạo đăng ký local

`POST {{baseUrl}}/api/v1/auth/registrations`

Body mẫu:

```json
{
  "identifier": "student@example.com",
  "password": "Password@1",
  "confirmPassword": "Password@1"
}
```

Kỳ vọng:

- HTTP `202 Accepted`.
- Chỉ tạo hoặc phục hồi `pending_registrations`; chưa tạo user/profile/session.
- Response có `resumed` và raw flow token; lưu tạm token vào `authFlowToken`.
- Response có `Cache-Control: no-store`.
- Gọi lại cùng identifier đang có pending hợp lệ phải trả `resumed=true`, flow token mới và không tự resend trong cooldown.
- Không gọi endpoint cũ `/api/v1/auth/register`.

Post-response script tối thiểu:

```javascript
const body = pm.response.json();
pm.environment.set("authFlowToken", body.data.flowToken);
```

## 3. Xác minh OTP và lấy JWT hệ thống

`POST {{baseUrl}}/api/v1/auth/registrations/verify`

Headers:

- `Content-Type: application/json`
- `X-Auth-Flow-Token: {{authFlowToken}}`

Body dùng `otpCode` theo contract đã chốt. OTP hợp lệ mới tạo `users`, `user_profiles`, Access Token và Refresh Token. Lưu token từ response vào `accessToken` và `refreshToken`.

Các case cần kiểm tra thêm:

- Nhập sai lần thứ 5 trả `OTP_ATTEMPTS_EXCEEDED` với HTTP 429; pending vẫn `PENDING`.
- Resend sau cooldown xoay flow token, tạo OTP mới, reset attempts và không gia hạn pending 24 giờ.
- Verify lại sau completion trả `REGISTRATION_ALREADY_COMPLETED` và không phát lại token cũ.
- Recovery/status và resend đều dùng `X-Auth-Flow-Token`; chi tiết endpoint theo `API-CONTRACT.md`.

## 4. Xem trạng thái onboarding

`GET {{baseUrl}}/api/v1/users/me/onboarding`

Headers:

- `Authorization: Bearer {{accessToken}}`

Kỳ vọng khi chưa hoàn tất:

- `profileCompleted: false`
- `nextStep: ONBOARDING_PROFILE`

Login và refresh vẫn hoạt động khi chưa onboarding. `PROFILE_NOT_COMPLETED` chỉ áp dụng khi gọi API mạng xã hội chính.

## 5. Hoàn tất onboarding

`PUT {{baseUrl}}/api/v1/users/me/onboarding`

Headers:

- `Authorization: Bearer {{accessToken}}`
- `Content-Type: application/json`

Body:

```json
{
  "displayName": "Nguyen Van A",
  "dateOfBirth": "2000-01-01",
  "bio": "Sinh viên yêu thích công nghệ"
}
```

Quy tắc:

- `displayName` và `dateOfBirth` bắt buộc.
- Người dùng phải đủ 18 tuổi tại ngày Backend xử lý.
- Avatar và bio tùy chọn.
- Ngày sinh tương lai hoặc chưa đủ 18 tuổi phải nhận lỗi nghiệp vụ tương ứng.

Kỳ vọng:

- `profileCompleted: true`
- `nextStep: FEED`
- Gọi hoàn tất lần hai trả `PROFILE_ALREADY_COMPLETED` theo contract hiện hành.

## 6. Cập nhật hồ sơ sau onboarding

`PUT {{baseUrl}}/api/v1/users/me/profile`

Headers:

- `Authorization: Bearer {{accessToken}}`
- `Content-Type: application/json`

Body:

```json
{
  "displayName": "Nguyen Van B",
  "dateOfBirth": "2000-01-01",
  "bio": "Cập nhật giới thiệu cá nhân"
}
```

Kỳ vọng không trả token mới và không đổi `profileCompleted` từ `true` về `false`.

## 7. Upload và xóa avatar

Upload: `POST {{baseUrl}}/api/v1/users/me/avatar`

- Header `Authorization: Bearer {{accessToken}}`.
- Body `form-data`, key `file`, type `File`.
- Ảnh `.jpg`, `.jpeg`, `.png` hoặc `.webp`, tối đa 10 MB.
- Response có `data.avatarUrl`, không trả `avatarPublicId`.

Xóa: `DELETE {{baseUrl}}/api/v1/users/me/avatar`

- Header `Authorization: Bearer {{accessToken}}`.
- Kỳ vọng `data.avatarUrl: null`, giữ nguyên `profileCompleted`; gọi lặp vẫn không lỗi.

## 8. Case lỗi nên kiểm tra

- Không gửi `Authorization` → `UNAUTHORIZED`.
- Dùng Refresh Token thay Access Token → `INVALID_ACCESS_TOKEN`.
- Upload file rỗng → `AVATAR_FILE_REQUIRED`.
- Upload file quá lớn → `AVATAR_FILE_TOO_LARGE`.
- Upload loại file không hỗ trợ → `AVATAR_FILE_TYPE_NOT_ALLOWED`.
- Upload file giả ảnh → `AVATAR_MIME_TYPE_INVALID`.
- Gọi API mạng xã hội chính trước onboarding → `PROFILE_NOT_COMPLETED`.
