const ADMIN_LANDING_ROUTES = Object.freeze([
  ['COLLABORATOR_DASHBOARD_VIEW', '/admin/collaborator'],
  ['DASHBOARD_BASIC_VIEW', '/admin'],
  ['USER_VIEW', '/admin/users'],
  ['USER_ANALYTICS_VIEW', '/admin/user-analytics'],
  ['POST_VIEW', '/admin/posts'],
  ['HASHTAG_VIEW', '/admin/hashtags'],
  ['REPORT_VIEW', '/admin/reports'],
  ['ADMIN_VIEW', '/admin/admins'],
]);

export function getAuthenticatedHome({ role, user, profileCompleted, permissions }) {
  if (!profileCompleted) return '/onboarding/profile';
  if ((role ?? user?.role) !== 'ADMIN') return '/feed/for-you';

  // Login response lưu permission trong user, còn AuthContext phẳng hóa permission ở state.
  const grantedPermissions = new Set(permissions ?? user?.permissions ?? []);
  const adminRoles = new Set(user?.adminRoles ?? []);
  // SUPER_ADMIN cũng có toàn bộ permission Cộng tác viên, nhưng phải vào trang quản trị chính.
  if (adminRoles.has('SUPER_ADMIN')) return '/admin';
  if (adminRoles.has('COLLABORATOR')
      && grantedPermissions.has('COLLABORATOR_DASHBOARD_VIEW')) return '/admin/collaborator';
  return ADMIN_LANDING_ROUTES.find(([permission]) => grantedPermissions.has(permission))?.[1] ?? '/403';
}

const RETURN_ROUTE_PREFIXES = ['/feed/', '/posts/', '/profile/', '/admin/'];
const RETURN_ROUTE_EXACT = new Set(['/saved', '/liked', '/search', '/admin']);

function canAccessAdminPath(requestedPath, session) {
  const permissions = new Set(session.permissions ?? session.user?.permissions ?? []);
  const adminRoles = new Set(session.adminRoles ?? session.user?.adminRoles ?? []);
  // Mọi ADMIN, gồm Cộng tác viên, đều được tự quản lý hồ sơ và mật khẩu.
  if (requestedPath === '/admin/profile') return true;
  if (requestedPath.startsWith('/admin/collaborator')) {
    // Hai khu vực đã được gỡ khỏi UI; URL cũ không được khôi phục dù token còn permission legacy.
    if (requestedPath.includes('/moderation-suggestions') || requestedPath.includes('/explore')) return false;
    if (requestedPath.includes('/hashtags')) return permissions.has('COLLABORATOR_HASHTAG_VIEW');
    if (requestedPath.includes('/posts/create')) return permissions.has('COLLABORATOR_POST_CREATE');
    if (requestedPath.includes('/analytics')) return permissions.has('COLLABORATOR_POST_ANALYTICS_VIEW');
    if (requestedPath.includes('/posts')) return permissions.has('COLLABORATOR_POST_VIEW_OWN');
    return permissions.has('COLLABORATOR_DASHBOARD_VIEW');
  }
  if (requestedPath === '/admin') return permissions.has('DASHBOARD_BASIC_VIEW');
  if (requestedPath.startsWith('/admin/users')) return permissions.has('USER_VIEW');
  if (requestedPath.startsWith('/admin/user-analytics')) return permissions.has('USER_ANALYTICS_VIEW');
  if (requestedPath.startsWith('/admin/posts')) return permissions.has('POST_VIEW');
  if (requestedPath.startsWith('/admin/hashtags')) return permissions.has('HASHTAG_VIEW');
  if (requestedPath.startsWith('/admin/reports') || requestedPath.startsWith('/admin/profile-reports')) {
    return permissions.has('REPORT_VIEW');
  }
  if (requestedPath.startsWith('/admin/admins')) return permissions.has('ADMIN_VIEW');
  if (requestedPath.startsWith('/admin/permissions')) return adminRoles.has('SUPER_ADMIN');
  if (requestedPath.startsWith('/admin/actions')) return adminRoles.has('SUPER_ADMIN');
  return false;
}

export function getSafeReturnPath(requestedPath, session) {
  if (typeof requestedPath !== 'string' || !requestedPath.startsWith('/') || requestedPath.startsWith('//') || requestedPath.includes('\\')) return null;
  if (!session.profileCompleted || requestedPath.startsWith('/onboarding')) return null;
  if (!RETURN_ROUTE_EXACT.has(requestedPath) && !RETURN_ROUTE_PREFIXES.some((prefix) => requestedPath.startsWith(prefix))) return null;
  if (requestedPath.startsWith('/admin')
      && (session.user.role !== 'ADMIN' || !canAccessAdminPath(requestedPath, session))) return null;
  return requestedPath;
}
