# Workflow review code

> `README.md` tại thư mục gốc là nguồn sự thật duy nhất và có mức ưu tiên cao nhất. Review phải đánh giá code, test, SQL và tài liệu theo trạng thái đích trong README.

## 1. Phạm vi review

Kiểm tra:

- Đúng nghiệp vụ.
- Đúng kiến trúc.
- Bảo mật.
- Hiệu năng.
- Validation.
- Phân quyền.
- Khả năng bảo trì.
- Test.
- Comment.
- Tài liệu.

Riêng Auth/Profile MVP cần kiểm tra thêm:

- Mỗi hành vi quan trọng phải truy ngược được tới mục tương ứng trong README.
- API, transaction, provider verification, token handling, linking và onboarding phải bao phủ các nhánh README quy định.
- SQL/DBML, Entity, Repository, source và test phải được so sánh theo cùng một trạng thái đích.
- Không chấp nhận hành vi cũ chỉ vì test hiện tại đang kiểm chứng hành vi đó.
- Dữ liệu nhạy cảm, quyền, concurrency, rollback và state transition phải có test phù hợp.

## 2. Mức độ vấn đề

- Critical: lỗ hổng, mất dữ liệu, sai quyền nghiêm trọng.
- High: lỗi nghiệp vụ chính hoặc crash.
- Medium: hiệu năng, maintainability hoặc edge case.
- Low: naming, style hoặc cải thiện nhỏ.

## 3. Cách báo cáo

Mỗi vấn đề gồm:

- Mức độ.
- File/dòng.
- Mô tả.
- Nguyên nhân.
- Ảnh hưởng.
- Hướng sửa.

Không chỉ đưa nhận xét chung chung.
