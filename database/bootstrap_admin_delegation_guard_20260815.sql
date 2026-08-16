-- Chỉ SUPER_ADMIN Bootstrap được giữ các quyền có thể tiếp tục phân quyền.
-- Migration này không tự hạ role của dữ liệu ADMIN cũ vì email Bootstrap thuộc cấu hình môi trường.
DELETE rp
FROM role_permissions rp
JOIN roles r ON r.id = rp.role_id
JOIN permissions p ON p.id = rp.permission_id
WHERE r.code <> 'SUPER_ADMIN'
  AND p.code IN ('ADMIN_CREATE', 'ADMIN_ROLE_ASSIGN', 'ADMIN_ROLE_REVOKE');

-- Kiểm tra thủ công sau migration: kết quả phải chỉ còn đúng email Bootstrap.
SELECT u.id, u.email
FROM users u
JOIN admin_roles ar ON ar.admin_id = u.id
JOIN roles r ON r.id = ar.role_id
WHERE r.code = 'SUPER_ADMIN';
