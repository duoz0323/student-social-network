# AGENTS.md

## 1. Mục đích

File này là điểm vào bắt buộc dành cho Codex, Antigravity và các AI Coding Agent khi làm việc với dự án **Student Social Network – Mạng xã hội tinh gọn hướng đến sinh viên**.

Mọi Agent phải đọc và tuân thủ file này trước khi phân tích, tạo, sửa, xóa mã nguồn, thay đổi database, cập nhật tài liệu hoặc cài đặt thư viện.

Mục tiêu của file này:

- Xác định nguồn sự thật của dự án.
- Quy định thứ tự đọc tài liệu.
- Buộc Agent kiểm tra tính đồng bộ giữa tài liệu, source code, SQL và DBML.
- Ngăn Agent tự ý mở rộng nghiệp vụ hoặc thay đổi kiến trúc.
- Chuẩn hóa quy trình phân tích, triển khai, kiểm thử và báo cáo kết quả.

---

## 2. Nguồn sự thật duy nhất của dự án

### 2.1. Nguồn ưu tiên cao nhất

`README.md` tại thư mục gốc là **nguồn sự thật duy nhất và có mức ưu tiên cao nhất** của dự án đối với:

- Mục tiêu và phạm vi hệ thống.
- Phạm vi MVP và mức độ ưu tiên P0, P1, P2.
- Nghiệp vụ của từng module.
- Kiến trúc Frontend, Backend và Database.
- Quy ước API.
- Mô hình xác thực và bảo mật.
- Quy tắc JWT Access Token và Refresh Token.
- Quy trình đăng ký local bằng OTP.
- Đăng ký và đăng nhập Google/Facebook.
- Liên kết nhiều phương thức xác thực trên cùng một tài khoản.
- Cấu trúc thư mục.
- Tiêu chí nghiệm thu.
- Yêu cầu kiểm thử.
- Quy ước Git và mã nguồn.
- Trạng thái triển khai được ghi nhận trong tài liệu.

Agent không được tự ý thay đổi quyết định nghiệp vụ trong `README.md` để làm cho tài liệu phù hợp với source code cũ.

### 2.2. Ý nghĩa của các nguồn còn lại

- `README.md`: trạng thái đích và quyết định nghiệp vụ chính thức.
- SQL hiện tại: trạng thái triển khai vật lý của database.
- DBML hiện tại: mô hình database dùng để mô tả và đối chiếu.
- Source code hiện tại: trạng thái implementation thực tế.
- Test hiện tại: phạm vi hành vi đang được kiểm chứng.
- `docs/`, `.agents/rules/`, `.agents/skills/`, `.agents/workflows/`: tài liệu hỗ trợ Agent làm việc.
- Tài liệu nghiệp vụ cũ hoặc phiên bản lịch sử: chỉ dùng tham khảo khi không mâu thuẫn với `README.md`.

### 2.3. Thứ tự ưu tiên khi có mâu thuẫn

Khi phát hiện mâu thuẫn, Agent phải áp dụng thứ tự sau:

1. `README.md`.
2. Quyết định trực tiếp mới nhất của người dùng trong nhiệm vụ hiện tại.
3. SQL và DBML hiện hành.
4. Source code và test hiện tại.
5. `AGENTS.md`, rule, skill và workflow.
6. Các tài liệu khác trong `docs/`.
7. Tài liệu lịch sử hoặc phiên bản cũ.

Nếu source code, SQL, DBML, test hoặc tài liệu khác chưa khớp `README.md`, Agent phải xem đó là **khoảng chênh lệch cần được báo cáo và đồng bộ**, không được tự động coi implementation cũ là đúng hơn.

### 2.4. Quy tắc xử lý mâu thuẫn

Khi phát hiện mâu thuẫn, Agent phải:

1. Nêu rõ file và nội dung mâu thuẫn.
2. Trích dẫn hoặc chỉ rõ mục tương ứng trong `README.md`.
3. Phân loại mâu thuẫn:
   - Nghiệp vụ.
   - API contract.
   - Database.
   - Security.
   - Frontend.
   - Backend.
   - Test.
   - Tài liệu.
