# Auth Frontend Handoff

## 1. Quy tắc token

- Access Token gửi bằng `Authorization: Bearer <access-token>` cho protected API.
- Refresh Token chỉ gửi vào refresh/logout theo contract; không dùng gọi Feed/Post.
- Registration flow token chỉ dùng trong registration status/resend/cancel/verify theo DTO/header hiện hành.
- Social challenge token chỉ dùng tại resolve social conflict.
- Link challenge token gửi bằng `X-Auth-Flow-Token` cho verify/resend email/phone.
- Reauthentication token gửi bằng `X-Auth-Flow-Token` khi unlink.
- Google/Facebook credential chỉ gửi tới endpoint provider hoặc reauthentication tương ứng.
- Xóa token tạm khi flow hoàn tất hoặc bị terminal. Không log token và không đưa token vào URL.
- Mọi response Auth phải được coi là dữ liệu `no-store`.

## 2. Endpoint public

| Method | URL | Header chính | Body/tác dụng | Success | Error chính |
| --- | --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/registrations` | Không | `identifier`, password/confirmation | `202`, pending + flow data | validation, already pending/existing |
| POST | `/api/v1/auth/registrations/verify` | Theo DTO hiện hành | Flow token + OTP + device | `200`, Access/Refresh Token, `nextStep` | OTP/flow expired/invalid |
| POST | `/api/v1/auth/registrations/resend` | Không | Registration flow token | `200`, OTP lifecycle | cooldown/expired |
| GET | `/api/v1/auth/registrations/status` | `X-Auth-Flow-Token` | Không | `200`, pending status | invalid flow |
| POST | `/api/v1/auth/registrations/cancel` | Không | Registration flow token | `200`, terminal status | completed/invalid flow |
| POST | `/api/v1/auth/login` | Không | Identifier, password, device | `200`, Access/Refresh Token, `nextStep` | invalid credentials/BLOCKED |
| POST | `/api/v1/auth/refresh-token` | Không | Refresh Token | `200`, rotated token response | invalid/expired/revoked |
| POST | `/api/v1/auth/logout` | Không | Refresh Token | `200`, revoked/idempotent result | normalized Auth error |
| POST | `/api/v1/auth/oauth/google` | Optional registration flow header | Google ID Token + device | `200` session hoặc conflict | provider/conflict errors |
| POST | `/api/v1/auth/oauth/facebook` | Optional registration flow header | Facebook Access Token + device | `200` session hoặc conflict | provider/conflict errors |
| POST | `/api/v1/auth/registrations/resolve-social-conflict` | `X-Auth-Flow-Token` | Resolution action | `200`, next step/session | invalid/expired/used challenge |

## 3. Endpoint yêu cầu JWT

| Method | URL | Header bổ sung | Mục đích |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/reauthenticate` | Authorization | Phát hành reauthentication token ngắn hạn |
| GET | `/api/v1/users/me/auth-providers` | Authorization | Danh sách method đã mask |
| POST | `/api/v1/users/me/auth-providers/email` | Authorization | Bắt đầu link email |
| POST | `/api/v1/users/me/auth-providers/phone` | Authorization | Bắt đầu link phone |
| POST | `/api/v1/users/me/auth-providers/email/verify` | Authorization + `X-Auth-Flow-Token` | Verify link email |
| POST | `/api/v1/users/me/auth-providers/phone/verify` | Authorization + `X-Auth-Flow-Token` | Verify link phone |
| POST | `/api/v1/users/me/auth-providers/email/resend` | Authorization + `X-Auth-Flow-Token` | Resend link email OTP |
| POST | `/api/v1/users/me/auth-providers/phone/resend` | Authorization + `X-Auth-Flow-Token` | Resend link phone OTP |
| POST | `/api/v1/users/me/auth-providers/google` | Authorization | Link Google sau Backend verify |
| POST | `/api/v1/users/me/auth-providers/facebook` | Authorization | Link Facebook sau Backend verify |
| DELETE | `/api/v1/users/me/auth-providers/{provider}` | Authorization + `X-Auth-Flow-Token` | Unlink sau reauthentication; trả `204` |

User chưa hoàn tất hồ sơ vẫn được dùng Auth/onboarding theo contract, nhưng API mạng xã hội trả `PROFILE_NOT_COMPLETED`. Frontend chỉ dùng `nextStep` để điều hướng; Backend vẫn là nơi kiểm tra quyền cuối cùng.

## 4. Xử lý phía Frontend

1. Không gửi `userId` đích cho link/unlink; Backend lấy user từ JWT.
2. Không suy luận account merge từ social email.
3. Hiển thị cooldown/`Retry-After` khi nhận `429` nhưng không bỏ qua OTP cooldown từ Backend.
4. Xóa flow/challenge token sau COMPLETED, CANCELLED, EXPIRED hoặc consume thành công.
5. Không gọi API mạng xã hội bằng provider credential.
6. Không lưu provider token lâu hơn request Auth cần thiết.
7. Khi refresh thất bại, xóa phiên local và điều hướng về login.
8. Logout không vô hiệu Access Token đã phát hành; Frontend phải xóa token local ngay.
