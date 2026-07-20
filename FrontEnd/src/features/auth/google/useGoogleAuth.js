import { useCallback, useEffect, useRef, useState } from 'react';
import { authConfig } from '../../../config/authConfig.js';
import { useAuth } from '../hooks/useAuth.js';
import { useRegistration } from '../hooks/useRegistration.js';
import { googleAuthService } from './googleAuthService.js';
import { loadGoogleIdentityServices } from './googleSdkLoader.js';
import { socialConflictService } from '../services/socialConflictService.js';
import { getGoogleErrorMessage } from '../utils/googleErrorMapper.js';

const CONFLICT_CODES = new Set(['AUTH_SOCIAL_ACCOUNT_CONFLICT', 'AUTH_SOCIAL_PENDING_CONFLICT']);

export function useGoogleAuth({ includeRegistrationFlow = false, onAuthenticated, onConflict } = {}) {
  const { setAuthenticatedSession, clearSession } = useAuth();
  const { registrationFlowToken, expiresAt, clearFlow } = useRegistration();
  const mountedRef = useRef(true);
  const requestLock = useRef(false);
  const sdkLoadLock = useRef(false);
  const sdkGeneration = useRef(0);
  const callbackRef = useRef({ onAuthenticated, onConflict });
  const registrationRef = useRef({ registrationFlowToken, expiresAt });
  const [isSdkLoading, setIsSdkLoading] = useState(Boolean(authConfig.googleClientId));
  const [isGoogleReady, setIsGoogleReady] = useState(false);
  const [isAuthenticating, setIsAuthenticating] = useState(false);
  const [error, setError] = useState(authConfig.googleClientId ? '' : 'Đăng nhập Google chưa được cấu hình.');

  useEffect(() => {
    callbackRef.current = { onAuthenticated, onConflict };
  }, [onAuthenticated, onConflict]);

  useEffect(() => {
    registrationRef.current = { registrationFlowToken, expiresAt };
  }, [registrationFlowToken, expiresAt]);

  const receiveCredential = useCallback(async (googleResponse) => {
    if (!mountedRef.current) return;
    const idToken = googleResponse?.credential;
    if (!idToken || requestLock.current) return;
    requestLock.current = true;
    if (mountedRef.current) { setIsAuthenticating(true); setError(''); }

    try {
      const currentFlow = registrationRef.current;
      const flowIsUsable = includeRegistrationFlow
        && currentFlow.registrationFlowToken
        && (!currentFlow.expiresAt || new Date(currentFlow.expiresAt).getTime() > Date.now());
      const response = await googleAuthService.authenticate(idToken, flowIsUsable ? currentFlow.registrationFlowToken : null);
      const session = setAuthenticatedSession(response);
      if (flowIsUsable) clearFlow();
      callbackRef.current.onAuthenticated?.(session);
    } catch (requestError) {
      if (CONFLICT_CODES.has(requestError.code) && socialConflictService.save(requestError.details)) {
        callbackRef.current.onConflict?.(requestError);
      } else {
        if (requestError.code === 'USER_BLOCKED') clearSession('BLOCKED', requestError);
        if (mountedRef.current) setError(getGoogleErrorMessage(requestError));
      }
    } finally {
      // Credential không được gán vào state/storage và tham chiếu callback kết thúc tại đây.
      requestLock.current = false;
      if (mountedRef.current) setIsAuthenticating(false);
    }
  }, [clearFlow, clearSession, includeRegistrationFlow, setAuthenticatedSession]);

  const configureSdk = useCallback((google) => {
    if (!mountedRef.current) return;
    google.accounts.id.initialize({
      client_id: authConfig.googleClientId,
      callback: receiveCredential,
      auto_select: false,
      cancel_on_tap_outside: true,
    });
    setIsGoogleReady(true);
    setIsSdkLoading(false);
  }, [receiveCredential]);

  const handleSdkFailure = useCallback(() => {
    if (!mountedRef.current) return;
    setError('Không thể tải Google Identity Services. Vui lòng thử lại.');
    setIsGoogleReady(false);
    setIsSdkLoading(false);
  }, []);

  const retrySdk = useCallback(() => {
    if (!authConfig.googleClientId || sdkLoadLock.current) return;
    sdkLoadLock.current = true;
    const generation = ++sdkGeneration.current;
    setIsSdkLoading(true);
    setError('');
    loadGoogleIdentityServices()
      .then((google) => { if (sdkGeneration.current === generation) configureSdk(google); })
      .catch(() => { if (sdkGeneration.current === generation) handleSdkFailure(); })
      .finally(() => { sdkLoadLock.current = false; });
  }, [configureSdk, handleSdkFailure]);

  useEffect(() => {
    mountedRef.current = true;
    const generation = ++sdkGeneration.current;
    if (authConfig.googleClientId) {
      sdkLoadLock.current = true;
      loadGoogleIdentityServices()
        .then((google) => { if (sdkGeneration.current === generation) configureSdk(google); })
        .catch(() => { if (sdkGeneration.current === generation) handleSdkFailure(); })
        .finally(() => { sdkLoadLock.current = false; });
    }
    return () => {
      mountedRef.current = false;
      if (sdkGeneration.current === generation) sdkGeneration.current += 1;
    };
  }, [configureSdk, handleSdkFailure]);

  const renderButton = useCallback((container) => {
    if (!container || !isGoogleReady || !window.google?.accounts?.id) return;
    container.textContent = '';
    window.google.accounts.id.renderButton(container, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'continue_with',
      shape: 'rectangular',
      width: Math.min(326, Math.max(240, container.clientWidth || 326)),
    });
  }, [isGoogleReady]);

  return { isConfigured: Boolean(authConfig.googleClientId), isSdkLoading, isGoogleReady, isAuthenticating, error, renderButton, retrySdk };
}
