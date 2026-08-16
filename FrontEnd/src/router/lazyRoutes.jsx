import { lazy, Suspense } from 'react';

// Các màn hình sau đăng nhập được tách theo route để guest không phải tải Feed và Admin ngay từ đầu.
export const AuthProvidersPage = lazy(() => import('../features/auth/pages/AuthProvidersPage.jsx'));
export const FeedPage = lazy(() => import('../features/feed/pages/FeedPage.jsx'));
export const PostDetailPage = lazy(() => import('../features/post/pages/PostDetailPage.jsx'));
export const ProfilePage = lazy(() => import('../features/profile/pages/ProfilePage.jsx'));
export const BlockedUsersPage = lazy(() => import('../features/profile/pages/BlockedUsersPage.jsx'));
export const RestrictedUsersPage = lazy(() => import('../features/profile/pages/RestrictedUsersPage.jsx'));
export const SavedPostsPage = lazy(() => import('../features/post/pages/SavedPostsPage.jsx'));
export const LikedPostsPage = lazy(() => import('../features/post/pages/LikedPostsPage.jsx'));
export const SearchPage = lazy(() => import('../features/search/pages/SearchPage.jsx'));
export const NotificationsPage = lazy(() => import('../features/notification/pages/NotificationsPage.jsx'));
export const MessagingPage = lazy(() => import('../features/messaging/pages/MessagingPage.jsx'));
export const AdminDashboardPage = lazy(() => import('../features/admin/pages/AdminDashboardPage.jsx'));
export const AdminUsersPage = lazy(() => import('../features/admin/pages/AdminUsersPage.jsx'));
export const AdminUserAnalyticsPage = lazy(() => import('../features/admin/pages/AdminUserAnalyticsPage.jsx'));
export const AdminPostAnalyticsPage = lazy(() => import('../features/admin/pages/AdminPostAnalyticsPage.jsx'));
export const AdminHashtagAnalyticsPage = lazy(() => import('../features/admin/pages/AdminHashtagAnalyticsPage.jsx'));
export const AdminPostsPage = lazy(() => import('../features/admin/pages/AdminPostsPage.jsx'));
export const AdminHashtagsPage = lazy(() => import('../features/admin/pages/AdminHashtagsPage.jsx'));
export const AdminAcademicPage = lazy(() => import('../features/admin/pages/AdminAcademicPage.jsx'));
export const AdminPostDetailPage = lazy(() => import('../features/admin/pages/AdminPostDetailPage.jsx'));
export const AdminReportsPage = lazy(() => import('../features/admin/pages/AdminReportsPage.jsx'));
export const AdminReportDetailPage = lazy(() => import('../features/admin/pages/AdminReportDetailPage.jsx'));
export const AdminProfileReportDetailPage = lazy(() => import('../features/admin/pages/AdminProfileReportDetailPage.jsx'));
export const AdminActionsPage = lazy(() => import('../features/admin/pages/AdminActionsPage.jsx'));
export const AdminManagementPage = lazy(() => import('../features/admin/pages/AdminManagementPage.jsx'));
export const AdminRolePermissionsPage = lazy(() => import('../features/admin/pages/AdminRolePermissionsPage.jsx'));
export const AdminProfilePage = lazy(() => import('../features/admin/pages/AdminProfilePage.jsx'));
export const CollaboratorDashboardPage = lazy(() => import('../features/admin/collaborator/pages/CollaboratorDashboardPage.jsx'));
export const CollaboratorPostsPage = lazy(() => import('../features/admin/collaborator/pages/CollaboratorPostsPage.jsx'));
export const CollaboratorPostDetailPage = lazy(() => import('../features/admin/collaborator/pages/CollaboratorPostDetailPage.jsx'));
export const CollaboratorPostAnalyticsPage = lazy(() => import('../features/admin/collaborator/pages/CollaboratorPostAnalyticsPage.jsx'));
export const CollaboratorHashtagsPage = lazy(() => import('../features/admin/collaborator/pages/CollaboratorHashtagsPage.jsx'));

export function LazyRouteBoundary({ children }) {
  const fallback = (
    <div className="flex min-h-48 items-center justify-center text-sm text-[var(--app-muted)]">
      Đang tải...
    </div>
  );
  return <Suspense fallback={fallback}>{children}</Suspense>;
}
