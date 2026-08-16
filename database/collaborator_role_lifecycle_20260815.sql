-- Đồng bộ COLLABORATOR thành role hệ thống cố định; provisioning danh tính được Backend thực hiện khi gán role.
START TRANSACTION;

UPDATE `roles`
SET `reserved` = 1,
    `description` = 'Tạo nội dung, tương tác xã hội và gửi đề xuất kiểm duyệt bằng Managed Social Identity.'
WHERE `code` = 'COLLABORATOR';

-- Khóa lại đúng bộ quyền Collaborator nếu ma trận từng bị chỉnh thủ công trước khi role trở thành reserved.
DELETE rp
FROM `role_permissions` rp
JOIN `roles` r ON r.id = rp.role_id
WHERE r.code = 'COLLABORATOR';

INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `roles` r
CROSS JOIN `permissions` p
WHERE r.code = 'COLLABORATOR'
  AND p.code LIKE 'COLLABORATOR\_%';

COMMIT;
