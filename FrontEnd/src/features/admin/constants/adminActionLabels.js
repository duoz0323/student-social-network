// Giữ mã enum để lọc API, chỉ Việt hóa phần nhãn hiển thị cho quản trị viên.
export const ADMIN_ACTION_OPTIONS = Object.freeze([
  { value: 'BLOCK_USER', label: 'Khóa tài khoản' },
  { value: 'UNBLOCK_USER', label: 'Mở khóa tài khoản' },
  { value: 'UPDATE_USER_PROFILE', label: 'Cập nhật hồ sơ người dùng' },
  { value: 'CREATE_HASHTAG', label: 'Tạo hashtag' },
  { value: 'UPDATE_HASHTAG', label: 'Đổi tên hashtag' },
  { value: 'DELETE_HASHTAG', label: 'Xóa hashtag' },
  { value: 'HIDE_POST', label: 'Ẩn bài viết' },
  { value: 'RESTORE_POST', label: 'Khôi phục bài viết' },
  { value: 'RESOLVE_REPORT', label: 'Xác nhận báo cáo' },
  { value: 'REJECT_REPORT', label: 'Từ chối báo cáo' },
  { value: 'RESOLVE_MODERATION_CASE', label: 'Xử lý hồ sơ kiểm duyệt có vi phạm' },
  { value: 'REJECT_MODERATION_CASE', label: 'Kết luận hồ sơ kiểm duyệt không vi phạm' },
  { value: 'RESOLVE_PROFILE_REPORT', label: 'Xác nhận báo cáo trang cá nhân' },
  { value: 'REJECT_PROFILE_REPORT', label: 'Từ chối báo cáo trang cá nhân' },
  { value: 'CREATE_ACADEMIC_DATA', label: 'Tạo dữ liệu học thuật' },
  { value: 'UPDATE_ACADEMIC_DATA', label: 'Cập nhật dữ liệu học thuật' },
  { value: 'CHANGE_ACADEMIC_STATUS', label: 'Đổi trạng thái dữ liệu học thuật' },
  { value: 'CREATE_ADMIN', label: 'Tạo quản trị viên' },
  { value: 'UPDATE_ADMIN', label: 'Cập nhật quản trị viên' },
  { value: 'UPDATE_ADMIN_PROFILE', label: 'Cập nhật hồ sơ quản trị viên' },
  { value: 'DISABLE_ADMIN', label: 'Vô hiệu hóa quản trị viên' },
  { value: 'ENABLE_ADMIN', label: 'Mở khóa quản trị viên' },
  { value: 'RESET_ADMIN_PASSWORD', label: 'Cấp lại mật khẩu quản trị viên' },
  { value: 'CHANGE_ADMIN_PASSWORD', label: 'Quản trị viên đổi mật khẩu' },
  { value: 'ASSIGN_ADMIN_ROLE', label: 'Gán vai trò quản trị' },
  { value: 'REVOKE_ADMIN_ROLE', label: 'Thu hồi vai trò quản trị' },
  { value: 'CREATE_ADMIN_ROLE', label: 'Tạo vai trò quản trị' },
  { value: 'UPDATE_ROLE_PERMISSIONS', label: 'Cập nhật quyền của vai trò' },
  { value: 'CREATE_MANAGED_SOCIAL_IDENTITY', label: 'Tạo danh tính xã hội được quản lý' },
  { value: 'DISABLE_MANAGED_SOCIAL_IDENTITY', label: 'Vô hiệu hóa danh tính xã hội được quản lý' },
  { value: 'COLLABORATOR_POST_CREATED', label: 'Cộng tác viên tạo bài viết' },
  { value: 'COLLABORATOR_POST_UPDATED', label: 'Cộng tác viên cập nhật bài viết' },
  { value: 'COLLABORATOR_POST_DELETED', label: 'Cộng tác viên xóa bài viết' },
  { value: 'MODERATION_SUGGESTION_CREATED', label: 'Tạo đề xuất kiểm duyệt' },
  { value: 'MODERATION_SUGGESTION_ACCEPTED', label: 'Chấp nhận đề xuất kiểm duyệt' },
  { value: 'MODERATION_SUGGESTION_REJECTED', label: 'Từ chối đề xuất kiểm duyệt' },
]);

const ADMIN_ACTION_LABEL_BY_VALUE = new Map(
  ADMIN_ACTION_OPTIONS.map((option) => [option.value, option.label]),
);

// Ưu tiên nhãn Việt hóa tại UI; fallback giữ khả năng hiển thị enum mới chưa được Frontend cập nhật.
export function getAdminActionLabel(actionType, fallback = actionType) {
  return ADMIN_ACTION_LABEL_BY_VALUE.get(actionType) ?? fallback ?? 'Thao tác quản trị';
}
