import test from 'node:test';
import assert from 'node:assert/strict';
import {
  ADMIN_USER_BLOCK_REASONS,
  getAdminUserBlockReasonLabel,
  isAdminUserBlockReason,
} from '../src/features/admin/constants/adminUserBlockReasons.js';

test('danh sách lý do khóa khớp chính xác enum Backend', () => {
  assert.deepEqual(
    ADMIN_USER_BLOCK_REASONS.map((reason) => reason.value),
    ['SPAM', 'HARASSMENT', 'HARMFUL_CONTENT', 'FAKE_ACCOUNT', 'REPEATED_VIOLATION', 'PROFILE_VIOLATION', 'OTHER'],
  );
});

test('chỉ chấp nhận mã lý do khóa được Backend công bố', () => {
  assert.equal(isAdminUserBlockReason('SPAM'), true);
  assert.equal(isAdminUserBlockReason('HARMFUL_CONTENT'), true);
  assert.equal(isAdminUserBlockReason(''), false);
  assert.equal(isAdminUserBlockReason('INVALID'), false);
});

test('hiển thị nhãn tiếng Việt cho lý do khóa trong chi tiết người dùng', () => {
  assert.equal(getAdminUserBlockReasonLabel('FAKE_ACCOUNT'), 'Tài khoản giả mạo');
  assert.equal(getAdminUserBlockReasonLabel('REPEATED_VIOLATION'), 'Vi phạm nhiều lần');
  assert.equal(getAdminUserBlockReasonLabel('PROFILE_VIOLATION'), 'Vi phạm trang cá nhân');
  assert.equal(getAdminUserBlockReasonLabel(null), '—');
});
