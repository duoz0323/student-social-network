export const PROFILE_REPORT_REASONS = Object.freeze([
  { value: 'PROHIBITED_CONTENT', label: 'Đăng nội dung không được phép hiển thị' },
  { value: 'IMPERSONATION', label: 'Tài khoản giả mạo ai đó' },
  { value: 'UNDER_MINIMUM_AGE', label: 'Chưa đủ độ tuổi tối thiểu' },
  { value: 'SCAM_OR_FRAUD', label: 'Lừa đảo hoặc gian lận' },
  { value: 'FALSE_INFORMATION', label: 'Thông tin sai sự thật' },
  { value: 'VIOLENCE_OR_DANGEROUS_ORGANIZATION', label: 'Bạo lực hoặc tổ chức nguy hiểm' },
]);

export function getProfileReportReasonLabel(reason) {
  return PROFILE_REPORT_REASONS.find((item) => item.value === reason)?.label || reason || 'Không xác định';
}