4. Đánh giá ảnh hưởng đến source code, database, test và giao diện.
5. Đề xuất phương án đồng bộ.
6. Chỉ sửa sau khi phạm vi đã rõ ràng.
7. Không tự ý thay đổi quyết định nghiệp vụ trong `README.md`.

---

## 3. Thứ tự tài liệu bắt buộc phải đọc

Trước mọi nhiệm vụ, Agent phải đọc theo thứ tự:

1. `README.md`.
2. `AGENTS.md`.
3. `docs/PRD.md` nếu tồn tại.
4. `docs/ARCHITECTURE.md` nếu tồn tại.
5. `docs/PROJECT-RULES.md` nếu tồn tại.
6. `.agents/rules/general-rules.md`.
7. Rule chuyên biệt theo nhiệm vụ:
   - Frontend: `.agents/rules/frontend-rules.md`.
   - Backend: `.agents/rules/backend-rules.md`.
   - Database: `.agents/rules/database-rules.md`.
   - Security/Auth: `.agents/rules/security-rules.md`.
8. Skill chuyên môn phù hợp:
   - `.agents/skills/frontend-development.md`.
   - `.agents/skills/backend-development.md`.
   - `.agents/skills/database-design.md`.
   - `.agents/skills/api-design.md`.
   - `.agents/skills/bug-fixing.md`.
9. Workflow phù hợp:
   - `.agents/workflows/implement-feature.md`.
   - `.agents/workflows/create-ui.md`.
   - `.agents/workflows/create-api.md`.
   - `.agents/workflows/fix-bug.md`.
   - `.agents/workflows/review-code.md`.
10. SQL, DBML, migration và seed liên quan.
11. Source code hiện tại của module.
12. Test hiện tại của module.
13. Tài liệu UI hoặc dữ liệu liên quan trong `docs/ui`, `docs/data`, `docs/business`, `docs/api` và `docs/database`.

Nếu một file không tồn tại, Agent phải ghi nhận rõ, không được giả định nội dung của file đó.

---

## 4. Bước bắt buộc trước khi triển khai chức năng mới

Trước khi sửa production code, Agent phải thực hiện **Giai đoạn 0 – Audit và đồng bộ tài liệu**.

### 4.1. Mục tiêu Giai đoạn 0

Agent phải xác định:

- Các file nào đang mô tả nghiệp vụ của module.
- Các file nào còn dùng nghiệp vụ cũ.
- SQL và DBML có khớp `README.md` không.
- Source code và test có khớp `README.md` không.
- API hiện tại có khớp contract mong muốn không.
- Có breaking change nào không.
- Có dữ liệu cũ nào có thể vi phạm schema mới không.
- Có rule, skill hoặc workflow nào hướng dẫn sai không.

### 4.2. Báo cáo bắt buộc của Giai đoạn 0

Agent phải trả một báo cáo gồm:

1. Danh sách file đã đọc.
2. Tóm tắt nghiệp vụ hiện tại theo `README.md`.
3. Bảng đối chiếu:
   - `README.md`.
   - Tài liệu khác.
   - SQL.
   - DBML.
   - Source code.
   - Test.
4. Danh sách điểm đã đồng bộ.
5. Danh sách điểm chưa đồng bộ.
6. Mức độ ảnh hưởng.
7. File cần sửa.
8. Thứ tự sửa đề xuất.
9. Test cần bổ sung.
10. Rủi ro và giả định.
11. Các quyết định cần người dùng xác nhận.

### 4.3. Quy tắc cập nhật tài liệu trong Giai đoạn 0

Nếu tài liệu khác mâu thuẫn với `README.md`, Agent được phép đề xuất cập nhật nhưng phải:

