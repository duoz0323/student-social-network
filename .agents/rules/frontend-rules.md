# Quy tắc Frontend

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. File này chỉ quy định cách tổ chức và kiểm tra Frontend; nghiệp vụ, trạng thái đích và API phải lấy từ README.

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

- Đọc luồng Auth, onboarding, API và tiêu chí nghiệm thu trong README trước khi dựng form hoặc route guard.
- Lập state diagram cho guest, pending registration, authenticated-but-incomplete và authenticated-complete.
- Không tạo user hoặc session giả trước thời điểm README/API contract cho phép.
- Provider credential chỉ được chuyển tới Auth service phù hợp; không lưu lâu dài hoặc dùng cho API nghiệp vụ.
- Validation phía Frontend phục vụ trải nghiệm; Backend vẫn là nơi quyết định cuối cùng.
- Mọi màn hình phải xử lý loading, resend/cooldown, error, recovery, conflict và navigation theo contract đã đồng bộ.
- Không sao chép danh sách field hoặc state Auth vào rule này; lấy chúng từ README và tài liệu API/UI tương ứng.

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

## 9. Ghi chú
- Hệ thống sử dụng 2 phiên bản logo theo theme:
  - `FrontEnd/src/assets/brand/logo-light.jpg` – nền đen, chữ Ui trắng (dùng cho giao diện sáng).
  - `FrontEnd/src/assets/brand/logo-dark.jpg` – nền trắng, chữ Ui đen (dùng cho giao diện tối).
- Trong React component, dùng hook `useThemeLogo()` từ `src/hooks/useThemeLogo.js` để tự động chọn logo phù hợp với theme.
- AdminShell cố định theme sáng nên import trực tiếp `logo-light.jpg`.
- Nếu logo trong ảnh Stitch khác với logo chính thức thì bỏ qua logo trong ảnh và sử dụng file logo chính thức.
