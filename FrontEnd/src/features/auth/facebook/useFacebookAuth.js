import { useCallback, useEffect, useRef, useState } from 'react';
import { authConfig } from '../../../config/authConfig.js';
import { useAuth } from '../hooks/useAuth.js';
import { useRegistration } from '../hooks/useRegistration.js';
import { socialConflictService } from '../services/socialConflictService.js';
import { getFacebookErrorMessage } from '../utils/facebookErrorMapper.js';
import { facebookAuthService } from './facebookAuthService.js';
import { loadFacebookSdk, requestFacebookCredential } from './facebookSdkAdapter.js';

const CONFLICT_CODES = new Set(['AUTH_SOCIAL_ACCOUNT_CONFLICT', 'AUTH_SOCIAL_PENDING_CONFLICT']);

export function useFacebookAuth({ includeRegistrationFlow = false, onAuthenticated, onConflict } = {}) {
  const auth = useAuth();
  const registration = useRegistration();
  const mountedRef = useRef(false);
  const operationLock = useRef(false);
  const requestControllerRef = useRef(null);
  const callbackRef = useRef({ onAuthenticated, onConflict });
  const registrationRef = useRef(registration);
  const [facebookSdk, setFacebookSdk] = useState(null);
  const [isSdkLoading, setIsSdkLoading] = useState(Boolean(authConfig.facebookAppId));
  const [isAuthenticating, setIsAuthenticating] = useState(false);
  const [error, setError] = useState(authConfig.facebookAppId ? '' : 'Đăng nhập Facebook chưa được cấu hình.');

  useEffect(() => {
    if (!error || !authConfig.facebookAppId) return undefined;
    const timeoutId = window.setTimeout(() => setError(''), 8_000);
    return () => window.clearTimeout(timeoutId);
  }, [error]);

  useEffect(() => { callbackRef.current = { onAuthenticated, onConflict }; }, [onAuthenticated, onConflict]);
  useEffect(() => { registrationRef.current = registration; }, [registration]);

  const configureSdk = useCallback(async () => {
    if (!authConfig.facebookAppId || operationLock.current) return;
    setIsSdkLoading(true);
    setError('');
    try {
      const sdk = await loadFacebookSdk(authConfig.facebookAppId);
      if (mountedRef.current) setFacebookSdk(sdk);
    } catch (sdkError) {
      if (mountedRef.current) setError(getFacebookErrorMessage(sdkError));
    } finally {
      if (mountedRef.current) setIsSdkLoading(false);
    }
  }, []);

  const startFacebookSignIn = useCallback(async () => {
    if (!facebookSdk || operationLock.current) return;
    operationLock.current = true;
    if (mountedRef.current) { setIsAuthenticating(true); setError(''); }
    try {
      const providerCredential = await requestFacebookCredential(facebookSdk);
      if (!mountedRef.current) return;
      const currentFlow = registrationRef.current;
      const flowIsUsable = includeRegistrationFlow && currentFlow.registrationFlowToken
        && (!currentFlow.expiresAt || new Date(currentFlow.expiresAt).getTime() > Date.now());
      const requestController = new AbortController();
      requestControllerRef.current = requestController;
      const response = await facebookAuthService.authenticate(
        providerCredential,
        flowIsUsable ? currentFlow.registrationFlowToken : null,
        requestController.signal,
      );
      if (!mountedRef.current) return;
      const session = auth.setAuthenticatedSession(response);
      if (flowIsUsable) currentFlow.clearFlow();
      callbackRef.current.onAuthenticated?.(session);
    } catch (requestError) {
      if (!mountedRef.current) return;
      const conflict = CONFLICT_CODES.has(requestError.code)
        ? socialConflictService.setConflict(requestError.details, {
          provider: 'FACEBOOK',
          maskedPendingIdentifier: registrationRef.current.maskedIdentifier,
        })
        : null;
      if (conflict) callbackRef.current.onConflict?.(requestError);
      else {
        if (requestError.code === 'USER_BLOCKED') auth.clearSession('BLOCKED', requestError);
        setError(getFacebookErrorMessage(requestError));
      }
    } finally {
      // Không giữ hoặc replay Facebook credential sau khi request kết thúc.
      operationLock.current = false;
      requestControllerRef.current = null;
      if (mountedRef.current) setIsAuthenticating(false);
    }
  }, [auth, facebookSdk, includeRegistrationFlow]);

  useEffect(() => {
    mountedRef.current = true;
    // Đưa cập nhật state ra khỏi thân effect; mounted guard loại bỏ callback đến sau unmount.
    const initialization = Promise.resolve().then(configureSdk);
    void initialization;
    return () => {
      mountedRef.current = false;
      requestControllerRef.current?.abort();
      requestControllerRef.current = null;
    };
  }, [configureSdk]);

  return {
    isConfigured: Boolean(authConfig.facebookAppId),
    isSdkLoading,
    isReady: Boolean(facebookSdk),
    isAuthenticating,
    error,
    retrySdk: configureSdk,
    startFacebookSignIn,
  };
}
