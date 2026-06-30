# Kỹ năng thiết kế Database

## 1. Quy trình

1. Đọc PRD.
2. Xác định thực thể.
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
- Email và số điện thoại duy nhất.
- Email được chuẩn hóa chữ thường trước khi lưu.
- Số điện thoại được chuẩn hóa về một định dạng thống nhất trước khi lưu.
- Không dùng `username` làm định danh công khai trong MVP.
- Ngày sinh thuộc hồ sơ người dùng, là thông tin tùy chọn và không được lớn hơn ngày hiện tại.
- Quan hệ xóa phù hợp.
- Index phục vụ truy vấn chính.
