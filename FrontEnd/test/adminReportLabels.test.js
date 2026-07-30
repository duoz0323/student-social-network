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

test('Việt hóa đầy đủ trạng thái báo cáo nhưng giữ nguyên mã enum Backend', () => {
  assert.deepEqual(ADMIN_REPORT_STATUSES, [
    { value: 'PENDING', label: 'Đang chờ' },
    { value: 'RESOLVED', label: 'Đã xử lý' },
    { value: 'REJECTED', label: 'Đã từ chối' },
  ]);
  assert.equal(getAdminReportStatusLabel('RESOLVED'), 'Đã xử lý');
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

test('hiển thị hình thức xử lý ở chi tiết báo cáo', () => {
  assert.equal(getAdminReportDetailStatusLabel('PENDING'), 'Đang chờ');
  assert.equal(getAdminReportDetailStatusLabel('RESOLVED'), 'Đã xử lý: Ẩn bài');
  assert.equal(getAdminReportDetailStatusLabel('REJECTED'), 'Đã xử lý: Từ chối');
});

test('đổi lý do báo cáo sang đúng enum lý do ẩn bài của Backend', () => {
  assert.equal(getAdminPostHideReasonFromReportReason('HARASSMENT'), 'HARASSMENT');
  assert.equal(getAdminPostHideReasonFromReportReason('INAPPROPRIATE'), 'INAPPROPRIATE_CONTENT');
  assert.equal(getAdminPostHideReasonFromReportReason('UNKNOWN'), 'OTHER');
});