- Không thay đổi ý nghĩa nghiệp vụ trong `README.md`.
- Không tạo thêm phụ lục chồng chéo ở cuối tài liệu.
- Sửa trực tiếp đúng mục đang chứa nội dung cũ.
- Không sao chép toàn bộ nội dung README sang nhiều file.
- File rule/skill/workflow chỉ nên dẫn về `README.md` và mô tả cách Agent làm việc.
- Tài liệu chi tiết chỉ được giữ khi không mâu thuẫn với README.
- Đánh dấu hoặc loại bỏ nội dung lỗi thời có khả năng làm Agent hiểu sai.
- Không sửa production code trong cùng bước audit tài liệu, trừ khi người dùng yêu cầu rõ ràng.

---

## 5. Phạm vi chức năng hiện tại

Luồng chính của hệ thống:

Đăng ký local bằng email  
→ Xác minh OTP  
→ Tạo tài khoản nội bộ  
→ Đăng ký/đăng nhập Google hoặc Facebook  
→ Đăng nhập local  
→ Cấp JWT Access Token và Refresh Token  
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

Không tự ý triển khai chức năng ngoài phạm vi được quy định trong `README.md`.

Khi một chức năng được chuyển từ “ngoài MVP” sang P0, P1 hoặc P2 trong `README.md`, Agent phải ưu tiên trạng thái mới nhất trong `README.md`.

---

## 6. Quy tắc Auth và onboarding hiện tại

### 6.1. Nguyên tắc kiến trúc

Hệ thống áp dụng mô hình:

```text
Một tài khoản nội bộ
        +
Nhiều phương thức xác thực
```

Email, Google và Facebook là các phương thức chứng minh danh tính. Tất cả phương thức đã được xác minh và liên kết hợp lệ phải ánh xạ về cùng một `users.id`.

### 6.2. Đăng ký local bằng email

- Request đăng ký dùng một trường `email`.
- Người dùng chỉ cung cấp đúng một email tại một thời điểm:
  - Email; hoặc
  - Email.
- Request gồm:
  - `email`.
  - `password`.
  - `confirmPassword`.
- Không dùng username hoặc display name trong form đăng ký.
- Không gọi email là Gmail trong API, database hoặc tài liệu nghiệp vụ.
- Email phải được chuẩn hóa về chữ thường và loại bỏ khoảng trắng.
- Email phải được chuẩn hóa về định dạng thống nhất.
- Form đăng ký local chỉ tạo `pending_registrations`.
- Không tạo `users`, `user_profiles`, Access Token hoặc Refresh Token trước khi OTP hợp lệ.
- Mật khẩu phải được băm trước khi lưu trong pending.
- OTP và flow token chỉ được lưu dạng hash.
- OTP có hiệu lực 10 phút.
- Chỉ cho gửi lại OTP sau 60 giây.
- Tối đa 5 lần nhập sai cho mỗi OTP.
- Pending registration có hiệu lực 24 giờ.
- OTP mới phải làm OTP cũ mất hiệu lực.
- Không được tồn tại hai pending còn hiệu lực cho cùng một email.
- Dữ liệu `CANCELLED` và `EXPIRED` được lưu tối đa 7 ngày trước khi xóa hoặc ẩn danh.

### 6.3. Xác minh OTP

Sau khi OTP hợp lệ, Backend phải:

1. Khóa hoặc kiểm soát đồng thời bản ghi pending phù hợp.
2. Kiểm tra trạng thái, thời hạn, số lần thử và OTP hash.
3. Tạo `users`.
4. Tạo `user_profiles`.
5. Đánh dấu pending là `COMPLETED`.
6. Tạo Refresh Token.
7. Cấp Access Token và Refresh Token.
8. Thực hiện các thao tác database quan trọng trong transaction phù hợp.

`users` và `user_profiles` phải được tạo trong cùng transaction. Nếu tạo profile thất bại, toàn bộ transaction phải rollback.

Không giữ transaction database trong lúc chờ SMTP, email, Google hoặc Facebook.

### 6.4. Khôi phục đăng ký đang chờ

- Pending được giữ trong 24 giờ khi mất mạng, đóng tab hoặc rời quy trình.
- Người dùng có thể tiếp tục xác minh hoặc gửi lại OTP.
- Nếu Frontend mất flow token, người dùng có thể nhập lại email.
- Backend phải phát hiện pending còn hiệu lực và không tạo bản ghi trùng.

