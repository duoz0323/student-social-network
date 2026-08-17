import assert from 'node:assert/strict';
import test from 'node:test';
import { getModerationReasonLabel, isModerationDetailNotification } from '../src/features/notification/utils/moderationNotification.js';

test('mở modal cho thông báo ẩn bài và hai mức cảnh báo vi phạm', () => {
  assert.equal(isModerationDetailNotification({ type: 'POST_HIDDEN_BY_ADMIN' }), true);
  assert.equal(isModerationDetailNotification({ type: 'CONTENT_VIOLATION_WARNING' }), true);
  assert.equal(isModerationDetailNotification({ type: 'CONTENT_VIOLATION_FINAL_WARNING' }), true);
  assert.equal(isModerationDetailNotification({ type: 'POST_LIKE' }), false);
});

test('Việt hóa lý do xử lý nhưng không suy đoán mã không biết', () => {
  assert.equal(getModerationReasonLabel('SCHOOL_POLICY_VIOLATION'), 'Vi phạm quy định của nhà trường');
  assert.equal(getModerationReasonLabel('UNKNOWN'), 'Không có lý do chi tiết được lưu cho thông báo này.');
});
