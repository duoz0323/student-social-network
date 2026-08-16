export const ADMIN_PERMISSIONS = Object.freeze({
  DASHBOARD_BASIC_VIEW: 'DASHBOARD_BASIC_VIEW', USER_VIEW: 'USER_VIEW', USER_SEARCH: 'USER_SEARCH',
  USER_FILTER: 'USER_FILTER', USER_DETAIL_VIEW: 'USER_DETAIL_VIEW', USER_BLOCK: 'USER_BLOCK',
  USER_PROFILE_UPDATE: 'USER_PROFILE_UPDATE',
  USER_UNBLOCK: 'USER_UNBLOCK', USER_ANALYTICS_VIEW: 'USER_ANALYTICS_VIEW', POST_VIEW: 'POST_VIEW',
  POST_HIDE: 'POST_HIDE', POST_RESTORE: 'POST_RESTORE', HASHTAG_VIEW: 'HASHTAG_VIEW',
  HASHTAG_SEARCH: 'HASHTAG_SEARCH', HASHTAG_DELETE: 'HASHTAG_DELETE', REPORT_VIEW: 'REPORT_VIEW',
  REPORT_DETAIL_VIEW: 'REPORT_DETAIL_VIEW', REPORT_RESOLVE_NO_VIOLATION: 'REPORT_RESOLVE_NO_VIOLATION',
  REPORT_RESOLVE_ACTION: 'REPORT_RESOLVE_ACTION',
  ADMIN_VIEW: 'ADMIN_VIEW', ADMIN_DETAIL_VIEW: 'ADMIN_DETAIL_VIEW', ADMIN_CREATE: 'ADMIN_CREATE',
  ADMIN_UPDATE: 'ADMIN_UPDATE', ADMIN_DISABLE: 'ADMIN_DISABLE', ADMIN_ENABLE: 'ADMIN_ENABLE',
  ADMIN_PASSWORD_RESET: 'ADMIN_PASSWORD_RESET',
  ADMIN_ROLE_ASSIGN: 'ADMIN_ROLE_ASSIGN',
  ADMIN_ROLE_REVOKE: 'ADMIN_ROLE_REVOKE',
  COLLABORATOR_DASHBOARD_VIEW: 'COLLABORATOR_DASHBOARD_VIEW',
  COLLABORATOR_POST_VIEW_OWN: 'COLLABORATOR_POST_VIEW_OWN',
  COLLABORATOR_POST_CREATE: 'COLLABORATOR_POST_CREATE',
  COLLABORATOR_POST_UPDATE_OWN: 'COLLABORATOR_POST_UPDATE_OWN',
  COLLABORATOR_POST_DELETE_OWN: 'COLLABORATOR_POST_DELETE_OWN',
  COLLABORATOR_POST_ANALYTICS_VIEW: 'COLLABORATOR_POST_ANALYTICS_VIEW',
  COLLABORATOR_HASHTAG_VIEW: 'COLLABORATOR_HASHTAG_VIEW',
});

export const ALL_ADMIN_PERMISSIONS = Object.freeze(Object.values(ADMIN_PERMISSIONS));

// Ba quyền này chỉ thuộc tài khoản Bootstrap, không được ủy quyền cho role hỗ trợ.
export const NON_DELEGABLE_ADMIN_PERMISSIONS = Object.freeze([
  ADMIN_PERMISSIONS.ADMIN_CREATE,
  ADMIN_PERMISSIONS.ADMIN_ROLE_ASSIGN,
  ADMIN_PERMISSIONS.ADMIN_ROLE_REVOKE,
]);

/** Nhãn nghiệp vụ hiển thị; mã role kỹ thuật vẫn được giữ nguyên khi trao đổi với Backend. */
export const ADMIN_ROLE_LABELS = Object.freeze({
  SUPER_ADMIN: 'Quản trị viên',
  USER_MANAGER: 'Quản lý người dùng',
  MODERATOR: 'Xử lý báo cáo',
  ADS_MANAGER: 'Quản lý ADS',
  COLLABORATOR: 'Cộng tác viên',
});

export function getAdminRoleLabel(roleCode, fallback = '') {
  return ADMIN_ROLE_LABELS[roleCode] || fallback || roleCode;
}

const ADMIN_ROLE_PRIORITY = Object.freeze([
  'SUPER_ADMIN',
  'USER_MANAGER',
  'MODERATOR',
  'ADS_MANAGER',
  'COLLABORATOR',
]);

/** Nhãn vai trò đại diện dùng ở hồ sơ sidebar; tài khoản nhiều role hiển thị role có ưu tiên cao nhất. */
export function getPrimaryAdminRoleLabel(adminRoles = []) {
  const primaryRole = ADMIN_ROLE_PRIORITY.find((roleCode) => adminRoles.includes(roleCode));
  if (primaryRole === 'SUPER_ADMIN') return 'Master Admin';
  return primaryRole ? getAdminRoleLabel(primaryRole) : 'Quản trị viên';
}

/**
 * Chọn nhóm menu theo role thực tế thay vì permission hiệu lực.
 * SUPER_ADMIN có toàn bộ permission nên không được suy luận nhầm thành Cộng tác viên.
 */
export function getAdminNavigationScopes(adminRoles = []) {
  const roles = new Set(adminRoles);
  const showCollaborator = roles.has('COLLABORATOR');
  const showRegularAdmin = !showCollaborator
    || [...roles].some((roleCode) => roleCode !== 'COLLABORATOR');
  return { showRegularAdmin, showCollaborator };
}
