import test from 'node:test';
import assert from 'node:assert/strict';
import {
  PROFILE_REPORT_REASONS,
  getProfileReportReasonLabel,
} from '../src/features/profile/constants/profileReportReasons.js';

test('báo cáo trang cá nhân chỉ cung cấp đúng sáu lý do đã chốt', () => {
  assert.deepEqual(PROFILE_REPORT_REASONS.map((item) => item.value), [
    'PROHIBITED_CONTENT',
    'IMPERSONATION',
    'UNDER_MINIMUM_AGE',
    'SCAM_OR_FRAUD',
    'FALSE_INFORMATION',
    'VIOLENCE_OR_DANGEROUS_ORGANIZATION',
  ]);
  assert.equal(getProfileReportReasonLabel('SCAM_OR_FRAUD'), 'Lừa đảo hoặc gian lận');
});