### 6.5. Google và Facebook

- Frontend nhận provider credential/token và gửi đến endpoint Auth tương ứng.
- Backend phải tự xác minh token với provider.
- Không tin `providerUserId`, email hoặc trạng thái verified do Frontend tự gửi.
- Google/Facebook token chỉ được dùng tại endpoint Auth chuyên biệt.
- API nghiệp vụ chỉ chấp nhận JWT do Backend phát hành.
- Provider đã liên kết phải đăng nhập về đúng `users.id`.
- Provider chưa liên kết và chưa thuộc tài khoản nào có thể tạo tài khoản nội bộ mới theo nghiệp vụ.
- Không tự động gộp hai tài khoản `ACTIVE` chỉ vì trùng email.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức đăng nhập.
- Dùng email bất biến của provider:
  - Google: `sub`.
  - Facebook: provider user ID.
- `user_auth_providers(provider, provider_user_id)` phải duy nhất.

### 6.6. Chuyển từ OTP sang social

- Pending email và social trả cùng email đã xác minh:
  - Được phép hoàn tất pending.
  - Giữ phương thức local.
  - Liên kết provider vào cùng một user.
- Pending email khác social email:
  - Không tự động gộp.
  - Yêu cầu người dùng lựa chọn tiếp tục OTP hoặc hủy pending.
- Pending email chuyển sang social:
  - Social không phải bằng chứng xác minh email.
  - Không sao chép email chưa xác minh sang tài khoản social.
  - Phải xác nhận hủy pending email trước khi tiếp tục.
- Chỉ hủy hoặc hoàn tất pending sau khi Backend đã xác minh social token thành công.

### 6.7. Đăng nhập local

- Người dùng đăng nhập bằng email và mật khẩu.
- Email chỉ được dùng đăng nhập khi `email_verified_at` khác `NULL`.
- `users.password_hash` được phép `NULL` với tài khoản chỉ dùng social.
- Tài khoản social-only chưa có mật khẩu không được đăng nhập local.
- Tài khoản `BLOCKED` bị từ chối.

### 6.8. Liên kết và gỡ phương thức xác thực

Người dùng đã đăng nhập có thể liên kết:

- Email.
- Google.
- Facebook.

Quy tắc:

- Email phải xác minh OTP trước khi liên kết.
- Google/Facebook phải được xác minh trong phiên đang đăng nhập.
- `user_id` đích phải lấy từ JWT hiện tại.
- Không suy ra tài khoản đích chỉ bằng email social.
- Email hoặc provider đã thuộc tài khoản khác phải bị từ chối.
- Không tự động gộp hai tài khoản đang hoạt động.
- Không được gỡ phương thức đăng nhập cuối cùng.

### 6.9. JWT và phiên đăng nhập

- Access Token có thời hạn ngắn.
- Refresh Token có thời hạn dài hơn.
- Refresh Token chỉ lưu dạng hash.
- Refresh Token có thể bị thu hồi.
- Logout phải thu hồi Refresh Token của phiên hiện tại.
- Khi tài khoản bị khóa, các Refresh Token còn hiệu lực phải bị thu hồi theo nghiệp vụ quản trị.
- Provider Token không thay thế JWT hệ thống.

### 6.10. Onboarding

- Sau khi tài khoản thật được tạo, Frontend điều hướng tới onboarding.
- Tên hiển thị và ngày sinh là bắt buộc theo `README.md` hiện tại.
- Người dùng phải đủ 18 tuổi tại thời điểm Backend xử lý.
- Avatar và bio là tùy chọn.
- `user_profiles.profile_completed_at` chỉ được cập nhật khi dữ liệu bắt buộc hợp lệ và người dùng xác nhận hoàn tất.
- `users.status = ACTIVE` chỉ thể hiện tài khoản không bị khóa.
- Khi `profile_completed_at` là `NULL`, Backend chỉ cho phép:
  - API Auth cần thiết.
  - Refresh Token.
  - Logout.
  - API onboarding.
  - API quản lý phương thức xác thực cần thiết theo contract đã chốt.
