-- Xóa permission dự phòng chưa có nghiệp vụ/API; khóa ngoại role_permissions dùng ON DELETE CASCADE.
DELETE FROM `permissions`
WHERE `code` = 'MODERATION_PROPOSAL_CREATE';
