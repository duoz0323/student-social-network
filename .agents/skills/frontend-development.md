# Kỹ năng phát triển Frontend

## 1. Mục tiêu

Xây dựng Frontend ReactJS đúng cấu trúc hiện tại của dự án, không tự ý đổi sang một cấu trúc khác.

## 2. Cấu trúc hiện tại

```text
src/
├── assets/
├── components/
├── config/
├── contexts/
├── features/
├── hooks/
├── router/
├── utils/
├── App.css
├── App.jsx
├── index.css
└── main.jsx
```

## 3. Quy trình triển khai feature

1. Đọc tài liệu UI.
2. Xác định route trong `src/router/`.
3. Xác định feature trong `src/features/`.
4. Tạo API service trong feature.
5. Tạo component riêng trong feature.
6. Tạo page trong feature nếu có route.
7. Tạo hook riêng trong feature nếu logic chỉ dùng nội bộ.
8. Chỉ đặt hook vào `src/hooks/` khi dùng chung.
9. Chỉ đặt component vào `src/components/` khi dùng chung.
10. Kiểm tra Loading, Empty, Error.

## 4. Ví dụ feature

```text
src/features/post/
├── components/
│   ├── PostCard.jsx
│   └── CreatePostForm.jsx
├── pages/
│   └── PostDetailPage.jsx
├── services/
│   └── postApi.js
├── hooks/
│   └── usePosts.js
├── utils/
│   └── postUtils.js
└── validation/
    └── postValidation.js
```

Không bắt buộc tạo đầy đủ các thư mục trên nếu chưa dùng.

## 5. Quy tắc thực tế

- Không tạo `shared/` vì dự án đang dùng `components/`.
- Không tạo `routes/` vì dự án đang dùng `router/`.
- Không tạo `app/` nếu chưa có yêu cầu.
- Không tạo `store/` nếu Context đã đủ.
- Không tự ý thêm Zustand hoặc Redux.
- Không chuyển toàn bộ code sang TypeScript.
- Không tự ý đổi tên thư mục hiện có.
- Form đăng ký auth dùng một trường `identifier` cho email hoặc số điện thoại, mật khẩu và xác nhận mật khẩu.
- Không bắt buộc nhập đồng thời email và số điện thoại trong MVP.
- Không thêm trường `username` vào UI đăng ký MVP.
- Form đăng nhập dùng một ô email hoặc số điện thoại.
- Không dùng nhãn `Gmail`; luôn dùng `email`.
- Sau đăng ký điều hướng đến onboarding hồ sơ.
- Onboarding yêu cầu tên hiển thị; avatar, ngày sinh và bio có thể bỏ qua.
- Route guard phải chuyển người đã đăng nhập nhưng chưa hoàn tất hồ sơ về onboarding.
- Khi API trả `PROFILE_NOT_COMPLETED`, điều hướng về onboarding.
- Ngày sinh chỉ đặt ở onboarding/cập nhật hồ sơ, không đặt ở đăng ký.

## 6. Kiểm tra

- `npm run lint`.
- `npm run build`.
- Không lỗi import.
- Không lỗi console nghiêm trọng.
- Route hoạt động.
- Responsive cơ bản.
- Loading, Empty, Error đầy đủ.
