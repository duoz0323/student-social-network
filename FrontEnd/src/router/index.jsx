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
import { AdminRoute, GuestRoute, ProfileCompletionRoute, ProtectedRoute, RootRedirect } from './routeGuards.jsx';
import {
  AdminActionsPage,
  AdminDashboardPage,
  AdminPostsPage,
  AdminReportDetailPage,
  AdminReportsPage,
  AdminUsersPage,
  AuthProvidersPage,
  FeedPage,
  LazyRouteBoundary,
  LikedPostsPage,
  NotificationsPage,
  PostDetailPage,
  ProfilePage,
  BlockedUsersPage,
  RestrictedUsersPage,
  SavedPostsPage,
  SearchPage,
} from './lazyRoutes.jsx';

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
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
  { path: '/onboarding/profile', element: <ProfileCompletionRoute><OnboardingProfilePage /></ProfileCompletionRoute> },
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
      {
        path: '/settings',
        element: <SettingsLayout />,
        children: [
          { index: true, element: <Navigate to="auth-providers" replace /> },
          { path: 'auth-providers', element: <AuthProvidersPage /> },
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
      { index: true, element: <AdminDashboardPage /> },
      { path: 'users', element: <AdminUsersPage /> },
      { path: 'posts', element: <AdminPostsPage /> },
      { path: 'reports', element: <AdminReportsPage /> },
      { path: 'reports/:reportId', element: <AdminReportDetailPage /> },
      { path: 'actions', element: <AdminActionsPage /> },
    ],
  },
  { path: '/register/success', element: <Navigate to="/onboarding/profile" replace /> },
  { path: '/403', element: <ErrorPage code="403" title="Không có quyền" description="Bạn không có quyền truy cập khu vực này." /> },
  { path: '/500', element: <ErrorPage code="500" title="Lỗi hệ thống" description="Đã có lỗi bất thường trong quá trình xử lý." /> },
  { path: '*', element: <ErrorPage code="404" title="Không tìm thấy" description="Trang hoặc tài nguyên không tồn tại." /> },
]);
