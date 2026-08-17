import { Navigate, useLocation } from 'react-router-dom';
import AuthBootstrap from '../features/auth/components/AuthBootstrap.jsx';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { getAuthenticatedHome } from '../features/auth/utils/authNavigation.js';
import { getAdminProfilePath } from '../features/admin/constants/adminRbac.js';

function LoadingGuard({ children }) {
  return <AuthBootstrap>{children}</AuthBootstrap>;
}

export function RootRedirect() {
  const auth = useAuth();
  return <LoadingGuard><Navigate to={auth.isAuthenticated ? getAuthenticatedHome(auth) : '/login'} replace /></LoadingGuard>;
}

export function GuestRoute({ children }) {
  const auth = useAuth();
  return <LoadingGuard>{auth.isAuthenticated ? <Navigate to={getAuthenticatedHome(auth)} replace /> : children}</LoadingGuard>;
}

export function ProtectedRoute({ children }) {
  const auth = useAuth();
  const location = useLocation();
  if (auth.isInitializing) return <AuthBootstrap>{children}</AuthBootstrap>;
  if (!auth.isAuthenticated) {
    const reason = auth.authStatus === 'SESSION_EXPIRED' ? 'SESSION_EXPIRED' : auth.authStatus === 'BLOCKED' ? 'BLOCKED' : null;
    return <Navigate to="/login" replace state={{ from: location.pathname, reason }} />;
  }
  if (!auth.profileCompleted) return <Navigate to="/onboarding/profile" replace />;
  return children;
}

export function ProfileCompletionRoute({
  children,
  requireCompleted = false,
  allowCompletionTransition = false,
  allowCompleted = false,
}) {
  const auth = useAuth();
  const location = useLocation();
  const hasValidCompletionTransition = allowCompletionTransition
    && location.state?.onboardingJustCompleted === true;

  if (auth.isInitializing) return <AuthBootstrap>{children}</AuthBootstrap>;
  if (!auth.isAuthenticated) return <Navigate to="/login" replace />;
  if (requireCompleted && !auth.profileCompleted && !hasValidCompletionTransition) {
    return <Navigate to="/onboarding/profile" replace />;
  }
  if (!requireCompleted && auth.profileCompleted && !allowCompleted) {
    return <Navigate to={getAuthenticatedHome(auth)} replace />;
  }
  return children;
}

export function AdminRoute({ children }) {
  const auth = useAuth();
  if (auth.isInitializing) return <AuthBootstrap>{children}</AuthBootstrap>;
  if (!auth.isAuthenticated) return <Navigate to="/login" replace />;
  if (!auth.profileCompleted) return <Navigate to="/onboarding/profile" replace />;
  return auth.role === 'ADMIN' ? children : <Navigate to="/403" replace />;
}

export function AdminPermissionRoute({ children, permission, anyOf = [], adminRole }) {
  const auth = useAuth();
  const allowed = adminRole
    ? auth.hasAdminRole(adminRole)
    : permission ? auth.hasPermission(permission) : anyOf.some(auth.hasPermission);
  return allowed ? children : <Navigate to="/403" replace />;
}

export function AdminProfileEntryRoute({ children }) {
  const auth = useAuth();
  const profilePath = getAdminProfilePath(auth.adminRoles);
  return profilePath === '/admin/profile' ? children : <Navigate to={profilePath} replace />;
}
