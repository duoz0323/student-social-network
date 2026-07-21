# Postman Test - Onboarding, Profile, Avatar

## 1. Chuẩn bị môi trường

- Đóng và mở lại IntelliJ IDEA sau khi cấu hình biến môi trường Cloudinary bằng `setx`.
- Chạy Backend từ IntelliJ hoặc terminal.
- Base URL mặc định: `http://localhost:8080`.
- Không đưa Access Token, Refresh Token hoặc Cloudinary secret vào URL.

## 2. Đăng ký và lấy Access Token

`POST {{baseUrl}}/api/v1/auth/register`

Headers:

- `Content-Type: application/json`

Body:

```json
{
  "identifier": "student@example.com",
  "password": "Password@1",
  "confirmPassword": "Password@1"
}
```

Lưu `data.accessToken` vào biến Postman `accessToken`.

## 3. Xem trạng thái onboarding

`GET {{baseUrl}}/api/v1/users/me/onboarding`

Headers:

- `Authorization: Bearer {{accessToken}}`

Kỳ vọng khi chưa hoàn tất:

- `profileCompleted: false`
- `nextStep: ONBOARDING_PROFILE`

## 4. Hoàn tất onboarding

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

Kỳ vọng:

- `profileCompleted: true`
- `nextStep: FEED`

Gọi lại endpoint này lần hai phải nhận lỗi `PROFILE_ALREADY_COMPLETED`.

## 5. Cập nhật hồ sơ sau onboarding

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

Kỳ vọng:

- Không trả token mới.
- Không thay đổi `profileCompleted` từ `true` về `false`.

## 6. Upload avatar

`POST {{baseUrl}}/api/v1/users/me/avatar`

Headers:

- `Authorization: Bearer {{accessToken}}`

Body:

- Chọn `form-data`.
- Key: `file`.
- Type: `File`.
- Value: chọn ảnh `.jpg`, `.jpeg`, `.png` hoặc `.webp`, tối đa 10 MB.

Kỳ vọng:

- Response có `data.avatarUrl`.
- Không có `avatarPublicId` trong response.

## 7. Xóa avatar

`DELETE {{baseUrl}}/api/v1/users/me/avatar`

Headers:

- `Authorization: Bearer {{accessToken}}`

Kỳ vọng:

- `data.avatarUrl: null`
- `profileCompleted` giữ nguyên.
- Gọi nhiều lần liên tiếp vẫn không lỗi.

## 8. Case lỗi nên kiểm tra

- Không gửi `Authorization` -> `UNAUTHORIZED`.
- Dùng Refresh Token thay Access Token -> `INVALID_ACCESS_TOKEN`.
- Upload file rỗng -> `AVATAR_FILE_REQUIRED`.
- Upload file lớn hơn 10 MB -> `AVATAR_FILE_TOO_LARGE`.
- Upload file đuôi không hỗ trợ -> `AVATAR_FILE_TYPE_NOT_ALLOWED`.
- Upload file giả ảnh -> `AVATAR_MIME_TYPE_INVALID`.
- Gọi `PUT /profile` trước onboarding -> `PROFILE_NOT_COMPLETED`.
