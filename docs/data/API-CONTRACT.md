# API Contract MVP

> `README.md` là nguồn sự thật cao nhất. File này là nơi duy nhất mô tả chi tiết request, response, HTTP status, header và error code của API. Các tài liệu flow/database/UI chỉ dẫn chiếu đến contract này.

## 1. Auth

### Quy ước Auth flow token

- Tất cả endpoint dùng tiền tố `/api/v1`.
- Không sử dụng lại `POST /api/v1/auth/register`.
- Opaque token ngắn hạn mặc định dùng header sau; riêng registration resend/cancel nhận `registrationFlowToken` trong JSON body theo contract từng endpoint:

```http
X-Auth-Flow-Token: <opaque-token>
```

- Endpoint xác định purpose của token: registration, link email, social conflict hoặc reauthentication.
- Backend trả raw flow token trong response body; database chỉ lưu HMAC-SHA-256 hash.
- Không truyền flow token bằng query parameter.
- Response chứa raw flow token, Access Token hoặc Refresh Token phải có `Cache-Control: no-store` và `Pragma: no-cache`.
- Frontend chỉ lưu flow token trong memory hoặc `sessionStorage`, không dùng `localStorage`.
- Registration flow token không được rotate khi resend. Recovery mất token là luồng riêng, chưa thuộc Giai đoạn 4.

### POST `/api/v1/auth/registrations`

Request:

