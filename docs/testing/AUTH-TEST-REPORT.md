# Báo cáo kiểm thử module Auth

## 1. Mục tiêu và nguồn đối chiếu

Báo cáo chốt kết quả hồi quy Auth trước tích hợp Frontend. Kết quả được đối chiếu với `README.md`, `docs/data/API-CONTRACT.md`, SQL, DBML, source, SecurityConfig và test hiện hành ngày 20/07/2026. Báo cáo không coi test bị skip là PASS.

## 2. Môi trường

| Thành phần | Giá trị |
| --- | --- |
| Hệ điều hành | Windows 11 10.0, amd64 |
| Java | Oracle JDK 21.0.9 LTS |
| Maven | 3.9.14 |
| Spring Boot | 4.1.0 theo Maven build |
| Database test | Không được cấu hình |
| `AUTH_TEST_DB_URL` | Thiếu |
| `AUTH_TEST_DB_USERNAME` | Thiếu |
| `AUTH_TEST_DB_PASSWORD` | Thiếu |

Không sử dụng database development/production và không gọi Google/Facebook thật.

## 3. Kết quả quality gate

| Lệnh | Run | Passed | Failed | Errors | Skipped | Kết quả |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| Nhóm Auth và Security | 189 | 177 | 0 | 0 | 12 | PASS có điều kiện |
| `mvn test` | 210 | 198 | 0 | 0 | 12 | BUILD SUCCESS |
| `mvn clean package` | 210 | 198 | 0 | 0 | 12 | BUILD SUCCESS |

## 4. Coverage thực tế

| Mã | Chức năng | Tiền điều kiện / đầu vào đại diện | Kết quả mong đợi | Kết quả thực tế | Trạng thái |
| --- | --- | --- | --- | --- | --- |
| AUTH-REG-01 | Start email/phone registration | Identifier và password hợp lệ | Chỉ tạo pending, lưu hash | Service/entity test pass | PASS |
| AUTH-OTP-01 | Verify OTP | Flow token và OTP hợp lệ | Tạo user/profile/token trong transaction | Unit và transaction test pass | PASS |
| AUTH-OTP-02 | OTP sai/hết hạn/quá số lần | Challenge tương ứng | Không tạo user/token | Lifecycle/verification test pass | PASS |
| AUTH-OTP-03 | Resend/cancel/status | Pending ở các trạng thái | Cooldown và lifecycle đúng | Lifecycle test pass | PASS |
| AUTH-LOGIN-01 | Login email/phone | User ACTIVE, verified, đúng password | Cấp JWT và refresh token | `AuthServiceImplTest` pass | PASS |
| AUTH-LOGIN-02 | Login thất bại/BLOCKED/social-only | Credential hoặc trạng thái không hợp lệ | Error chuẩn hóa, không tạo token | Service test pass | PASS |
| AUTH-SESSION-01 | Refresh rotation/logout | Refresh token hợp lệ/cũ/revoked | Rotate hoặc revoke nguyên tử | Unit test pass | PASS |
| AUTH-GOOGLE-01 | Google verification/provisioning | Mocked Google credential | Verify claim, không lưu raw token | Verifier/transaction test pass | PASS |
| AUTH-FACEBOOK-01 | Facebook verification/provisioning | Mocked Facebook credential | Verify app/user, không tạo email giả | Verifier/service test pass | PASS |
| AUTH-SOCIAL-01 | Social conflict | Challenge và action hợp lệ/không hợp lệ | Không tự merge ACTIVE user | Resolution test pass | PASS |
| AUTH-METHOD-01 | List/link auth methods | JWT user hiện tại | Mask identifier, không lộ provider ID | Management/link test pass | PASS |
| AUTH-REAUTH-01 | Reauthentication | Password/provider proof | Token opaque, hash-at-rest, TTL/binding đúng | Controller/service/domain test pass | PASS |
| AUTH-UNLINK-01 | Unlink | Challenge đúng, còn phương thức khác | Gỡ đúng method, consume challenge | Service/controller test pass | PASS |
| AUTH-CLEAN-01 | Cleanup | Challenge/token hết hạn | Expire/delete theo batch | Cleanup test pass | PASS |
| AUTH-RATE-01 | Rate limit | Vượt fixed-window quota | 429, Retry-After, opaque key | Filter/limiter test pass | PASS |
| AUTH-SEC-01 | Public/protected paths | Exact method và URI | Chỉ Auth public được permit | Security path/JWT filter test pass | PASS |
| AUTH-DATA-01 | Mapping/hash/constraint contract | Entity và schema metadata | Mapping/hash không lộ secret | Contract/entity test pass | PASS |

## 5. Transaction rollback