- API mạng xã hội chính phải trả `PROFILE_NOT_COMPLETED`.

---

## 7. Quy tắc nghiệp vụ chính của các module khác

### 7.1. Hồ sơ người dùng

- Hồ sơ công khai trong MVP.
- Không trả email hoặc dữ liệu xác thực trong API hồ sơ công khai.
- Chỉ chủ tài khoản được cập nhật hồ sơ.
- Ngày sinh không được ở tương lai.
- Quy tắc bắt buộc/tùy chọn phải theo `README.md`.

### 7.2. Follow

- Không được follow chính mình.
- Không tạo quan hệ follow trùng.
- Follow có hiệu lực ngay.
- Không triển khai Follow Request trong MVP hiện tại.

### 7.3. Bài viết

- Nội dung tối đa 500 ký tự.
- Tối đa 4 ảnh.
- Mỗi bài có tối đa 1 hashtag.
- Bài phải có nội dung hoặc ít nhất một ảnh.
- Chỉ tác giả được sửa hoặc xóa bài.
- Chỉ được sửa trong 15 phút sau khi đăng.
- Xóa bài là xóa mềm.
- Trạng thái:
  - `PUBLISHED`.
  - `HIDDEN`.
  - `DELETED`.

### 7.4. Hashtag

- Mỗi bài tối đa một hashtag.
- Xóa toàn bộ ký tự `#` ở đầu.
- Trim khoảng trắng Unicode.
- Gộp khoảng trắng liên tiếp.
- Chuẩn hóa Unicode NFC.
- Chuyển về chữ thường.
- Hỗ trợ tiếng Việt có dấu và nhiều từ.
- Giới hạn tối đa 100 code point.
- Hashtag không được tính là nội dung bài viết.
- API gợi ý hashtag không phân trang.

### 7.5. Tương tác

- Mỗi user chỉ Like một bài tối đa một lần.
- Mỗi user chỉ Save một bài tối đa một lần.
- Chỉ tác giả bình luận được xóa bình luận của mình.
- Không tương tác với bài `HIDDEN` hoặc `DELETED`.

### 7.6. Feed

- Feed Following lấy bài của tài khoản đang follow và sắp xếp mới nhất trước.
- Feed For You dùng xếp hạng cơ bản theo độ mới, lượt Like và bình luận.
- Không dùng Machine Learning trong MVP.
- Không hiển thị bài `HIDDEN` hoặc `DELETED`.

### 7.7. Tìm kiếm

- Tìm user theo tên hiển thị.
- Tìm bài theo nội dung và hashtag.
- Có phân trang.
- MySQL là nguồn tìm kiếm trong MVP.
- Elasticsearch là hướng phát triển trừ khi `README.md` được cập nhật khác.

### 7.8. Báo cáo

- Chỉ báo cáo bài viết trong MVP.
- Một user không được có nhiều báo cáo `PENDING` cho cùng một bài.
- Gửi báo cáo không tự động ẩn bài.
- Admin quyết định xử lý.
- Báo cáo và snapshot phải tuân thủ transaction đã chốt.

### 7.9. Quản trị

- API `/api/v1/admin/**` chỉ dành cho `ADMIN`.
- USER phải nhận `403`.
- Không token phải nhận `401`.
- Tài khoản `BLOCKED` bị từ chối.
- Admin không được tự thao tác sai quy tắc với chính mình hoặc admin khác.
- Khóa tài khoản phải ghi lịch sử và thu hồi Refresh Token.
- Ẩn/khôi phục bài phải tuân thủ state machine.
- Hành động quản trị quan trọng phải được ghi nhận theo thiết kế hiện tại.

---

## 8. Quy tắc làm việc bắt buộc

Trước khi sửa code, Agent phải:

