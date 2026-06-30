# Quy tắc Frontend

## 1. Công nghệ

- ReactJS.
- Vite.
- Tailwind CSS.
- React Router DOM.
- Axios.
- ESLint.

## 2. Cấu trúc Frontend chính thức

Agent phải tuân thủ cấu trúc hiện tại của dự án:

```text
FrontEnd/
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── config/
│   ├── contexts/
│   ├── features/
│   ├── hooks/
│   ├── router/
│   ├── utils/
│   ├── App.css
│   ├── App.jsx
│   ├── index.css
│   └── main.jsx
├── .env
├── .gitignore
├── eslint.config.js
├── index.html
├── package-lock.json
├── package.json
├── README.md
└── vite.config.js
```

Không tự ý đổi `router/` thành `routes/`.
Không tự ý thêm `store/`, `layouts/`, `shared/` hoặc `app/` nếu chưa được yêu cầu.

## 3. Trách nhiệm thư mục

### `assets/`

Tài nguyên tĩnh được import trong ứng dụng.

### `components/`

Component dùng chung cho nhiều feature.

Không đặt component chỉ thuộc một feature vào đây.

### `config/`

- Axios instance.
- Interceptor.
- Environment config.
- Constants.

### `contexts/`

React Context dùng toàn ứng dụng.

Chỉ tạo Context khi dữ liệu cần chia sẻ ở nhiều khu vực.

### `features/`

Mỗi module nghiệp vụ nằm trong một feature:

- auth.
- profile.
- follow.
- post.
- interaction.
- feed.
- search.
- report.
- admin.

Mỗi feature chỉ tạo các thư mục con thực sự cần:

```text
features/post/
├── components/
├── pages/
├── services/
├── hooks/
├── utils/
└── validation/
```

### `hooks/`

Custom hook dùng chung nhiều feature.

### `router/`

Toàn bộ cấu hình route:

- Public Route.
- Protected Route.
- Admin Route.
- Router chính.

### `utils/`

Hàm tiện ích dùng chung.

Không đặt component, API service hoặc logic nghiệp vụ phức tạp tại đây.

## 4. Quy tắc component

- Component giao diện không gọi Axios trực tiếp.
- API đặt tại `features/<feature>/services/`.
- Component riêng đặt tại `features/<feature>/components/`.
- Page riêng đặt tại `features/<feature>/pages/`.
- Không đưa mọi component vào `src/components/`.
- Không đưa mọi state vào Context.
- Không để `App.jsx` chứa toàn bộ giao diện.
- Không để `App.css` hoặc `index.css` chứa toàn bộ style component.

## 5. Trạng thái UI bắt buộc

Màn hình gọi API phải có:

- Loading.
- Empty.
- Error.
- Success nếu phù hợp.

Form phải:

- Validate dữ liệu.
- Disable khi đang gửi.
- Ngăn submit lặp.
- Hiển thị lỗi rõ ràng.

## 6. Quy tắc form xác thực

- Form đăng ký MVP gồm `email`, `phoneNumber`, `password` và xác nhận mật khẩu nếu UI cần.
- Không hiển thị hoặc gửi trường `username` trong form đăng ký MVP.
- Form đăng nhập dùng một trường định danh chung cho email hoặc số điện thoại và trường mật khẩu.
- Label, placeholder và thông báo lỗi dùng thuật ngữ `email`, không dùng `gmail`.
- Frontend validate định dạng email, số điện thoại, độ mạnh mật khẩu và mật khẩu xác nhận trước khi gửi.
- Ngày sinh không nằm trong form đăng ký; chỉ xuất hiện khi cập nhật hồ sơ và không được lớn hơn ngày hiện tại.
- Frontend không hiển thị công khai email hoặc số điện thoại trong hồ sơ người khác.
- Tên hiển thị là dữ liệu hồ sơ, người dùng chỉnh trong màn hình hồ sơ sau khi đăng ký.

## 7. Quy tắc API

- Base URL lấy từ `.env`.
- Dùng Axios instance trong `src/config/`.
- Access Token gắn qua interceptor.
- Refresh Token chỉ thử lại một lần.
- Không tạo vòng lặp refresh.
- Refresh thất bại thì xóa phiên và về đăng nhập.

## 8. Bảo mật

- Không lưu mật khẩu.
- Không log token.
- Không render HTML thô chưa làm sạch.
- Không tin role phía Frontend.
- Không commit secret.
