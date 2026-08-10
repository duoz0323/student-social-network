# Kỹ năng thiết kế Database

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. SQL/DBML chỉ là baseline kỹ thuật để audit; mọi đề xuất schema phải chứng minh phù hợp README.

## 1. Quy trình

1. Đọc README, sau đó đọc PRD và tài liệu database để phát hiện khác biệt.
2. Đối chiếu SQL/DBML baseline với trạng thái đích trong README.
3. Xác định thuộc tính.
4. Xác định khóa chính.
5. Xác định khóa ngoại.
6. Xác định quan hệ 1-1, 1-N, N-N.
7. Xác định unique constraint.
8. Xác định index.
9. Xác định trạng thái và xóa mềm.
10. Kiểm tra vòng đời dữ liệu.

## 2. Nguyên tắc

- Chuẩn hóa vừa đủ.
- Không tạo bảng chỉ để phục vụ một giá trị đơn giản nếu không cần.
- Dùng bảng trung gian cho N-N.
- Không lưu dữ liệu lặp không cần thiết.
- Không lưu ảnh dạng BLOB.
- Dùng foreign key cho quan hệ chính.
- Tối ưu truy vấn Feed, Profile và Search MVP.

## 3. Kiểm tra

- Không tồn tại Follow trùng.
- Không tồn tại Like trùng.
- Không tồn tại Save trùng.
- Các invariant trong README có constraint hoặc transaction/service bảo đảm tương ứng.
- SQL, DBML, Entity, Repository và test được kiểm tra đồng bộ theo cùng một trạng thái đích.
- Nullable, unique, foreign key, index, lifecycle, cleanup và dữ liệu cũ đều được phân tích.
- Với username, kiểm tra nullable trước onboarding, unique constraint khi có giá trị và migration legacy reset completion mà không làm mất các field hồ sơ cũ.
- Quan hệ xóa phù hợp.
- Index phục vụ truy vấn chính.