Đã có test unit/Mockito cho lỗi trong verification, refresh-token issuance, reauthentication challenge và các service transaction. `RegistrationVerificationRollbackMySqlIntegrationTest` tồn tại nhưng bị skip do thiếu database test. Chưa có một bộ MySQL rollback đầy đủ cho mọi điểm lỗi liệt kê trong đặc tả Giai đoạn 12; vì vậy rollback thực tế trên MySQL chưa được xác nhận toàn diện.

## 6. Concurrency MySQL

Các test conditional hiện có:

| Test | Kịch bản | Kết quả |
| --- | --- | --- |
| `RegistrationLifecycleMySqlIntegrationTest` | Lifecycle pending | SKIPPED – thiếu `AUTH_TEST_DB_URL` |
| `RegistrationVerificationMySqlIntegrationTest` | Verify/pending concurrency | SKIPPED – thiếu `AUTH_TEST_DB_URL` |
| `RegistrationVerificationRollbackMySqlIntegrationTest` | Rollback verification | SKIPPED – thiếu `AUTH_TEST_DB_URL` |
| `RefreshTokenConcurrencyMySqlIntegrationTest` | Hai thao tác cùng refresh token | SKIPPED – thiếu `AUTH_TEST_DB_URL` |
| `ReauthenticationConcurrencyMySqlIntegrationTest` | Hai thao tác cùng challenge | SKIPPED – thiếu `AUTH_TEST_DB_URL` |
| `AuthRepositoryMySqlIntegrationTest` | Repository locking/schema | SKIPPED – thiếu `AUTH_TEST_DB_URL` |

Chưa có đủ 15 kịch bản MySQL concurrency được yêu cầu ở Giai đoạn 12, gồm social provisioning, social/local race, link/unlink và hai unlink khác method. Đây là blocker của xác nhận readiness tuyệt đối.

## 7. Security, error handling và dữ liệu nhạy cảm

- Public Auth path dùng tập URI exact; reauthenticate và toàn bộ `/users/me/auth-providers` yêu cầu JWT.
- JWT filter từ chối user `BLOCKED` trên protected API và không ghi Authorization header.
- Response Auth thành công dùng `Cache-Control: no-store`; security/rate-limit error cũng dùng `no-store`.
- Global exception handler không trả stack trace, SQL exception, constraint name hoặc exception message nội bộ.
- Quét tĩnh không phát hiện lời gọi log/console chứa password, OTP, token, Authorization hoặc secret trong source Auth.
- Secret cấu hình tham chiếu environment variable; không phát hiện secret thật trong `application.yaml`.
- Rate-limit key là SHA-256 của user/IP và endpoint, không chứa request body hoặc raw credential.

## 8. Audit API và database

- Controller Auth có 12 endpoint `/api/v1/auth/**`, một endpoint reauthentication và 10 endpoint quản lý auth method; method/path khớp contract hiện hành.
- Registration flow token, social challenge token và reauthentication token dùng đúng header/body theo contract hiện tại.
- SQL và DBML đều mô tả các bảng Auth chính: users, pending registration, provider, refresh token, link/social/reauthentication challenge.
- Entity enum, nullable và lifecycle method phù hợp CHECK/unique constraint đã rà soát.
- Không thay đổi database trong Giai đoạn 12.
- Index refresh token chưa có `expires_at` ở vị trí đầu; cleanup có thể scan khi dữ liệu lớn. Đây là đề xuất tối ưu schema, không phải thay đổi trong giai đoạn này.

## 9. Manual provider checklist

Google: token hợp lệ, sai Client ID, hết hạn, provider mới/đã link, email conflict và user BLOCKED.

Facebook: token hợp lệ, sai App ID, hết hạn, không có email, provider mới/đã link, email conflict và user BLOCKED.

Không commit token hoặc App Secret thật. Manual provider test không phải điều kiện để Maven pass.

## 10. Kết luận readiness

Module Auth **sẵn sàng có điều kiện cho Frontend phát triển và tích hợp contract trong môi trường development**: toàn bộ test tự động khả dụng pass, không phát hiện production regression mới, và package thành công sau khi quality gate kết thúc.

Module Auth **chưa đủ bằng chứng để chốt production/concurrency readiness** vì database test chưa được cấu hình và bộ 15 kịch bản MySQL concurrency chưa đầy đủ. Trước khi xác nhận hoàn tất tuyệt đối cần:

1. Cấp database test riêng qua `AUTH_TEST_DB_*`.
2. Bổ sung các kịch bản concurrency MySQL còn thiếu.
3. Chạy lại `mvn test` và `mvn clean package` với 0 skip liên quan MySQL bắt buộc.

## 11. Giới hạn kiến trúc đã chốt

- Access Token vẫn hiệu lực đến expiration sau logout.
- Chưa có token family/Access Token blacklist.
- Rate limit và cleanup coordination là single-instance.
- Provider thật cần manual test trong development.
