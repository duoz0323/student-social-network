# AGENTS.md

## 1. Mục đích

File này là điểm vào chính dành cho Codex, Antigravity và các AI Coding Agent khi làm việc với dự án **Mạng xã hội tinh gọn hướng đến sinh viên**.

Agent phải đọc tài liệu theo đúng thứ tự trước khi tạo, sửa hoặc xóa mã nguồn.

## 2. Thứ tự tài liệu bắt buộc phải đọc

1. `docs/PRD.md`
2. `docs/ARCHITECTURE.md`
3. `docs/PROJECT-RULES.md`
4. `.agents/rules/general-rules.md`
5. Rule chuyên biệt theo nhiệm vụ:
   - Frontend: `.agents/rules/frontend-rules.md`
   - Backend: `.agents/rules/backend-rules.md`
   - Database: `.agents/rules/database-rules.md`
   - Security: `.agents/rules/security-rules.md`
6. Skill chuyên môn phù hợp:
   - `.agents/skills/frontend-development.md`
   - `.agents/skills/backend-development.md`
   - `.agents/skills/database-design.md`
   - `.agents/skills/api-design.md`
   - `.agents/skills/bug-fixing.md`
7. Workflow phù hợp:
   - `.agents/workflows/implement-feature.md`
   - `.agents/workflows/create-ui.md`
   - `.agents/workflows/create-api.md`
   - `.agents/workflows/fix-bug.md`
   - `.agents/workflows/review-code.md`
8. Tài liệu UI hoặc Data liên quan trong `docs/ui` và `docs/data`.

## 3. Phạm vi MVP bắt buộc tuân thủ

Luồng chính của MVP:

Đăng ký
→ Đăng nhập
→ Hoàn tất hồ sơ ban đầu
→ Quản lý hồ sơ
→ Follow/Unfollow
→ Tạo và quản lý bài viết
→ Xem Feed For You
→ Xem Feed Following
→ Like/Unlike
→ Bình luận
→ Lưu/Bỏ lưu bài viết
→ Tìm kiếm
→ Báo cáo bài viết
→ Quản trị người dùng, bài viết và báo cáo.

Không tự ý triển khai chức năng ngoài phạm vi MVP.

Các chức năng chưa thuộc MVP:

- Xác thực email.
- Đăng nhập Google hoặc Facebook.
- Hồ sơ riêng tư.
- Follow Request.
- Block và Restrict.
- Video và tài liệu trong bài viết.
- Bài viết nháp.
- Mention.
- Repost.
- Trích dẫn bài viết.
- Chủ đề nội dung riêng.
- Địa điểm và Discovery Map.
- Feed tùy chỉnh.
- Elasticsearch.
- Nhắn tin.
- Thông báo thời gian thực.
- Dashboard nâng cao.
- Moderation Case.
- Audit Log chi tiết.

## 4. Quy tắc làm việc bắt buộc

Trước khi sửa code, Agent phải:

1. Đọc tài liệu liên quan.
2. Tóm tắt yêu cầu.
3. Xác định actor và luồng nghiệp vụ.
4. Liệt kê file dự kiến tạo hoặc sửa.
5. Trình bày kế hoạch ngắn.
6. Chỉ triển khai sau khi đã hiểu rõ phạm vi.

Không được:

- Tự ý mở rộng nghiệp vụ.
- Tự ý đổi kiến trúc.
- Tự ý thay đổi database.
- Tự ý cài thêm thư viện.
- Xóa file hàng loạt mà không giải thích.
- Hard-code token, URL bí mật hoặc thông tin nhạy cảm.
- Đưa secret vào Git.
- Trả JPA Entity trực tiếp ra API.
- Đặt logic nghiệp vụ trong Controller.
- Gọi API trực tiếp trong component trình bày.

## 4.1. Quy tắc Auth và onboarding MVP

- Đăng ký chỉ dùng một trường định danh chung `identifier` cho email hoặc số điện thoại, kèm `password` và `confirmPassword`.
- Không dùng username hoặc display name trong form/request đăng ký MVP.
- Không gọi email là Gmail trong API, database hoặc tài liệu nghiệp vụ.
- Đăng nhập dùng email hoặc số điện thoại kèm mật khẩu.
- Sau đăng ký, Backend tạo `users` và `user_profiles` rỗng trong cùng transaction.
- Nếu tạo `user_profiles` thất bại thì rollback `users`.
- `user_profiles.display_name` và `user_profiles.profile_completed_at` ban đầu là `NULL`.
- Sau đăng ký, Frontend điều hướng người dùng đến onboarding hồ sơ.
- Tên hiển thị bắt buộc để hoàn tất hồ sơ; avatar, ngày sinh và bio là tùy chọn.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa, không đồng nghĩa hồ sơ đã hoàn tất.
- Khi `profile_completed_at` còn `NULL`, Backend chỉ cho phép API xác thực cần thiết, Refresh Token, đăng xuất và onboarding.
- API mạng xã hội chính phải trả lỗi `PROFILE_NOT_COMPLETED` nếu hồ sơ chưa hoàn tất.

## 5. Quy tắc mã nguồn

- Tên class, biến, hàm, component, hook và file dùng tiếng Anh rõ nghĩa.
- Mọi đoạn code được tạo phải có comment tiếng Việt giải thích mục đích và nghiệp vụ.
- Không comment những câu lệnh quá hiển nhiên.
- Ưu tiên mã nguồn đơn giản, dễ đọc và dễ bảo trì.
- Không tạo abstraction sớm khi chưa cần thiết.
- Mỗi module chịu trách nhiệm cho một nhóm nghiệp vụ rõ ràng.
- API danh sách phải có phân trang.
- Backend là nơi quyết định quyền truy cập cuối cùng.
- Frontend chỉ hỗ trợ trải nghiệm người dùng, không thay thế kiểm tra quyền Backend.


## 6.1. Cấu trúc Frontend hiện tại

Agent phải giữ nguyên các thư mục đang có:

- `src/assets`
- `src/components`
- `src/config`
- `src/contexts`
- `src/features`
- `src/hooks`
- `src/router`
- `src/utils`

Không tự ý đổi `router` thành `routes`, không tự ý thêm `shared`, `app`, `layouts` hoặc `store`.

## 6. Quy tắc hoàn thành nhiệm vụ

Sau khi hoàn thành, Agent phải báo cáo:

1. Chức năng đã thực hiện.
2. File đã tạo.
3. File đã sửa.
4. Cách chạy.
5. Cách kiểm thử.
6. Trường hợp đã kiểm tra.
7. Phần chưa hoàn thành.
8. Rủi ro hoặc giả định còn tồn tại.
