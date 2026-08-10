# Kỹ năng phát triển Frontend

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Skill này hướng dẫn cách triển khai, không thay thế đặc tả nghiệp vụ trong README.

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

1. Đọc README, sau đó đọc tài liệu UI và ghi nhận mọi điểm chưa đồng bộ.
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
- Với Auth/onboarding, lập UI state machine từ README và API contract đã đồng bộ trước khi sửa component.
- Không tạo user, JWT hoặc session giả trước thời điểm contract cho phép.
- Provider credential chỉ được gửi qua Auth service và không được dùng cho API nghiệp vụ.
- Route guard, recovery, cooldown, conflict, error và onboarding phải bao phủ các trạng thái README yêu cầu.
- Với username onboarding: giữ `@` ở lớp trình bày, debounce availability, chống stale response, map lỗi submit vào field và hydrate hồ sơ legacy trước khi cho sửa.
- Chỉ profile current/other hiển thị `@username` trong phạm vi hiện tại; route vẫn dùng `userId`, không tự mở rộng sang Post/Comment/Follow/Search.
- Không lấy hành vi mock hoặc giao diện cũ làm nguồn nghiệp vụ.

## 6. Kiểm tra

- `npm run lint`.
- `npm run build`.
- Không lỗi import.
- Không lỗi console nghiêm trọng.
- Route hoạt động.
- Responsive cơ bản.
- Loading, Empty, Error đầy đủ.
