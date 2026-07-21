/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { configureHttpAuthHandlers } from '../api/httpClient.js';
import { tokenManager } from '../api/tokenManager.js';
import { authService } from '../features/auth/services/authService.js';

export const AuthContext = createContext(null);

const SESSION_ERROR_CODES = new Set(['INVALID_REFRESH_TOKEN', 'REFRESH_TOKEN_EXPIRED', 'REFRESH_TOKEN_REVOKED']);

function sessionState(session, authStatus = 'AUTHENTICATED') {
  return {
    user: session.user,
    profileCompleted: Boolean(session.profileCompleted),
    role: session.user.role,
    authStatus,
    error: null,
  };
}

function emptyState(authStatus, error = null) {
  return { user: null, profileCompleted: false, role: null, authStatus, error };
}

export function AuthProvider({ children }) {
  const initializedRef = useRef(false);
  const [state, setState] = useState(() => emptyState('INITIALIZING'));

  const clearSession = useCallback((status = 'UNAUTHENTICATED', error = null) => {
    authService.clearSession();
    setState(emptyState(status, error));
  }, []);

  const refreshSession = useCallback(async () => {
    // Bootstrap phải giữ INITIALIZING để route guard không điều hướng trước khi biết kết quả refresh.
    setState((current) => current.authStatus === 'INITIALIZING'
      ? current
      : { ...current, authStatus: 'REFRESHING' });
    try {
      const session = await authService.refreshSession();
      if (!session) {
        clearSession('UNAUTHENTICATED');
        return null;
      }
      setState(sessionState(session));
      return session.accessToken;
    } catch (error) {
      if (error.code === 'USER_BLOCKED') clearSession('BLOCKED', error);
      else if (SESSION_ERROR_CODES.has(error.code) || error.status === 401) clearSession('SESSION_EXPIRED', error);
      else setState((current) => ({ ...current, authStatus: current.user ? 'AUTHENTICATED' : 'UNAUTHENTICATED', error }));
      throw error;
    }
  }, [clearSession]);

  const initializeAuth = useCallback(async () => {
    const storedSession = authService.getStoredSession();
    if (!storedSession?.user || !tokenManager.getRefreshToken()) {
      clearSession('UNAUTHENTICATED');
      return;
    }

    try {
      await refreshSession();
    } catch {
      // refreshSession đã phân loại lỗi và cập nhật state phù hợp.
    }
  }, [clearSession, refreshSession]);

  const login = useCallback(async (payload, signal) => {
    try {
      const session = await authService.loginLocal(payload, signal);
      setState(sessionState(session));
      return session;
    } catch (error) {
      if (error.code === 'USER_BLOCKED') clearSession('BLOCKED', error);
      throw error;
    }
  }, [clearSession]);

  const setAuthenticatedSession = useCallback((response) => {
    const session = authService.setAuthenticatedSession(response);
    setState(sessionState(session));
    return session;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
      return true;
    } catch {
      return false;
    } finally {
      setState(emptyState('UNAUTHENTICATED'));
    }
  }, []);

  const updateProfileCompletion = useCallback((profileCompleted) => {
    setState((current) => {
      const next = { ...current, profileCompleted: Boolean(profileCompleted) };
      if (next.user) tokenManager.setSessionSnapshot({ user: next.user, profileCompleted: next.profileCompleted });
      return next;
    });
  }, []);

  useEffect(() => {
    // httpClient chỉ nhận callback và không import ngược Auth Context nên không có circular dependency.
    configureHttpAuthHandlers({
      getAccessToken: () => tokenManager.getAccessToken(),
      refreshAccessToken: refreshSession,
      clearAuthentication: () => clearSession('SESSION_EXPIRED'),
      onProfileNotCompleted: () => updateProfileCompletion(false),
      onBlocked: () => clearSession('BLOCKED'),
    });
  }, [clearSession, refreshSession, updateProfileCompletion]);

  useEffect(() => {
    if (initializedRef.current) return;
    initializedRef.current = true;
    initializeAuth();
  }, [initializeAuth]);

  const value = useMemo(() => ({
    ...state,
    isAuthenticated: state.authStatus === 'AUTHENTICATED' || state.authStatus === 'REFRESHING',
    isInitializing: state.authStatus === 'INITIALIZING',
    isRefreshing: state.authStatus === 'REFRESHING',
    isAdmin: state.role === 'ADMIN',
    initializeAuth,
    login,
    setAuthenticatedSession,
    logout,
    refreshSession,
    clearSession,
    updateProfileCompletion,
    hasRole: (role) => state.role === role,
  }), [state, initializeAuth, login, setAuthenticatedSession, logout, refreshSession, clearSession, updateProfileCompletion]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
