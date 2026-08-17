import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import AdminShell from '../components/layout/AdminShell.jsx';
import SettingsLayout from '../components/layout/SettingsLayout.jsx';
import UserShell from '../components/layout/UserShell.jsx';
import LoginPage from '../features/auth/pages/LoginPage.jsx';
import ForgotPasswordPage from '../features/auth/pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from '../features/auth/pages/ResetPasswordPage.jsx';
import { PasswordRecoveryProvider } from '../features/auth/hooks/usePasswordRecovery.js';
import RegisterPage from '../features/auth/pages/RegisterPage.jsx';
import VerifyRegistrationOtpPage from '../features/auth/pages/VerifyRegistrationOtpPage.jsx';
import SocialConflictPendingPage from '../features/auth/pages/SocialConflictPendingPage.jsx';
import OnboardingProfilePage from '../features/auth/pages/OnboardingProfilePage.jsx';
import OnboardingSuccessPage from '../features/auth/pages/OnboardingSuccessPage.jsx';
import ErrorPage from '../features/system/pages/ErrorPage.jsx';
import { AdminPermissionRoute, AdminProfileEntryRoute, AdminRoute, GuestRoute, ProfileCompletionRoute, ProtectedRoute, RootRedirect } from './routeGuards.jsx';
import { ADMIN_PERMISSIONS } from '../features/admin/constants/adminRbac.js';
import {
  AdminActionsPage,
  AccountStandingPage,
  CommunityStandardsPage,
  AdminAcademicPage,
  AdminManagementPage,
  AdminRolePermissionsPage,
  AdminProfilePage,
  AdminNotificationsPage,
  AdminDashboardPage,
  AdminHashtagsPage,
  AdminPostsPage,
  AdminPostDetailPage,
  AdminReportDetailPage,
  AdminProfileReportDetailPage,
  AdminReportsPage,
  AdminModerationSuggestionsPage,
  AdminUsersPage,
  AdminUserAnalyticsPage,
  AdminPostAnalyticsPage,
  AdminHashtagAnalyticsPage,
  AuthProvidersPage,
  FeedPage,
  LazyRouteBoundary,
  LikedPostsPage,
  NotificationsPage,
  MessagingPage,
  PostDetailPage,
  ProfilePage,
  BlockedUsersPage,
  RestrictedUsersPage,
  CollaboratorDashboardPage,
  CollaboratorPostsPage,
  CollaboratorPostDetailPage,
  CollaboratorPostAnalyticsPage,
  CollaboratorHashtagsPage,
  CollaboratorIdentityPage,
  CollaboratorExplorePage,
  CollaboratorModerationSuggestionsPage,
  SavedPostsPage,
  SearchPage,
} from './lazyRoutes.jsx';

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  { path: '/policies/community-standards', element: <LazyRouteBoundary><CommunityStandardsPage /></LazyRouteBoundary> },
  { path: '/login', element: <GuestRoute><LoginPage /></GuestRoute> },
  {
    element: <GuestRoute><PasswordRecoveryProvider><Outlet /></PasswordRecoveryProvider></GuestRoute>,
    children: [
      { path: '/forgot-password', element: <ForgotPasswordPage /> },
      { path: '/reset-password', element: <ResetPasswordPage /> },
    ],
  },
  { path: '/register', element: <GuestRoute><RegisterPage /></GuestRoute> },
  { path: '/register/verify', element: <GuestRoute><VerifyRegistrationOtpPage /></GuestRoute> },
  { path: '/auth/social-conflict', element: <GuestRoute><SocialConflictPendingPage /></GuestRoute> },
  {
    path: '/onboarding/profile',
    element: <ProfileCompletionRoute allowCompleted><OnboardingProfilePage /></ProfileCompletionRoute>,
  },
  {
    path: '/onboarding/success',
    element: (
      <ProfileCompletionRoute requireCompleted allowCompletionTransition>
        <OnboardingSuccessPage />
      </ProfileCompletionRoute>
    ),
  },
  {
    element: <ProtectedRoute><LazyRouteBoundary><UserShell /></LazyRouteBoundary></ProtectedRoute>,
    children: [
      { path: '/feed/:type', element: <FeedPage /> },
      { path: '/posts/:postId', element: <PostDetailPage /> },
      { path: '/profile/me', element: <ProfilePage self /> },
      { path: '/profile/:userId', element: <ProfilePage /> },
      { path: '/saved', element: <SavedPostsPage /> },
      { path: '/liked', element: <LikedPostsPage /> },
      { path: '/search', element: <SearchPage /> },
      { path: '/notifications', element: <NotificationsPage /> },
      { path: '/messages', element: <MessagingPage /> },
      { path: '/messages/:conversationId', element: <MessagingPage /> },
      {
        path: '/settings',
        element: <SettingsLayout />,
        children: [
          { index: true, element: <Navigate to="auth-providers" replace /> },
          { path: 'auth-providers', element: <AuthProvidersPage /> },
          { path: 'account-status', element: <AccountStandingPage /> },
          { path: 'blocked-users', element: <BlockedUsersPage /> },
          { path: 'restricted-users', element: <RestrictedUsersPage /> },
        ],
      },
    ],
  },
  {
    path: '/admin',
    element: <AdminRoute><LazyRouteBoundary><AdminShell /></LazyRouteBoundary></AdminRoute>,
    children: [
      { path: 'notifications', element: <AdminNotificationsPage /> },
      { path: 'profile', element: <AdminProfileEntryRoute><AdminProfilePage /></AdminProfileEntryRoute> },
      { index: true, element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.DASHBOARD_BASIC_VIEW}><AdminDashboardPage /></AdminPermissionRoute> },
      { path: 'users', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.USER_VIEW}><AdminUsersPage /></AdminPermissionRoute> },
      { path: 'user-analytics', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.USER_ANALYTICS_VIEW}><AdminUserAnalyticsPage /></AdminPermissionRoute> },
      { path: 'post-analytics', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.POST_VIEW}><AdminPostAnalyticsPage /></AdminPermissionRoute> },
      { path: 'hashtag-analytics', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.HASHTAG_VIEW}><AdminHashtagAnalyticsPage /></AdminPermissionRoute> },
      { path: 'posts', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.POST_VIEW}><AdminPostsPage /></AdminPermissionRoute> },
      { path: 'hashtags', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.HASHTAG_VIEW}><AdminHashtagsPage /></AdminPermissionRoute> },
      { path: 'academic', element: <AdminAcademicPage /> },
      { path: 'posts/:postId', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.POST_VIEW}><AdminPostDetailPage /></AdminPermissionRoute> },
      { path: 'reports', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.REPORT_VIEW}><AdminReportsPage /></AdminPermissionRoute> },
      { path: 'reports/:caseId', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.REPORT_DETAIL_VIEW}><AdminReportDetailPage /></AdminPermissionRoute> },
      { path: 'moderation-suggestions', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.MODERATION_SUGGESTION_VIEW}><AdminModerationSuggestionsPage /></AdminPermissionRoute> },
      { path: 'moderation-suggestions/:suggestionId', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.MODERATION_SUGGESTION_DETAIL_VIEW}><AdminModerationSuggestionsPage /></AdminPermissionRoute> },
      { path: 'profile-reports/:caseId', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.REPORT_DETAIL_VIEW}><AdminProfileReportDetailPage /></AdminPermissionRoute> },
      { path: 'admins', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.ADMIN_VIEW}><AdminManagementPage /></AdminPermissionRoute> },
      { path: 'permissions', element: <AdminPermissionRoute adminRole="SUPER_ADMIN"><AdminRolePermissionsPage /></AdminPermissionRoute> },
      { path: 'actions', element: <AdminPermissionRoute adminRole="SUPER_ADMIN"><AdminActionsPage /></AdminPermissionRoute> },
      { path: 'collaborator', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_DASHBOARD_VIEW}><CollaboratorDashboardPage /></AdminPermissionRoute> },
      { path: 'collaborator/profile', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_DASHBOARD_VIEW}><CollaboratorIdentityPage /></AdminPermissionRoute> },
      { path: 'collaborator/posts', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_POST_VIEW_OWN}><CollaboratorPostsPage /></AdminPermissionRoute> },
      { path: 'collaborator/posts/create', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_POST_CREATE}><Navigate to="/admin/collaborator/posts?create=1" replace /></AdminPermissionRoute> },
      { path: 'collaborator/posts/:postId', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_POST_VIEW_OWN}><CollaboratorPostDetailPage /></AdminPermissionRoute> },
      { path: 'collaborator/posts/:postId/analytics', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_POST_ANALYTICS_VIEW}><CollaboratorPostAnalyticsPage /></AdminPermissionRoute> },
      { path: 'collaborator/hashtags', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_HASHTAG_VIEW}><CollaboratorHashtagsPage /></AdminPermissionRoute> },
      { path: 'collaborator/explore', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_EXPLORE_VIEW}><CollaboratorExplorePage /></AdminPermissionRoute> },
      { path: 'collaborator/moderation-suggestions', element: <AdminPermissionRoute permission={ADMIN_PERMISSIONS.COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN}><CollaboratorModerationSuggestionsPage /></AdminPermissionRoute> },
    ],
  },
  { path: '/register/success', element: <Navigate to="/onboarding/profile" replace /> },
  { path: '/403', element: <ErrorPage code="403" title="Không có quyền" description="Bạn không có quyền truy cập khu vực này." /> },
  { path: '/500', element: <ErrorPage code="500" title="Lỗi hệ thống" description="Đã có lỗi bất thường trong quá trình xử lý." /> },
  { path: '*', element: <ErrorPage code="404" title="Không tìm thấy" description="Trang hoặc tài nguyên không tồn tại." /> },
]);
