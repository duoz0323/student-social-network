/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { useApp } from '../contexts/AppContext.jsx';
import AdminShell from '../components/layout/AdminShell.jsx';
import UserShell from '../components/layout/UserShell.jsx';
import LoginPage from '../features/auth/pages/LoginPage.jsx';
import RegisterPage from '../features/auth/pages/RegisterPage.jsx';
import RegisterSuccessPage from '../features/auth/pages/RegisterSuccessPage.jsx';
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

function ProtectedRoute({ children }) {
  const { currentUser } = useApp();
  return currentUser ? children : <Navigate to="/login" replace />;
}

function AdminRoute({ children }) {
  const { currentUser } = useApp();
  if (!currentUser) return <Navigate to="/login" replace />;
  return currentUser.role === 'ADMIN' ? children : <Navigate to="/403" replace />;
}

export const router = createBrowserRouter([
  { path: '/', element: <Navigate to="/feed/for-you" replace /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/register/success', element: <RegisterSuccessPage /> },
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
  { path: '/403', element: <ErrorPage code="403" title="Khong co quyen" description="Ban khong co quyen truy cap khu vuc nay." /> },
  { path: '/500', element: <ErrorPage code="500" title="Loi he thong" description="Da co loi bat thuong trong qua trinh xu ly." /> },
  { path: '*', element: <ErrorPage code="404" title="Khong tim thay" description="Trang hoac tai nguyen khong ton tai." /> },
]);
