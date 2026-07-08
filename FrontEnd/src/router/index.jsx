/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { useApp } from '../contexts/AppContext.jsx';
import AdminShell from '../components/layout/AdminShell.jsx';
import UserShell from '../components/layout/UserShell.jsx';
import LoginPage from '../features/auth/pages/LoginPage.jsx';
import RegisterPage from '../features/auth/pages/RegisterPage.jsx';
import OnboardingProfilePage from '../features/auth/pages/OnboardingProfilePage.jsx';
import OnboardingSuccessPage from '../features/auth/pages/OnboardingSuccessPage.jsx';
import FeedPage from '../features/feed/pages/FeedPage.jsx';
import PostDetailPage from '../features/post/pages/PostDetailPage.jsx';
import ProfilePage from '../features/profile/pages/ProfilePage.jsx';
import SavedPostsPage from '../features/post/pages/SavedPostsPage.jsx';
import SearchPage from '../features/search/pages/SearchPage.jsx';
import AdminDashboardPage from '../features/admin/pages/AdminDashboardPage.jsx';
import AdminUsersPage from '../features/admin/pages/AdminUsersPage.jsx';
import AdminPostsPage from '../features/admin/pages/AdminPostsPage.jsx';
import AdminReportsPage from '../features/admin/pages/AdminReportsPage.jsx';
import AdminReportDetailPage from '../features/admin/pages/AdminReportDetailPage.jsx';
import ErrorPage from '../features/system/pages/ErrorPage.jsx';

function getHomePath(currentUser) {
  if (!currentUser) return '/login';
  if (!currentUser.profileCompletedAt) return '/onboarding/profile';
  return currentUser.role === 'ADMIN' ? '/admin' : '/feed/for-you';
}

function RootRedirect() {
  const { currentUser } = useApp();
  return <Navigate to={getHomePath(currentUser)} replace />;
}

function GuestRoute({ children }) {
  const { currentUser } = useApp();
  return currentUser ? <Navigate to={getHomePath(currentUser)} replace /> : children;
}

function OnboardingRoute({ children, requireCompleted = false, allowCompleted = false }) {
  const { currentUser } = useApp();
  if (!currentUser) return <Navigate to="/login" replace />;
  if (currentUser.status !== 'ACTIVE') return <Navigate to="/login" replace />;

  const completed = Boolean(currentUser.profileCompletedAt);
  // requireCompleted: trang chỉ cho phép khi đã hoàn tất hồ sơ
  if (requireCompleted && !completed) return <Navigate to="/onboarding/profile" replace />;
  // Nếu không có allowCompleted: redirect sang feed khi đã hoàn tất để tránh quay lại trang onboarding
  if (!requireCompleted && !allowCompleted && completed) return <Navigate to={getHomePath(currentUser)} replace />;
  return children;
}

function ProtectedRoute({ children }) {
  const { currentUser } = useApp();
  if (!currentUser) return <Navigate to="/login" replace />;
  if (currentUser.status !== 'ACTIVE') return <Navigate to="/login" replace />;
  return currentUser.profileCompletedAt ? children : <Navigate to="/onboarding/profile" replace />;
}

function AdminRoute({ children }) {
  const { currentUser } = useApp();
  if (!currentUser) return <Navigate to="/login" replace />;
  if (currentUser.status !== 'ACTIVE') return <Navigate to="/login" replace />;
  if (!currentUser.profileCompletedAt) return <Navigate to="/onboarding/profile" replace />;
  return currentUser.role === 'ADMIN' ? children : <Navigate to="/403" replace />;
}

export const router = createBrowserRouter([
  { path: '/', element: <RootRedirect /> },
  {
    path: '/login',
    element: (
      <GuestRoute>
        <LoginPage />
      </GuestRoute>
    ),
  },
  {
    path: '/register',
    element: (
      <GuestRoute>
        <RegisterPage />
      </GuestRoute>
    ),
  },
  {
    path: '/onboarding/profile',
    element: (
      <OnboardingRoute>
        <OnboardingProfilePage />
      </OnboardingRoute>
    ),
  },
  {
    path: '/onboarding/success',
    element: (
      // allowCompleted: cho phép truy cập sau khi hoàn tất hồ sơ — không redirect sang feed
      <OnboardingRoute allowCompleted>
        <OnboardingSuccessPage />
      </OnboardingRoute>
    ),
  },
  {
    element: (
      <ProtectedRoute>
        <UserShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/feed/:type', element: <FeedPage /> },
      { path: '/posts/:postId', element: <PostDetailPage /> },
      { path: '/profile/me', element: <ProfilePage self /> },
      { path: '/profile/:userId', element: <ProfilePage /> },
      { path: '/saved', element: <SavedPostsPage /> },
      { path: '/search', element: <SearchPage /> },
    ],
  },
  {
    path: '/admin',
    element: (
      <AdminRoute>
        <AdminShell />
      </AdminRoute>
    ),
    children: [
      { index: true, element: <AdminDashboardPage /> },
      { path: 'users', element: <AdminUsersPage /> },
      { path: 'posts', element: <AdminPostsPage /> },
      { path: 'reports', element: <AdminReportsPage /> },
      { path: 'reports/:reportId', element: <AdminReportDetailPage /> },
    ],
  },
  { path: '/403', element: <ErrorPage code="403" title="Không có quyền" description="Bạn không có quyền truy cập khu vực này." /> },
  { path: '/500', element: <ErrorPage code="500" title="Lỗi hệ thống" description="Đã có lỗi bất thường trong quá trình xử lý." /> },
  { path: '*', element: <ErrorPage code="404" title="Không tìm thấy" description="Trang hoặc tài nguyên không tồn tại." /> },
]);
