# Personalized For You V1 – Manual E2E Checklist

> Chỉ đánh dấu feature `INTEGRATED` sau khi chạy checklist này trên Backend + Frontend + MySQL thật. Đây là ranking rule-based, không phải AI/ML.

## Chuẩn bị

- Viewer là USER `ACTIVE`, profile hoàn tất.
- Tạo ít nhất hai author hợp lệ và hơn 20 Post `PUBLISHED` để kiểm tra nhiều cursor.
- Ghi lại Academic, Interests, Follow, interaction history, hashtag và thời điểm publish để kết quả có thể lặp lại.

## Checklist

- [ ] Recency: Post 2 giờ đứng trước Post 5 ngày khi các signal khác bằng nhau.
- [ ] Follow: Post của author đang Follow nhận bonus; Unfollow rồi refresh làm bonus biến mất.
- [ ] Academic: cùng Major > chỉ cùng School khi các signal khác bằng nhau; rename master data không mất match.
- [ ] Academic inactive: profile cũ vẫn giữ reference nhưng request Feed mới không cộng signal inactive.
- [ ] Interests: common ACTIVE Interests tạo bonus đúng; Interest inactive vẫn được bảo toàn nhưng không cộng điểm.
- [ ] Historical author: Like/Comment/Save/Repost trên Post khác của Author X tăng ranking Post mới của X.
- [ ] Candidate isolation: interaction trên chính candidate chỉ ảnh hưởng engagement, không tự cộng author/hashtag history.
- [ ] Hashtag: exact cùng `hashtag_id` nhận bonus; hashtag khác hoặc tên gần giống không nhận bonus.
- [ ] Block: Block ở bất kỳ chiều nào loại toàn bộ Post của author.
- [ ] Restrict: Restrict không làm Post biến mất.
- [ ] Cold start: viewer không Follow/Academic/Interest/history vẫn nhận Feed từ recency + engagement.
- [ ] Infinite scroll: tải hơn 20 Post qua nhiều cursor không duplicate/missing với dataset tĩnh.
- [ ] Recency boundary: Post gần mốc bucket không đổi vị trí chỉ vì chuyển sang trang kế tiếp trong cùng phiên cursor.
- [ ] Refresh: Follow/Like/Save mới không cần rerank item đang render; refresh tạo ranking session mới và phản ánh signal.
- [ ] Regression: Following Feed vẫn trả `ORIGINAL`/`REPOST`; Post Detail/Profile/Saved/Liked/Search và Like/Comment/Save/Repost không đổi contract.
- [ ] UI: loading, empty, initial error, retry load-more, refresh, tab switching và BroadcastChannel/realtime reconciliation hoạt động.

## Kết quả

- Ngày chạy:
- Môi trường/commit:
- Người chạy:
- Kết quả: `PASS` / `FAIL`
- Ghi chú và bằng chứng:
