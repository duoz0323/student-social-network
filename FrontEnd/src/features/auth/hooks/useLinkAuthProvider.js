import { useCallback, useEffect, useRef, useState } from 'react';
import { authConfig } from '../../../config/authConfig.js';
import { loadFacebookSdk, requestFacebookCredential } from '../facebook/facebookSdkAdapter.js';
import { requestGoogleCredential } from '../google/googleSdkLoader.js';
import { authProviderService } from '../services/authProviderService.js';
import { getAuthProviderErrorMessage, isAmbiguousProviderError, isTerminalLinkError } from '../utils/authProviderErrorMapper.js';

async function acquireCredential(provider) {
  if (provider === 'GOOGLE') {
    if (!authConfig.googleClientId) throw new Error('GOOGLE_NOT_CONFIGURED');
    return requestGoogleCredential();
  }
  if (!authConfig.facebookAppId) throw new Error('FACEBOOK_NOT_CONFIGURED');
  return requestFacebookCredential(await loadFacebookSdk(authConfig.facebookAppId));
}

function friendlyClientError(error) {
  const code = error?.code ?? error?.message;
  if (code === 'GOOGLE_NOT_CONFIGURED') return 'Google chưa được cấu hình.';
  if (code === 'FACEBOOK_NOT_CONFIGURED') return 'Facebook chưa được cấu hình.';
  if (code === 'GOOGLE_POPUP_CLOSED' || code === 'FACEBOOK_POPUP_CLOSED') return '';
  if (code === 'FACEBOOK_PERMISSION_DENIED') return 'Bạn chưa cấp quyền Facebook.';
  return getAuthProviderErrorMessage(error);
}

