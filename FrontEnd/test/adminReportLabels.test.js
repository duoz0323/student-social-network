import assert from 'node:assert/strict';
import test from 'node:test';
import {
  ADMIN_REPORT_REASONS,
  ADMIN_REPORT_STATUSES,
  getAdminPostHideReasonFromReportReason,
  getAdminReportDetailStatusLabel,
  getAdminReportReasonLabel,
  getAdminReportStatusLabel,
} from '../src/features/admin/constants/adminReportLabels.js';
import {
  ADMIN_POST_HIDE_REASONS,
  isAdminPostHideReason,
} from '../src/features/admin/constants/adminPostHideReasons.js';

test('Việt hóa đầy đủ trạng thái hồ sơ kiểm duyệt nhưng giữ nguyên mã enum Backend', () => {
  assert.deepEqual(ADMIN_REPORT_STATUSES, [
    { value: 'OPEN', label: 'Đang chờ' },
    { value: 'RESOLVED_ACTION_TAKEN', label: 'Đã xử lý vi phạm' },
    { value: 'RESOLVED_NO_VIOLATION', label: 'Không vi phạm' },
  ]);
  assert.equal(getAdminReportStatusLabel('RESOLVED_ACTION_TAKEN'), 'Đã xử lý vi phạm');
});

test('Việt hóa đầy đủ bảy lý do báo cáo theo enum Backend', () => {
  assert.deepEqual(ADMIN_REPORT_REASONS.map((reason) => reason.value), [
    'SPAM',
    'HARASSMENT',
    'HARMFUL_CONTENT',
    'VIOLENCE',
    'MISINFORMATION',
    'INAPPROPRIATE',
    'OTHER',
  ]);
  assert.equal(getAdminReportReasonLabel('HARASSMENT'), 'Quấy rối');
  assert.equal(getAdminReportReasonLabel('HARMFUL_CONTENT'), 'Nội dung độc hại hoặc xúc phạm');
  assert.equal(getAdminReportReasonLabel('UNKNOWN'), 'UNKNOWN');
});

test('hiển thị kết luận ở chi tiết hồ sơ kiểm duyệt', () => {
  assert.equal(getAdminReportDetailStatusLabel('OPEN'), 'Đang chờ');
  assert.equal(getAdminReportDetailStatusLabel('RESOLVED_ACTION_TAKEN'), 'Đã xử lý vi phạm');
  assert.equal(getAdminReportDetailStatusLabel('RESOLVED_NO_VIOLATION'), 'Không vi phạm');
});

test('đổi lý do báo cáo sang đúng enum lý do ẩn bài của Backend', () => {
  assert.equal(getAdminPostHideReasonFromReportReason('HARASSMENT'), 'HARASSMENT');
  assert.equal(getAdminPostHideReasonFromReportReason('INAPPROPRIATE'), 'INAPPROPRIATE_CONTENT');
  assert.equal(getAdminPostHideReasonFromReportReason('UNKNOWN'), 'OTHER');
});

test('danh sách lý do ẩn bài khớp chính xác enum Backend', () => {
  assert.deepEqual(ADMIN_POST_HIDE_REASONS.map((reason) => reason.value), [
    'SPAM',
    'HARASSMENT',
    'HARMFUL_CONTENT',
    'VIOLENCE',
    'MISINFORMATION',
    'SCHOOL_POLICY_VIOLATION',
    'INAPPROPRIATE_CONTENT',
    'OTHER',
  ]);
  assert.equal(isAdminPostHideReason('SCHOOL_POLICY_VIOLATION'), true);
  assert.equal(isAdminPostHideReason('INAPPROPRIATE'), false);
});