1. Đọc tài liệu liên quan.
2. Tóm tắt yêu cầu.
3. Xác định actor và luồng nghiệp vụ.
4. Đối chiếu `README.md` với source, SQL, DBML và test.
5. Liệt kê file dự kiến tạo hoặc sửa.
6. Nêu database và API bị ảnh hưởng.
7. Trình bày kế hoạch ngắn.
8. Nêu test cần viết hoặc cập nhật.
9. Chỉ triển khai sau khi hiểu rõ phạm vi.

Agent không được:

- Tự ý mở rộng nghiệp vụ.
- Tự ý đổi kiến trúc.
- Tự ý thay đổi database.
- Tự ý đổi API contract.
- Tự ý cài thêm thư viện.
- Tự ý thay đổi `README.md` để hợp thức hóa source cũ.
- Tự động gộp tài khoản chỉ vì trùng email.
- Xóa file hàng loạt mà không giải thích.
- Hard-code token, URL bí mật hoặc thông tin nhạy cảm.
- Đưa secret vào Git.
- Log password, OTP, flow token, provider token hoặc Refresh Token.
- Trả JPA Entity trực tiếp ra API.
- Đặt logic nghiệp vụ trong Controller.
- Gọi API trực tiếp trong component trình bày.
- Tin dữ liệu xác thực social do Frontend tự khai báo.
- Giữ transaction database khi gọi dịch vụ bên ngoài.

---

## 9. Quy trình sửa lỗi bắt buộc

Khi người dùng gửi code lỗi hoặc log lỗi, Agent phải làm theo thứ tự:

### Bước 1: Phân tích nguyên nhân cốt lõi

- Đọc log đầy đủ.
- Xác định lớp, method và dữ liệu liên quan.
- Phân biệt nguyên nhân trực tiếp và nguyên nhân gốc.
- Đối chiếu với nghiệp vụ và schema.

### Bước 2: Đề xuất giải pháp

- Nêu phương án tối ưu.
- Nêu ảnh hưởng tới API, database và module khác.
- Nêu rủi ro hồi quy.
- Nêu test cần chạy.

### Bước 3: Đưa code đã sửa

- Code hoàn chỉnh.
- Mã nguồn sạch.
- Có comment tiếng Việt giải thích mục đích và nghiệp vụ.
- Không chỉ đưa patch rời rạc khi cần toàn bộ file để hiểu.
- Kèm cách kiểm thử.

Không được đưa code sửa ngay trước khi hoàn tất phân tích nguyên nhân.

---

## 10. Quy tắc mã nguồn

- Tên class, biến, hàm, component, hook và file dùng tiếng Anh rõ nghĩa.
- Mọi đoạn code được tạo phải có comment tiếng Việt giải thích mục đích và nghiệp vụ.
- Không comment những câu lệnh quá hiển nhiên.
- Ưu tiên mã nguồn đơn giản, dễ đọc và dễ bảo trì.
- Không tạo abstraction sớm khi chưa cần thiết.
- Mỗi module chịu trách nhiệm cho một nhóm nghiệp vụ rõ ràng.
- API danh sách phải có phân trang, trừ API được `README.md` quy định không phân trang.
- Backend là nơi quyết định quyền truy cập cuối cùng.
- Frontend chỉ hỗ trợ trải nghiệm, không thay thế kiểm tra quyền Backend.
- Controller không truy cập Repository trực tiếp.
- Entity không trả trực tiếp ra API.
- Service chịu trách nhiệm nghiệp vụ và transaction.
- Repository chỉ chịu trách nhiệm truy cập dữ liệu.
- Exception phải được xử lý tập trung.
- Tránh N+1.
- Không tải relationship không cần thiết.
- Validation phải có ở DTO và Service khi cần.
- Dữ liệu nhạy cảm không được xuất hiện trong response hoặc log.

---

## 11. Cấu trúc Frontend hiện tại

Agent phải giữ nguyên các thư mục đang có:

- `src/assets`.
- `src/components`.
- `src/config`.
- `src/contexts`.
- `src/features`.
- `src/hooks`.
- `src/router`.
- `src/utils`.

Không tự ý:

