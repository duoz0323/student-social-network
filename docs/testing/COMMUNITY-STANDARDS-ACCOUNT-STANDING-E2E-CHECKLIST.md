# Community Standards & Account Standing — Manual E2E Checklist

Trạng thái tài liệu: `IMPLEMENTED`, automated test `TESTED`, manual E2E `NOT RUN`.

Chuẩn bị hai tài khoản USER đã hoàn tất onboarding, một tài khoản ADMIN có quyền xử lý báo cáo/người dùng và ba bài viết có thể tạo ba Moderation Case độc lập.

- [ ] 1. USER chưa có case vi phạm mở `/settings/account-status`: hiển thị `0 / 3`, còn `3`, trạng thái nhẹ nhàng.
- [ ] 2. Admin resolve case vi phạm thứ nhất bằng `RESOLVED_ACTION_TAKEN`.
- [ ] 3. USER nhận Notification cảnh báo `1/3`; realtime có thể đến ngay và reload REST vẫn còn Notification.
- [ ] 4. Account Standing hiển thị `1 / 3`, còn `2`.
- [ ] 5. Admin resolve case vi phạm thứ hai.
- [ ] 6. USER nhận final warning `2/3`.
- [ ] 7. Account Standing hiển thị `2 / 3`, còn `1`.
- [ ] 8. Admin resolve case vi phạm thứ ba.
- [ ] 9. USER chuyển `BLOCKED`, lý do `REPEATED_VIOLATION`; không có warning “còn 0 lần”.
- [ ] 10. Access/Refresh Token cũ không thể tiếp tục tạo phiên hợp lệ.
- [ ] 11. Login lại trả `ACCOUNT_BLOCKED` và hiển thị màn hình khóa chuyên biệt, không lộ Admin/reporter/internal note.
- [ ] 12. Admin mở modal xác nhận và unblock USER.
- [ ] 13. USER đăng nhập lại được.
- [ ] 14. USER thấy Notification tài khoản đã được mở khóa.
- [ ] 15. Account Standing/lịch sử vi phạm không tự reset sau unblock.
- [ ] 16. Admin hide Post: USER nhận Notification và UI không cố mở Post Detail bị ẩn.
- [ ] 17. Admin restore Post: USER nhận Notification khôi phục.
- [ ] 18. Admin chỉnh Profile vì vi phạm: USER nhận Notification public-safe.
- [ ] 19. Admin manual block: lịch sử, audit, revoke token và Notification tồn tại; Login chỉ thấy lý do public-safe.
- [ ] 20. Guest không có JWT mở được `/policies/community-standards`; kiểm tra thêm mobile, desktop, light và dark mode.

Kiểm tra concurrency bổ sung: gửi đồng thời hai request resolve cùng `caseId`; chỉ một request thành công, violation chỉ tăng một lần, không tạo warning/block/Notification trùng.
