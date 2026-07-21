import { useCallback, useEffect, useRef, useState } from 'react';
import { authConfig } from '../../../config/authConfig.js';
import { useAuth } from '../hooks/useAuth.js';
import { useRegistration } from '../hooks/useRegistration.js';
import { socialConflictService } from '../services/socialConflictService.js';
import { getGoogleErrorMessage } from '../utils/googleErrorMapper.js';
import { googleAuthService } from './googleAuthService.js';
import { loadGoogleIdentityServices } from './googleSdkLoader.js';

const CONFLICT_CODES = new Set(['AUTH_SOCIAL_ACCOUNT_CONFLICT', 'AUTH_SOCIAL_PENDING_CONFLICT']);

export function useGoogleAuth({ includeRegistrationFlow = false, onAuthenticated, onConflict } = {}) {
  const auth = useAuth();
  const registration = useRegistration();
  const mountedRef = useRef(false);
  const operationLock = useRef(false);
  const requestControllerRef = useRef(null);
  const callbackRef = useRef({ onAuthenticated, onConflict });
  const registrationRef = useRef(registration);
  const [isSdkLoading, setIsSdkLoading] = useState(Boolean(authConfig.googleClientId));
  const [isGoogleReady, setIsGoogleReady] = useState(false);
  const [isAuthenticating, setIsAuthenticating] = useState(false);
  const [error, setError] = useState(authConfig.googleClientId ? '' : 'Đăng nhập Google chưa được cấu hình.');

  useEffect(() => {
    if (!error || !authConfig.googleClientId) return undefined;
    const timeoutId = window.setTimeout(() => setError(''), 8_000);
    return () => window.clearTimeout(timeoutId);
  }, [error]);

  useEffect(() => { callbackRef.current = { onAuthenticated, onConflict }; }, [onAuthenticated, onConflict]);
  useEffect(() => { registrationRef.current = registration; }, [registration]);

  const configureSdk = useCallback(async () => {
    if (!authConfig.googleClientId || operationLock.current) return;
    setIsSdkLoading(true);
    setError('');
    try {
      await loadGoogleIdentityServices();
      if (mountedRef.current) setIsGoogleReady(true);
    } catch {
      if (mountedRef.current) setError('Không thể tải Google Identity Services. Vui lòng thử lại.');
    } finally {
      if (mountedRef.current) setIsSdkLoading(false);
    }
  }, []);

  const receiveCredential = useCallback(async (googleResponse) => {
    if (!isGoogleReady || operationLock.current || !googleResponse?.credential) return;
    operationLock.current = true;
    if (mountedRef.current) { setIsAuthenticating(true); setError(''); }
    try {
      const currentFlow = registrationRef.current;
      const flowIsUsable = includeRegistrationFlow && currentFlow.registrationFlowToken
        && (!currentFlow.expiresAt || new Date(currentFlow.expiresAt).getTime() > Date.now());
      const controller = new AbortController();
      requestControllerRef.current = controller;
      const response = await googleAuthService.authenticate(googleResponse.credential, flowIsUsable ? currentFlow.registrationFlowToken : null, controller.signal);
      if (!mountedRef.current) return;
      const session = auth.setAuthenticatedSession(response);
      if (flowIsUsable) currentFlow.clearFlow();
      callbackRef.current.onAuthenticated?.(session);
    } catch (requestError) {
      if (!mountedRef.current) return;
      const conflict = CONFLICT_CODES.has(requestError.code)
        ? socialConflictService.setConflict(requestError.details, {
          provider: 'GOOGLE',
          maskedPendingIdentifier: registrationRef.current.maskedIdentifier,
        }) : null;
      if (conflict) callbackRef.current.onConflict?.(requestError);
      else {
        if (requestError.code === 'USER_BLOCKED') auth.clearSession('BLOCKED', requestError);
        setError(getGoogleErrorMessage(requestError));
      }
    } finally {
      operationLock.current = false;
      requestControllerRef.current = null;
      if (mountedRef.current) setIsAuthenticating(false);
    }
  }, [auth, includeRegistrationFlow, isGoogleReady]);

  const renderButton = useCallback((container) => {
    if (!container || !isGoogleReady || !window.google?.accounts?.id) return;
    container.replaceChildren();
    window.google.accounts.id.initialize({
      client_id: authConfig.googleClientId,
      auto_select: false,
      cancel_on_tap_outside: true,
      callback: receiveCredential,
    });
    // Nút chính thức mở hộp chọn tài khoản đầy đủ, thay cho One Tap nhỏ ở góc màn hình.
    window.google.accounts.id.renderButton(container, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      shape: 'rectangular',
      logo_alignment: 'left',
      width: Math.min(400, Math.max(240, Math.round(container.getBoundingClientRect().width || 400))),
    });
  }, [isGoogleReady, receiveCredential]);

  useEffect(() => {
    mountedRef.current = true;
    void Promise.resolve().then(configureSdk);
    return () => { mountedRef.current = false; requestControllerRef.current?.abort(); };
  }, [configureSdk]);

  return { isConfigured: Boolean(authConfig.googleClientId), isSdkLoading, isGoogleReady, isAuthenticating, error, renderButton, retrySdk: configureSdk };
}
