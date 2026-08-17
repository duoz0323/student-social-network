import test from 'node:test';
import assert from 'node:assert/strict';
import { ADMIN_ACTION_OPTIONS, getAdminActionLabel } from '../src/features/admin/constants/adminActionLabels.js';

test('bộ lọc lịch sử quản trị giữ enum API nhưng hiển thị toàn bộ nhãn tiếng Việt', () => {
  assert.equal(ADMIN_ACTION_OPTIONS.length, 36);
  assert.equal(new Set(ADMIN_ACTION_OPTIONS.map((option) => option.value)).size, 36);
  assert.equal(new Set(ADMIN_ACTION_OPTIONS.map((option) => option.label)).size, 36);
  assert.ok(ADMIN_ACTION_OPTIONS.every((option) => /^[A-Z0-9_]+$/.test(option.value)));
  assert.ok(ADMIN_ACTION_OPTIONS.every((option) => !/^[A-Z0-9_]+$/.test(option.label)));
  assert.deepEqual(
    ADMIN_ACTION_OPTIONS.find((option) => option.value === 'BLOCK_USER'),
    { value: 'BLOCK_USER', label: 'Khóa tài khoản' },
  );
  assert.deepEqual(
    ADMIN_ACTION_OPTIONS.find((option) => option.value === 'MODERATION_SUGGESTION_ACCEPTED'),
    { value: 'MODERATION_SUGGESTION_ACCEPTED', label: 'Chấp nhận đề xuất kiểm duyệt' },
  );
  assert.equal(getAdminActionLabel('COLLABORATOR_POST_CREATED', 'Collaborator tạo bài viết'), 'Cộng tác viên tạo bài viết');
  assert.equal(getAdminActionLabel('FUTURE_ACTION', 'Thao tác mới'), 'Thao tác mới');
});