```json
{
  "email": "minh@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

Quy tắc:

- `email` là địa chỉ email dùng để đăng ký và phải có định dạng hợp lệ.
- Request đăng ký chỉ hỗ trợ phương thức local bằng email.
- Request đăng ký không nhận `username`, `displayName`, avatar, ngày sinh hoặc bio.
- Backend chuẩn hóa email trước khi kiểm tra trùng và lưu.
- Backend chỉ tạo hoặc resume `pending_registrations`; chưa tạo `users`, `user_profiles` hoặc JWT.
- Mật khẩu, OTP và flow token được băm trước khi lưu.
- Nếu cùng normalized email có pending hợp lệ, Backend trả `resumed=true`, rotate flow token và không tự resend OTP khi còn cooldown.
- Resume không được thay `password_hash`. Muốn đổi mật khẩu, người dùng phải cancel pending hiện tại rồi tạo registration mới.
- Không trả `ACTIVE_REGISTRATION_EXISTS` cho pending có thể resume.
- Domain idempotency bảo đảm không tạo pending còn hiệu lực trùng email.

Response `202 Accepted`:

```json
{
  "success": true,
  "message": "Yêu cầu đăng ký đã được tiếp nhận",
  "data": {
    "flowToken": "<registration-flow-token>",
    "flowType": "REGISTRATION",
    "registrationType": "EMAIL",
    "maskedIdentifier": "m***@example.com",
    "status": "PENDING",
    "resumed": false,
    "otpExpiresAt": "2026-07-19T10:10:00+07:00",
    "resendAvailableAt": "2026-07-19T10:01:00+07:00",
    "pendingExpiresAt": "2026-07-20T10:00:00+07:00",
    "nextStep": "VERIFY_OTP"
  },
  "timestamp": "2026-07-19T10:00:00+07:00"
}
```

Response có raw flow token phải gửi `Cache-Control: no-store`.

### GET `/api/v1/auth/registrations/status`

Headers:

```http
X-Auth-Flow-Token: <registration-flow-token>
```

Response `200`:

```json
{
  "success": true,
  "data": {
    "registrationType": "EMAIL",
    "maskedIdentifier": "m***@example.com",
    "status": "PENDING",
    "otpExpiresAt": "2026-07-19T10:10:00+07:00",
    "resendAvailableAt": "2026-07-19T10:01:00+07:00",
    "pendingExpiresAt": "2026-07-20T10:00:00+07:00",
    "resendCount": 0,
    "deliveryStatus": "SENT",
    "canResend": false,
    "remainingOtpAttempts": 5,
    "nextStep": "VERIFY_OTP"
  },
  "timestamp": "2026-07-19T10:00:30+07:00"
}
```

Không trả OTP, hash hoặc token mới.

Quy tắc terminal:

- `COMPLETED` trả trạng thái `COMPLETED`, không phát lại Access Token hoặc Refresh Token.
- `CANCELLED` trả trạng thái `CANCELLED`.
- `EXPIRED` trả trạng thái `EXPIRED`.
- Pending vừa hết hạn được terminalize bằng transaction có row lock trước khi trả `EXPIRED`.
- Token không tồn tại mới trả `AUTH_REGISTRATION_FLOW_INVALID`.

### POST `/api/v1/auth/registrations/verify`

Headers:

```http
Content-Type: application/json
X-Auth-Flow-Token: <registration-flow-token>
```

Request:

```json
{
  "code": "123456",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

Quy tắc:

- Backend pessimistic-lock pending row và thực hiện transaction ngắn.
- OTP hợp lệ mới tạo `users`, `user_profiles` và Refresh Token hash trong cùng transaction.
- Không gọi Brevo/provider khi đang giữ lock.
- Verify sau completion trả `AUTH_REGISTRATION_ALREADY_COMPLETED` và không phát lại token cũ.
- Deadlock chỉ được retry hữu hạn; lỗi nghiệp vụ không được retry.

Response `200`:

```json
{
  "success": true,
  "message": "Xác minh đăng ký thành công",
  "data": {
    "accessToken": "<access-token>",
    "refreshToken": "<refresh-token>",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 2592000,
    "profileCompleted": false,
    "nextStep": "ONBOARDING",
    "user": {
      "id": 1,
      "role": "USER"
    }
  },
  "timestamp": "2026-07-19T10:05:00+07:00"
}
```

### POST `/api/v1/auth/registrations/resend`

Request:

```json
{
  "registrationFlowToken": "<registration-flow-token>"
}
```

- Chỉ áp dụng với pending `PENDING` còn hiệu lực.
- Lần sai thứ 5 chỉ khóa OTP hiện tại; pending vẫn `PENDING`.
- Resend phải thỏa cooldown và rate limit lấy từ cấu hình.
- Resend sinh OTP mới, tăng `otpVersion`, reset `failedAttempts`, giữ nguyên flow token, đặt delivery state về `PENDING`, xóa delivery failure cũ và không gia hạn pending 24 giờ.
- OTP expiry mới không vượt quá pending expiry.
- Response `200` trả trạng thái, email đã che, OTP expiry, resend cooldown và pending expiry; không trả flow token mới:

```json
{
  "success": true,
  "data": {
    "status": "PENDING",
    "maskedIdentifier": "m***@example.com",
    "otpExpiresAt": "2026-07-19T10:20:00+07:00",
    "resendAvailableAt": "2026-07-19T10:11:00+07:00",
    "pendingExpiresAt": "2026-07-20T10:00:00+07:00",
    "message": "Đã phát hành OTP mới"
  },
  "timestamp": "2026-07-19T10:10:00+07:00"
}
```

Delivery được thực hiện sau khi transaction challenge đã commit:

- Provider trả thất bại chắc chắn: Backend ghi `FAILED`, có thể cho resend sớm nhưng vẫn áp dụng application rate limit.
- Timeout hoặc kết quả không chắc chắn: Backend ghi `UNKNOWN` và giữ cooldown 60 giây.
- Không log OTP, recipient đầy đủ hoặc provider payload.
- `COMPLETED`, `CANCELLED` và `EXPIRED` trả mã lỗi đúng trạng thái; chỉ token không tồn tại mới trả `AUTH_REGISTRATION_FLOW_INVALID`.

### POST `/api/v1/auth/registrations/cancel`

Request:

```json
{
  "registrationFlowToken": "<registration-flow-token>"
}
```

Cancel có domain idempotency: pending đã `CANCELLED` tiếp tục trả `200` với status `CANCELLED`.

- Pending còn hiệu lực chuyển `CANCELLED`, giải phóng active email key và xóa OTP/password secret.
- Pending đã hết hạn chuyển `EXPIRED` và trả status `EXPIRED`.
- Pending `EXPIRED` trả `200` với status `EXPIRED`.
- Pending `COMPLETED` trả `AUTH_REGISTRATION_ALREADY_COMPLETED`; không xóa user hoặc phát hành token.
- Terminal flow hash chỉ dùng để tra trạng thái/idempotency, không được verify OTP hoặc cấp JWT.

Response `200`:

```json
{
  "success": true,
  "data": {
    "status": "CANCELLED",
    "terminalAt": "2026-07-19T10:10:00+07:00",
    "message": "Đã hủy đăng ký"
  },
  "timestamp": "2026-07-19T10:10:00+07:00"
}
```

### POST `/api/v1/auth/login`

Actor:

- Khách chưa đăng nhập hoặc người dùng có phiên đã hết hạn Access Token.

Request:

```json
{
  "email": "minh@example.com",
  "password": "Password123!",
  "deviceId": "optional-device-id",
  "deviceInfo": "optional-browser-information"
}
```

Quy tắc:

- `email` là email. Backend tự xác định loại email để truy vấn đúng trường.
- Email được trim và chuẩn hóa chữ thường trước khi truy vấn.
- Email được chuẩn hóa theo utility hiện có của Backend trước khi truy vấn.
- `deviceId` và `deviceInfo` là tùy chọn, dùng để ghi nhận thông tin phiên nếu Client cung cấp.
- Chỉ tài khoản `ACTIVE` được đăng nhập.
- Tài khoản `BLOCKED` bị từ chối đăng nhập.
- Email chỉ đăng nhập được khi `email_verified_at` khác `NULL`; email chỉ đăng nhập được khi `email_verified_at` khác `NULL`.
- Tài khoản social-only có `password_hash = NULL` bị từ chối an toàn và không gọi `PasswordEncoder.matches` với hash null.
- Mật khẩu được kiểm tra bằng `PasswordEncoder`, không so sánh chuỗi thô.
- Lỗi sai email hoặc sai mật khẩu phải dùng cùng một mã lỗi để không tiết lộ tài khoản có tồn tại hay không.
- Người dùng chưa hoàn tất hồ sơ vẫn được đăng nhập; response phải trả `profileCompleted` để Frontend điều hướng.
- `PROFILE_NOT_COMPLETED` không được trả từ login; lỗi này chỉ áp dụng khi gọi API mạng xã hội chính.
- Không trả `password_hash`, `token_hash`, email hoặc dữ liệu nhạy cảm.

Ví dụ đăng nhập bằng email:

```json
{
  "password": "Password123!",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

Response 200:

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "demo-access-token",
    "refreshToken": "demo-refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 2592000,
    "profileCompleted": false,
    "nextStep": "COMPLETE_PROFILE",
    "user": {
      "id": 1,
      "role": "USER"
    }
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Điều hướng Frontend:

- `profileCompleted = false`: `nextStep = COMPLETE_PROFILE`, chuyển đến onboarding hồ sơ.
- `profileCompleted = true`: `nextStep = HOME`, chuyển đến Feed.

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `email`, thiếu `password` hoặc dữ liệu không hợp lệ. |
| 401 | `INVALID_CREDENTIALS` | Identifier không tồn tại hoặc mật khẩu không đúng. |
| 403 | `USER_BLOCKED` | Tài khoản tồn tại, mật khẩu đúng nhưng tài khoản bị khóa. |
| 403 | `AUTH_IDENTIFIER_NOT_VERIFIED` | Email dùng để đăng nhập chưa được xác minh. |
| 403 | `AUTH_PASSWORD_LOGIN_NOT_AVAILABLE` | Tài khoản social-only chưa có mật khẩu local. |
| 500 | `AUTH_REFRESH_TOKEN_CREATION_FAILED` | Không thể tạo phiên Refresh Token. |
| 500 | `AUTH_LOGIN_FAILED` | Không thể hoàn tất phiên đăng nhập. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

### POST `/api/v1/auth/refresh-token`

Actor:

- Người dùng có Refresh Token còn hiệu lực.

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

Quy tắc:

- Refresh Token là chuỗi ngẫu nhiên opaque, không chứa thông tin người dùng và không được parse như JWT.
- Backend chỉ lưu và truy vấn SHA-256 hash của Refresh Token, không lưu token thô.
- Refresh Token đã bị thu hồi hoặc hết hạn không được cấp Access Token mới.
- Tài khoản sở hữu token phải còn `ACTIVE`.
- Người dùng chưa hoàn tất hồ sơ vẫn được refresh token.
- `PROFILE_NOT_COMPLETED` không được trả từ refresh token.
- Mỗi lần refresh thành công, Backend khóa phiên cũ, thu hồi token cũ và trả cặp Access Token/Refresh Token mới.
- Client phải thay thế Refresh Token đang lưu bằng giá trị mới trong cùng response.

Response 200:

```json
{
  "success": true,
  "message": "Làm mới Access Token thành công",
  "data": {
    "accessToken": "new-demo-access-token",
    "refreshToken": "new-demo-refresh-token",
    "tokenType": "Bearer",
    "accessTokenExpiresIn": 900,
    "refreshTokenExpiresIn": 2592000,
    "profileCompleted": false
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `refreshToken`. |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh Token sai định dạng hoặc không tồn tại trong database. |
| 401 | `REFRESH_TOKEN_EXPIRED` | Refresh Token đã hết hạn. |
| 401 | `REFRESH_TOKEN_REVOKED` | Refresh Token đã bị thu hồi. |
| 403 | `USER_BLOCKED` | Tài khoản sở hữu token đã bị khóa. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

### POST `/api/v1/auth/logout`

Actor:

- Người dùng muốn đăng xuất khỏi phiên hiện tại.

Request:

```json
{
  "refreshToken": "demo-refresh-token"
}
```

Quy tắc:

- Backend hash Refresh Token thô bằng SHA-256 rồi tìm bản ghi tương ứng.
- Chỉ Refresh Token của phiên hiện tại bị thu hồi.
- Không thu hồi toàn bộ phiên khác của cùng người dùng.
- Không yêu cầu hồ sơ đã hoàn tất.
- Không trả token hoặc token hash trong response.
- Có thể trả thành công theo hướng idempotent để không tiết lộ trạng thái tồn tại của token.

Response 200:

```json
{
  "success": true,
  "message": "Đăng xuất thành công",
  "data": {
    "loggedOut": true
  },
  "timestamp": "2026-06-21T10:00:00"
}
```

Error:

| HTTP status | Code | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Request thiếu `refreshToken`. |
| 401 | `INVALID_REFRESH_TOKEN` | Refresh Token sai định dạng hoặc không tồn tại trong database. |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống ngoài dự kiến. |

### POST `/api/v1/auth/oauth/google`

Request:

```json
{
  "idToken": "<google-id-token>",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

### POST `/api/v1/auth/oauth/facebook`

Request:

```json
{
  "accessToken": "<facebook-access-token>",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

Quy tắc chung cho social authentication:

- Backend tự xác minh provider token và dùng provider user ID bất biến.
- Không lưu raw provider token.
- Provider đã link phải đăng nhập về đúng `users.id`.
- Facebook không trả email vẫn được tạo provider-only user; không tạo email giả hoặc placeholder.
- `providerEmail` nullable và user mới vẫn phải hoàn tất onboarding.
- Social email trùng user `ACTIVE` nhưng provider chưa link không được tự link hoặc tạo user thứ hai; trả `SOCIAL_ACCOUNT_CONFLICT`.
- Với `ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER`, Backend chỉ trả hướng dẫn đăng nhập tài khoản hiện có hoặc bắt đầu account recovery; conflict này không được tự resolve thành provider link.
- Nếu request tham gia luồng pending, gửi registration flow token bằng `X-Auth-Flow-Token`.
- Thành công trả token response giống login và gửi `Cache-Control: no-store`.

Social conflict response `409`:

```json
{
  "success": false,
  "code": "SOCIAL_PENDING_CONFLICT",
  "message": "Cần lựa chọn cách tiếp tục đăng ký",
  "details": {
    "flowToken": "<social-conflict-token>",
    "flowType": "SOCIAL_CONFLICT",
    "conflictType": "PENDING_EMAIL_MISMATCH",
    "allowedActions": [
      "CONTINUE_OTP",
      "CANCEL_PENDING_AND_CONTINUE_SOCIAL"
    ],
    "expiresIn": 300
  },
  "timestamp": "2026-07-19T10:00:00+07:00"
}
```

Social conflict token là opaque, một lần, TTL 5 phút và chỉ lưu dạng hash. Response phải gửi `Cache-Control: no-store`.

Allowed actions theo conflict type:

- `PENDING_EMAIL_MISMATCH` và `PENDING_EMAIL_REQUIRES_CANCEL`: `CONTINUE_OTP` hoặc `CANCEL_PENDING_AND_CONTINUE_SOCIAL`.
- `ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER`: `LOGIN_EXISTING_ACCOUNT` hoặc `START_ACCOUNT_RECOVERY`; không có action tự link provider.

### POST `/api/v1/auth/registrations/resolve-social-conflict`

Headers:

```http
X-Auth-Flow-Token: <social-conflict-token>
```

Request:

```json
{
  "action": "CANCEL_PENDING_AND_CONTINUE_SOCIAL",
  "deviceId": "optional-device-id",
  "deviceInfo": "Chrome on Windows"
}
```

- `CONTINUE_OTP` giữ pending và trả trạng thái tiếp tục OTP, không cấp JWT.
- `CANCEL_PENDING_AND_CONTINUE_SOCIAL` chỉ hủy pending sau khi social identity đã được Backend xác minh và challenge còn hiệu lực.
- Pending email cùng verified social email được hoàn tất thành một user, giữ local method và link provider mà không trả conflict.
- Pending email chuyển social không mang email chưa verified sang user social.

Response `200` với `CONTINUE_OTP`:

```json
{
  "success": true,
  "data": {
    "resolved": true,
    "nextStep": "VERIFY_OTP"
  },
  "timestamp": "2026-07-19T10:02:00+07:00"
}
```

Với hành động tiếp tục social thành công, response dùng đúng session response của login/social Auth và có `Cache-Control: no-store`. Conflict token bị consume trong cả hai trường hợp thành công và không được dùng lại.

### POST `/api/v1/auth/reauthenticate`

API này yêu cầu JWT và chỉ dùng trước thao tác bảo mật nhạy cảm, trước mắt là unlink auth method. User đích luôn lấy từ `SecurityContext`, không nhận `userId` từ Client.

Request dùng một trong các bằng chứng đã liên kết, đồng thời bind rõ purpose và phương thức đích. Ví dụ local password:

```json
{
  "method": "PASSWORD",
  "purpose": "UNLINK_AUTH_METHOD",
  "targetMethod": "GOOGLE",
  "password": "current-password"
}
```

Hoặc provider credential:

```json
{
  "method": "GOOGLE",
  "purpose": "UNLINK_AUTH_METHOD",
  "targetMethod": "EMAIL",
  "providerCredential": "<google-id-token>"
}
```

Facebook dùng cùng trường `providerCredential`:

```json
{
  "method": "FACEBOOK",
  "purpose": "UNLINK_AUTH_METHOD",
  "targetMethod": "GOOGLE",
  "providerCredential": "<facebook-access-token>"
}
```

Validation:

- `method` chỉ nhận `PASSWORD`, `GOOGLE` hoặc `FACEBOOK`.
- `purpose` bắt buộc và hiện chỉ nhận `UNLINK_AUTH_METHOD`.
- `targetMethod` bắt buộc, nhận `EMAIL`, `EMAIL`, `GOOGLE` hoặc `FACEBOOK`; Backend không tự suy ra target từ proof method.
- `PASSWORD` bắt buộc có `password` và không được gửi `providerCredential`.
- `GOOGLE`/`FACEBOOK` bắt buộc có `providerCredential` và không được gửi `password`.
- Google/Facebook credential được Backend xác minh ngoài database transaction; provider identity đã xác minh phải thuộc đúng user hiện tại.
- Giai đoạn hiện tại chưa hỗ trợ OTP reauthentication vì contract và schema không có OTP fields.

Backend tự xác minh bằng chứng. Response `200`:

```json
{
  "success": true,
  "data": {
    "reauthenticationToken": "<reauthentication-token>",
    "method": "PASSWORD",
    "purpose": "UNLINK_AUTH_METHOD",
    "targetMethod": "GOOGLE",
    "expiresAt": "2026-07-19T10:05:00+07:00",
    "status": "ACTIVE"
  },
  "timestamp": "2026-07-19T10:00:00+07:00"
}
```

Reauthentication token là opaque, một lần, TTL 5 phút, chỉ lưu HMAC hash và được gửi lại cho endpoint nhạy cảm bằng `X-Auth-Flow-Token`. Token chỉ hợp lệ cho đúng user, `purpose` và `targetMethod`. Một user chỉ có một challenge `ACTIVE` cho cùng scope; challenge mới làm challenge cũ mất hiệu lực. Proof có thể là bất kỳ auth method hợp lệ nào của chính user và không bắt buộc trùng method sắp unlink. Response có `Cache-Control: no-store` và không trả credential, token hash hoặc provider user ID.

### GET `/api/v1/users/me/auth-providers`

Response `200` chỉ trả trạng thái phương thức đăng nhập, email đã che, verified state, linked time và `canUnlink`; không trả provider user ID hoặc dữ liệu nhạy cảm:

```json
{
  "success": true,
  "data": {
    "methods": [
      {
        "type": "EMAIL",
        "maskedIdentifier": "m***@example.com",
        "verified": true,
        "linkedAt": "2026-07-19T10:00:00+07:00",
        "canUnlink": false
      },
      {
        "type": "GOOGLE",
        "maskedIdentifier": null,
        "verified": true,
        "linkedAt": "2026-07-19T10:00:00+07:00",
        "canUnlink": true
      }
    ]
  },
  "timestamp": "2026-07-19T10:05:00+07:00"
}
```

### Link email

```http
POST /api/v1/users/me/auth-providers/email
POST /api/v1/users/me/auth-providers/email/verify
POST /api/v1/users/me/auth-providers/email/resend
```

- Initiate nhận `{ "email": "student@example.com" }` và trả `202`:

```json
{
  "success": true,
  "data": {
    "flowToken": "<link-email-flow-token>",
    "flowType": "LINK_EMAIL",
    "maskedIdentifier": "s***@example.com",
    "otpExpiresAt": "2026-07-19T10:10:00+07:00",
    "resendAvailableAt": "2026-07-19T10:01:00+07:00"
  },
  "timestamp": "2026-07-19T10:00:00+07:00"
}
```

- Verify nhận link flow token qua `X-Auth-Flow-Token` và body `{ "code": "123456" }`.
- Resend nhận link flow token qua header, reset attempts, rotate token và áp dụng cooldown/rate limit cấu hình.
- Verify thành công trả `200` với method object như trong `GET /auth-providers`; resend trả `200` với flow token mới và OTP metadata như initiate. Mọi response chứa flow token có `Cache-Control: no-store`.
- Link challenge có TTL 15 phút. Resend không gia hạn challenge và OTP expiry không được vượt challenge expiry.

### Link email

```http
POST /api/v1/users/me/auth-providers/email
POST /api/v1/users/me/auth-providers/email/verify
POST /api/v1/users/me/auth-providers/email/resend
```


Liên kết email dùng verification challenge riêng, không tái sử dụng `pending_registrations`.

### Link Google/Facebook

```http
POST /api/v1/users/me/auth-providers/google
POST /api/v1/users/me/auth-providers/facebook
```

- Request lần lượt nhận `idToken` hoặc `accessToken`.
- User đích luôn lấy từ JWT hiện tại.
- Đã link vào chính user trả trạng thái hiện tại; đã thuộc user khác trả `PROVIDER_LINKED_TO_ANOTHER_USER`.
- Thành công trả `200` với method object như trong `GET /auth-providers`; không trả provider credential hoặc provider user ID.

### DELETE `/api/v1/users/me/auth-providers/{provider}`

Yêu cầu JWT và reauthentication token qua `X-Auth-Flow-Token`.

- Không cho unlink phương thức cuối cùng.
- Auth method không tồn tại trả `AUTH_METHOD_NOT_LINKED`, không mặc định `204`.
- Thành công trả `204 No Content`.
- Nếu không còn local email hợp lệ nhưng còn social provider, đặt `password_hash = NULL` trong cùng transaction.
- Thu hồi các session khác sau thay đổi auth method là hạng mục P1 riêng; không tự triển khai cho tới khi contract được chốt.

### Domain idempotency

- Không dùng generic `Idempotency-Key` trong MVP.
- Start registration resume pending hợp lệ và rotate flow token; không tự resend trong cooldown.
- Verify sau completion trả `AUTH_REGISTRATION_ALREADY_COMPLETED`, không phát lại token.
- Cancel đã `CANCELLED` tiếp tục trả thành công.
- Resend lặp trong cooldown trả `AUTH_OTP_RESEND_TOO_SOON`.
- Unlink auth method không tồn tại trả `AUTH_METHOD_NOT_LINKED`.

### Password Recovery

Password Recovery chỉ áp dụng cho tài khoản `ACTIVE` đã có `password_hash` và có EMAIL tương ứng đã verified. Start trả response trung tính cho cả email tồn tại, không tồn tại và không đủ điều kiện. Social-only không được dùng luồng này để tạo mật khẩu lần đầu.

- `POST /api/v1/auth/password-recovery`
  - Body: `{ "email": "student@example.com", "deviceId": "optional" }`.
  - `202`: `{ "accepted": true, "flowType": "PASSWORD_RECOVERY", "recoveryFlowToken": "...", "otpExpiresAt": "...", "resendAvailableAt": "...", "challengeExpiresAt": "..." }`.
- `POST /api/v1/auth/password-recovery/verify`
  - Header: `X-Auth-Flow-Token` chứa recovery flow token.
  - Body: `{ "code": "123456" }`.
  - Thành công trả `{ "resetAuthorizedToken": "...", "resetTokenExpiresAt": "..." }`; decoy không bao giờ phát reset token.
- `POST /api/v1/auth/password-recovery/resend`
  - Header: `X-Auth-Flow-Token`; không có body.
  - Xoay flow token và OTP, trả timestamps mới; OTP không vượt challenge expiry.
- `POST /api/v1/auth/password-recovery/complete`
  - Header: `X-Auth-Flow-Token` chứa reset-authorized token.
  - Body: `{ "newPassword": "...", "confirmPassword": "..." }`.
  - Thành công trả `{ "completed": true }`, không tự đăng nhập, không cấp JWT và thu hồi toàn bộ Refresh Token.

Không có status/cancel API. Challenge 15 phút, OTP 10 phút, resend cooldown 60 giây, tối đa 5 lần sai, reset token 5 phút. Token không được đặt trong body/URL/log; delivery chạy bất đồng bộ sau commit và decoy không enqueue delivery.

Complete thu hồi toàn bộ Refresh Token trong cùng transaction. Access Token stateless đã phát hành vẫn có hiệu lực tới expiry vì production hiện chưa có `tokenVersion`.

### Error code Auth

| HTTP | Code | Ý nghĩa |
| ---: | --- | --- |
| 400 | `VALIDATION_ERROR` | Request không hợp lệ. |
| 400 | `INVALID_IDENTIFIER` | Identifier không hợp lệ. |
| 400 | `OTP_INVALID` | OTP sai; `details` trả `remainingAttempts`. |
| 401 | `AUTH_FLOW_TOKEN_REQUIRED` | Endpoint yêu cầu nhưng thiếu `X-Auth-Flow-Token`. |
| 401 | `AUTH_FLOW_TOKEN_INVALID` | Flow token sai, không đúng purpose hoặc đã bị rotate. |
| 401 | `AUTH_REGISTRATION_FLOW_INVALID` | Flow token sai hoặc không hợp lệ cho endpoint. |
| 401 | `INVALID_CREDENTIALS` | Login local thất bại. |
| 403 | `AUTH_IDENTIFIER_NOT_VERIFIED` | Email đăng nhập local chưa được xác minh. |
| 403 | `AUTH_PASSWORD_LOGIN_NOT_AVAILABLE` | Social-only account chưa có mật khẩu local hợp lệ. |
| 401 | `INVALID_PROVIDER_TOKEN` | Provider token không hợp lệ. |
| 401 | `PROVIDER_TOKEN_EXPIRED` | Provider token hết hạn. |
| 401 | `REAUTHENTICATION_FAILED` | Bằng chứng xác thực lại không hợp lệ. |
| 403 | `USER_BLOCKED` | Tài khoản bị khóa. |
| 409 | `IDENTIFIER_ALREADY_REGISTERED` | Identifier đã thuộc user thật. |
| 409 | `AUTH_REGISTRATION_CANCELLED` | Pending đã hủy. |
| 409 | `AUTH_REGISTRATION_ALREADY_COMPLETED` | Pending đã hoàn tất. |
| 409 | `REGISTRATION_STATE_CONFLICT` | Trạng thái pending không cho phép thao tác. |
| 409 | `SOCIAL_ACCOUNT_CONFLICT` | Social email trùng ACTIVE user chưa link. |
| 409 | `SOCIAL_PENDING_CONFLICT` | Pending và social identity cần người dùng lựa chọn. |
| 409 | `PROVIDER_ALREADY_LINKED` | Provider đã link vào chính user. |
| 409 | `PROVIDER_LINKED_TO_ANOTHER_USER` | Provider thuộc user khác. |
| 409 | `IDENTIFIER_LINKED_TO_ANOTHER_USER` | Email thuộc user khác. |
| 409 | `LAST_AUTH_METHOD` | Không được gỡ phương thức cuối cùng. |
| 409 | `AUTH_METHOD_NOT_LINKED` | Phương thức cần gỡ không tồn tại. |
| 410 | `REGISTRATION_EXPIRED` | Pending hết hạn. |
| 410 | `OTP_EXPIRED` | OTP hiện tại hết hạn. |
| 410 | `OTP_ALREADY_USED` | OTP đã dùng. |
| 410 | `AUTH_FLOW_TOKEN_EXPIRED` | Flow token đã hết TTL. |
| 410 | `AUTH_FLOW_TOKEN_ALREADY_USED` | Flow token một lần đã được consume. |
| 410 | `SOCIAL_CONFLICT_EXPIRED` | Social conflict token hết hạn hoặc đã dùng. |
| 429 | `OTP_ATTEMPTS_EXCEEDED` | OTP hiện tại đã đủ 5 lần sai. |
| 429 | `AUTH_OTP_RESEND_TOO_SOON` | Chưa hết cooldown; trả `retryAfterSeconds`. |
| 429 | `OTP_RATE_LIMITED` | Vượt rate limit cấu hình. |
| 502 | `OTP_DELIVERY_FAILED` | Email provider xác nhận gửi thất bại; challenge vẫn có thể tiếp tục/resend theo policy. |
| 503 | `OTP_DELIVERY_UNKNOWN` | Không xác định chắc kết quả gửi; giữ cooldown để tránh phát nhiều OTP. |
| 401 | `AUTH_PASSWORD_RECOVERY_FLOW_INVALID` | Recovery flow token thiếu, sai hoặc đã bị xoay. |
| 410 | `AUTH_PASSWORD_RECOVERY_FLOW_EXPIRED` | Challenge Password Recovery đã hết hạn. |
| 400 | `AUTH_PASSWORD_RECOVERY_OTP_INVALID` | OTP recovery không hợp lệ; response không tiết lộ eligibility. |
| 410 | `AUTH_PASSWORD_RECOVERY_OTP_EXPIRED` | OTP recovery đã hết hạn. |
| 429 | `AUTH_PASSWORD_RECOVERY_OTP_ATTEMPTS_EXCEEDED` | OTP recovery đã đủ 5 lần sai. |
| 429 | `AUTH_PASSWORD_RECOVERY_RESEND_TOO_SOON` | Resend đang trong cooldown. |
| 401 | `AUTH_PASSWORD_RESET_TOKEN_INVALID` | Reset-authorized token không hợp lệ. |
| 410 | `AUTH_PASSWORD_RESET_TOKEN_EXPIRED` | Reset-authorized token đã hết hạn. |
| 409 | `AUTH_PASSWORD_RESET_TOKEN_USED` | Reset-authorized token đã được dùng. |
| 400 | `AUTH_PASSWORD_MUST_BE_DIFFERENT` | Mật khẩu mới trùng mật khẩu hiện tại. |

`PROFILE_NOT_COMPLETED` không phải lỗi login hoặc refresh. Mã này chỉ dùng khi user chưa hoàn tất onboarding gọi API mạng xã hội chính.

## 2. User

### GET `/api/v1/users/me/onboarding`

Response 200:

```json
{
  "profileCompleted": false,
  "displayName": null,
  "avatarUrl": null,
  "dateOfBirth": null,
  "bio": null
}
```

### PUT `/api/v1/users/me/onboarding`

Request:

```json
{
  "displayName": "Nguyễn Hoàng Minh",
  "dateOfBirth": "2000-01-01",
  "bio": null
}
```

Quy tắc:

- `displayName` và `dateOfBirth` bắt buộc để hoàn tất hồ sơ.
- `bio` là tùy chọn; avatar được quản lý qua API multipart riêng.
- `dateOfBirth` không được nằm trong tương lai và người dùng phải đủ 18 tuổi tại ngày Backend xử lý.
- API này vừa lưu dữ liệu hợp lệ vừa cập nhật `profile_completed_at` trong cùng transaction.
- `users.status = ACTIVE` không đồng nghĩa hồ sơ đã hoàn tất.
- API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED` khi `profile_completed_at` còn `NULL`.

Ví dụ lỗi:

```json
{
  "success": false,
  "code": "PROFILE_NOT_COMPLETED",
  "message": "Bạn cần hoàn tất hồ sơ trước khi sử dụng chức năng này",
  "timestamp": "2026-06-21T10:00:00"
}
```

### GET `/api/v1/users/{userId}`

### PUT `/api/v1/users/me/profile`

Request:

```json
{
  "displayName": "Nguyễn Hoàng Minh",
  "dateOfBirth": "2000-01-01",
  "bio": "Sinh viên CNTT"
}
```

Quy tắc:

- Chỉ dùng sau khi onboarding hoàn tất.
- `displayName` và `dateOfBirth` bắt buộc; ngày sinh vẫn phải thỏa điều kiện đủ 18 tuổi tại ngày cập nhật.
- Avatar không cập nhật qua JSON.

### POST `/api/v1/users/me/avatar`

- Content-Type: `multipart/form-data`.
- Part bắt buộc: `file`.
- Hỗ trợ JPG, JPEG, PNG, WEBP, tối đa 10 MB.

### DELETE `/api/v1/users/me/avatar`

- Xóa URL và `public_id` trong database, sau đó xóa file cũ trên Cloudinary.

Route UI tương ứng:

- Hồ sơ cá nhân: `/profile/me`.
- Hồ sơ người dùng khác: `/profile/:userId`.

## 3. Follow

### POST `/api/v1/users/{userId}/follow`

### DELETE `/api/v1/users/{userId}/follow`

### GET `/api/v1/users/{userId}/followers?page=0&size=20`

### GET `/api/v1/users/{userId}/following?page=0&size=20`

## 4. Post

### POST `/api/v1/posts`

Request:

```json
{
  "content": "Nội dung bài viết",
  "imageUrls": [
    "https://example.com/image-1.jpg"
  ],
  "hashtags": [
    "sinhvien",
    "hoctap"
  ]
}
```

### GET `/api/v1/posts/{postId}`

### PATCH `/api/v1/posts/{postId}`

Request:

```json
{
  "content": "Nội dung đã sửa",
  "hashtags": [
    "doan"
  ]
}
```

### DELETE `/api/v1/posts/{postId}`

## 5. Interaction

### POST `/api/v1/posts/{postId}/likes`

### DELETE `/api/v1/posts/{postId}/likes`

### POST `/api/v1/posts/{postId}/comments`

Request:

```json
{
  "content": "Bình luận mẫu"
}
```

### GET `/api/v1/posts/{postId}/comments?page=0&size=20`

### DELETE `/api/v1/comments/{commentId}`

### POST `/api/v1/posts/{postId}/save`

### DELETE `/api/v1/posts/{postId}/save`

### GET `/api/v1/users/me/saved-posts?page=0&size=20`

## 6. Feed

### GET `/api/v1/feeds/for-you?page=0&size=20`

### GET `/api/v1/feeds/following?page=0&size=20`

## 7. Search

### GET `/api/v1/search/users?q=minh&page=0&size=20`

### GET `/api/v1/search/posts?q=hoctap&page=0&size=20`

## 8. Report

### POST `/api/v1/posts/{postId}/reports`

Request:

```json
{
  "reason": "SPAM",
  "description": "Nội dung quảng cáo lặp lại."
}
```

## 9. Admin

### GET `/api/v1/admin/users?page=0&size=20`

### PATCH `/api/v1/admin/users/{userId}/status`

```json
{
  "status": "BLOCKED"
}
```

### GET `/api/v1/admin/posts?page=0&size=20`

### PATCH `/api/v1/admin/posts/{postId}/status`

```json
{
  "status": "HIDDEN"
}
```

### GET `/api/v1/admin/reports?status=PENDING&page=0&size=20`

### PATCH `/api/v1/admin/reports/{reportId}`

```json
{
  "status": "RESOLVED",
  "hidePost": true
}
```