export function useLinkAuthProvider({ onUpdated }) {
  const mountedRef = useRef(false);
  const operationLock = useRef(false);
  const controllerRef = useRef(null);
  const callbackRef = useRef(onUpdated);
  const [linkFlow, setLinkFlow] = useState(null);
  const [state, setState] = useState({ isSubmitting: false, error: '', success: '', ambiguousTarget: null });

  useEffect(() => { callbackRef.current = onUpdated; }, [onUpdated]);
  useEffect(() => {
    mountedRef.current = true;
    return () => { mountedRef.current = false; controllerRef.current?.abort(); };
  }, []);

  const begin = useCallback(() => {
    if (operationLock.current) return null;
    operationLock.current = true;
    const controller = new AbortController();
    controllerRef.current = controller;
    if (mountedRef.current) setState((current) => ({ ...current, isSubmitting: true, error: '', success: '' }));
    return controller;
  }, []);

  const finish = useCallback(() => {
    operationLock.current = false;
    controllerRef.current = null;
    if (mountedRef.current) setState((current) => ({ ...current, isSubmitting: false }));
  }, []);

  const safeRefetch = useCallback(async () => {
    try { return await callbackRef.current?.(); } catch { return null; }
  }, []);

  const startEmailLink = useCallback(async (email) => {
    const controller = begin();
    if (!controller) return null;
    try {
      const response = await authProviderService.startEmailLink(email.trim(), controller.signal);
      if (!response?.flowToken || response.flowType !== 'LINK_EMAIL') throw new Error('INVALID_LINK_RESPONSE');
      const flow = { type: 'EMAIL', linkFlowToken: response.flowToken, maskedIdentifier: response.maskedIdentifier, otpExpiresAt: response.otpExpiresAt, resendAvailableAt: response.resendAvailableAt, challengeExpiresAt: response.challengeExpiresAt };
      if (mountedRef.current) setLinkFlow(flow);
      return flow;
    } catch (error) {
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish]);

  const completeEmailLink = useCallback(async (payload) => {
    if (!linkFlow) return null;
    const controller = begin();
    if (!controller) return null;
    try {
      const method = await authProviderService.completeEmailLink(payload, linkFlow.linkFlowToken, controller.signal);
      if (mountedRef.current) { setLinkFlow(null); setState((current) => ({ ...current, success: 'Email đã sẵn sàng đăng nhập.' })); }
      await callbackRef.current?.();
      return method;
    } catch (error) {
      if (isTerminalLinkError(error) && mountedRef.current) setLinkFlow(null);
      if (isAmbiguousProviderError(error)) await safeRefetch();
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish, linkFlow, safeRefetch]);

  const verifyEmailOtp = useCallback(async (code) => {
    if (!linkFlow) return null;
    const controller = begin();
    if (!controller) return null;
    try {
      const response = await authProviderService.verifyEmailLink(code, linkFlow.linkFlowToken, controller.signal);
      if (!response?.flowToken) throw new Error('INVALID_LINK_RESPONSE');
      const verifiedFlow = { ...linkFlow, linkFlowToken: response.flowToken, challengeExpiresAt: response.expiresAt, otpVerified: true };
      if (mountedRef.current) setLinkFlow(verifiedFlow);
      return verifiedFlow;
    } catch (error) {
      if (isTerminalLinkError(error) && mountedRef.current) setLinkFlow(null);
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish, linkFlow]);

  const setPassword = useCallback(async (proofMethod, payload) => {
    const controller = begin();
    if (!controller) return null;
    try {
      const credential = await acquireCredential(proofMethod);
      if (!mountedRef.current) return null;
      const result = await authProviderService.setPassword(proofMethod, credential, payload, controller.signal);
      if (mountedRef.current) setState((current) => ({ ...current, success: 'Đã thiết lập mật khẩu.' }));
      return result;
    } catch (error) {
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish]);

  const changePassword = useCallback(async (payload) => {
    const controller = begin();
    if (!controller) return null;
    try {
      const result = await authProviderService.changePassword(payload, controller.signal);
      if (mountedRef.current) setState((current) => ({ ...current, success: 'Đã thay đổi mật khẩu.' }));
      return result;
    } catch (error) {
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish]);

  const resendOtp = useCallback(async () => {
    if (!linkFlow) return null;
    const controller = begin();
    if (!controller) return null;
    try {
      const response = await authProviderService.resendEmailLink(linkFlow.linkFlowToken, controller.signal);
      if (!response?.flowToken) throw new Error('INVALID_LINK_RESPONSE');
      const rotated = { ...linkFlow, linkFlowToken: response.flowToken, maskedIdentifier: response.maskedIdentifier, otpExpiresAt: response.otpExpiresAt, resendAvailableAt: response.resendAvailableAt, challengeExpiresAt: response.challengeExpiresAt };
      if (mountedRef.current) setLinkFlow(rotated);
      return rotated;
    } catch (error) {
      if (isTerminalLinkError(error) && mountedRef.current) setLinkFlow(null);
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish, linkFlow]);

  const linkSocial = useCallback(async (provider) => {
    const controller = begin();
    if (!controller) return null;
    try {
      const credential = await acquireCredential(provider);
      if (!mountedRef.current) return null;
      const method = await authProviderService.linkSocial(provider, credential, controller.signal);
      if (mountedRef.current) setState((current) => ({ ...current, success: `Đã liên kết ${provider === 'GOOGLE' ? 'Google' : 'Facebook'}.` }));
      await callbackRef.current?.();
      return method;
    } catch (error) {
      if (isAmbiguousProviderError(error)) await safeRefetch();
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish, safeRefetch]);

  const unlinkWithProof = useCallback(async (targetMethod, proofMethod, password = '') => {
    const controller = begin();
    if (!controller) return false;
    try {
      const proof = proofMethod === 'PASSWORD' ? password : await acquireCredential(proofMethod);
      if (!mountedRef.current) return false;
      const challenge = await authProviderService.reauthenticate(proofMethod, targetMethod, proof, controller.signal);
      const reauthenticationToken = challenge?.reauthenticationToken;
      if (!reauthenticationToken || challenge.purpose !== 'UNLINK_AUTH_METHOD' || challenge.targetMethod !== targetMethod || challenge.status !== 'ACTIVE') throw new Error('INVALID_REAUTHENTICATION_RESPONSE');
      if (!mountedRef.current) return false;
      await authProviderService.unlink(targetMethod, reauthenticationToken, controller.signal);
      await callbackRef.current?.();
      if (mountedRef.current) setState((current) => ({ ...current, success: 'Đã gỡ phương thức đăng nhập.', ambiguousTarget: null }));
      return true;
    } catch (error) {
      if (isAmbiguousProviderError(error)) {
        if (mountedRef.current) setState((current) => ({ ...current, ambiguousTarget: targetMethod }));
        await safeRefetch();
        if (mountedRef.current) setState((current) => ({ ...current, ambiguousTarget: null }));
      }
      if (mountedRef.current) setState((current) => ({ ...current, error: friendlyClientError(error) }));
      throw error;
    } finally { finish(); }
  }, [begin, finish, safeRefetch]);

  const clearLinkFlow = useCallback(() => setLinkFlow(null), []);
  const clearMessages = useCallback(() => setState((current) => ({ ...current, error: '', success: '' })), []);

  return { ...state, linkFlow, startEmailLink, verifyEmailOtp, completeEmailLink, resendOtp, linkSocial,
    unlinkWithProof, setPassword, changePassword, clearLinkFlow, clearMessages };
}