- Đổi `router` thành `routes`.
- Thêm `shared`, `app`, `layouts` hoặc `store` nếu chưa được chấp thuận.
- Gọi Axios trực tiếp trong JSX hoặc component trình bày.
- Lưu provider token lâu hơn mức cần thiết.
- Dùng provider token gọi API nghiệp vụ.

---

## 12. Quy tắc Database

- SQL và DBML phải đồng bộ.
- Entity phải khớp schema.
- Dùng foreign key cho quan hệ chính.
- Dùng unique constraint cho quan hệ chống trùng.
- Dùng transaction cho thao tác nhiều bước.
- Không tự động chạy migration trên database thật.
- Không sửa dữ liệu thật nếu người dùng chưa yêu cầu.
- Không xóa bảng/cột mà chưa phân tích dữ liệu cũ.
- Phải báo cáo breaking change.
- Không lưu password, OTP, flow token hoặc Refresh Token dạng thô.
- `users.password_hash` được phép `NULL` với tài khoản social-only.
- `user_auth_providers(provider, provider_user_id)` phải duy nhất.
- Pending còn hiệu lực không được trùng email.
- Mọi thay đổi schema phải cập nhật cả SQL, DBML, entity và test liên quan.

---

## 13. Quy tắc Security

- API public phải được khai báo rõ ràng.
- Các API khác mặc định yêu cầu xác thực.
- API Admin yêu cầu `ADMIN`.
- Access Token và Refresh Token phải tách vai trò.
- Refresh Token có thể bị thu hồi.
- Provider Token chỉ dùng cho Auth endpoint.
- Backend phải verify token Google/Facebook.
- Không tin email hoặc provider ID do Frontend tự gửi.
- Không trả stack trace hoặc thông tin nhạy cảm.
- CORS chỉ cho phép origin cần thiết.
- Secret phải lấy từ biến môi trường.
- Rate limit áp dụng cho login, OTP, resend và social auth theo thiết kế.
- Tài khoản `BLOCKED` bị từ chối ở mọi phương thức.
- User chưa hoàn tất hồ sơ bị chặn ở API mạng xã hội chính.

---

## 14. Quy tắc cập nhật README

`README.md` là nguồn sự thật cao nhất, vì vậy Agent phải cập nhật cẩn thận.

Agent chỉ được cập nhật README khi:

- Người dùng yêu cầu.
- API, database hoặc cấu trúc source đã được chốt và triển khai.
- Cần đồng bộ trạng thái triển khai.
- Cần sửa nội dung lỗi thời hoặc mâu thuẫn đã được xác nhận.

Khi cập nhật:

- Sửa trực tiếp đúng mục hiện có.
- Không nối thêm “phụ lục cập nhật” ở cuối.
- Không lặp lại cùng một nghiệp vụ ở nhiều vị trí.
- Không đánh dấu `IMPLEMENTED` khi mới chỉ `DESIGNED`.
- Phân biệt:
  - `PLANNED`.
  - `DESIGNED`.
  - `IMPLEMENTED`.
  - `TESTED`.
  - `INTEGRATED`.
- Sau khi sửa, kiểm tra toàn bộ README để loại bỏ nội dung cũ còn sót.

---

## 15. Quy tắc hoàn thành nhiệm vụ

Sau khi hoàn thành, Agent phải báo cáo:

1. Chức năng đã thực hiện.
2. Nghiệp vụ đã áp dụng.
3. File đã đọc.
4. File đã tạo.
5. File đã sửa.
6. Database đã thay đổi hoặc không thay đổi.
7. API đã thay đổi hoặc không thay đổi.
8. Test đã thêm hoặc cập nhật.
9. Lệnh đã chạy.
10. Kết quả build/test.
11. Cách chạy.
12. Cách kiểm thử thủ công.
13. Phần chưa hoàn thành.
14. Rủi ro hoặc giả định còn tồn tại.
15. Các điểm chưa đồng bộ còn lại.
16. Tài liệu đã cập nhật.

Nếu không thể chạy test hoặc build, Agent phải nói rõ lý do và không được tuyên bố chức năng đã hoạt động hoàn chỉnh.

