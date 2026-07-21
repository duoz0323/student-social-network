/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useMemo, useRef, useState } from 'react';
import { useAuth } from '../features/auth/hooks/useAuth.js';
import { registrationService } from '../features/auth/services/registrationService.js';
import { isTerminalRegistrationError } from '../features/auth/utils/registrationErrorMapper.js';

export const RegistrationContext = createContext(null);

function createState(flow = null) {
  return {
    registrationFlowToken: flow?.registrationFlowToken ?? null,
    maskedIdentifier: flow?.maskedIdentifier ?? '',
    expiresAt: flow?.expiresAt ?? null,
    otpExpiresAt: flow?.otpExpiresAt ?? null,
    resendAvailableAt: flow?.resendAvailableAt ?? null,
    remainingAttempts: flow?.remainingAttempts ?? null,
    canResend: flow?.canResend ?? false,
    resumed: flow?.resumed === true,
    status: flow ? flow.status : 'IDLE',
    nextStep: flow?.nextStep ?? null,
    isSubmitting: false,
    isVerifying: false,
    isResending: false,
    isRestoring: false,
    isCancelling: false,
    error: null,
  };
}

export function RegistrationProvider({ children }) {
  const auth = useAuth();
  const operationLock = useRef(null);
  const [state, setState] = useState(() => createState(registrationService.getStoredFlow()));

  const clearFlow = useCallback(() => {
    registrationService.clearFlow();
    setState(createState());
  }, []);

  const startRegistration = useCallback(async (payload, signal) => {
    if (operationLock.current) return null;
    operationLock.current = 'START';
    setState((current) => ({ ...current, isSubmitting: true, status: 'STARTING', error: null }));
    try {
      const flow = await registrationService.startRegistration(payload, signal);
      setState(createState(flow));
      return flow;
    } catch (error) {
      setState((current) => ({ ...current, isSubmitting: false, status: 'ERROR', error }));
      throw error;
    } finally {
      operationLock.current = null;
    }
  }, []);

  const restoreFlow = useCallback(async (signal) => {
    if (operationLock.current) return null;
    operationLock.current = 'RESTORE';
    setState((current) => ({ ...current, isRestoring: true, error: null }));
    try {
      const flow = await registrationService.getRegistrationStatus(signal);
      if (!flow) {
        clearFlow();
        return null;
      }
      if (flow.nextStep !== 'VERIFY_OTP') {
        clearFlow();
        return { ...flow, terminal: true };
      }
      setState(createState(flow));
      return flow;
    } catch (error) {
      if (isTerminalRegistrationError(error)) clearFlow();
      else setState((current) => ({ ...current, isRestoring: false, error }));
      throw error;
    } finally {
      operationLock.current = null;
    }
  }, [clearFlow]);

  const verifyOtp = useCallback(async (code, signal) => {
    if (operationLock.current) return null;
    operationLock.current = 'VERIFY';
    setState((current) => ({ ...current, isVerifying: true, status: 'VERIFYING', error: null }));
    try {
      const response = await registrationService.verifyOtp(code, {}, signal);
      if (!response) throw new Error('Không tìm thấy phiên đăng ký để xác minh.');
      const session = auth.setAuthenticatedSession(response);
      registrationService.clearFlow();
      setState(createState({ status: 'COMPLETED' }));
      return session;
    } catch (error) {
      if (isTerminalRegistrationError(error)) {
        clearFlow();
        throw error;
      }
      const remainingAttempts = error.details?.remainingAttempts;
      const locked = error.code === 'AUTH_OTP_ATTEMPTS_EXCEEDED' || error.code === 'OTP_ATTEMPTS_EXCEEDED';
      setState((current) => ({ ...current, isVerifying: false, status: locked ? 'LOCKED' : 'OTP_REQUIRED', remainingAttempts: remainingAttempts ?? current.remainingAttempts, error }));
      throw error;
    } finally {
      operationLock.current = null;
    }
  }, [auth, clearFlow]);

  const resendOtp = useCallback(async (signal) => {
    if (operationLock.current) return null;
    operationLock.current = 'RESEND';
    setState((current) => ({ ...current, isResending: true, status: 'RESENDING', error: null }));
    try {
      const flow = await registrationService.resendOtp(signal);
      if (!flow) throw new Error('Không tìm thấy phiên đăng ký để gửi lại OTP.');
      setState(createState(flow));
      return flow;
    } catch (error) {
      setState((current) => ({ ...current, isResending: false, status: 'OTP_REQUIRED', error }));
      throw error;
    } finally {
      operationLock.current = null;
    }
  }, []);

  const cancelRegistration = useCallback(async (signal) => {
    if (operationLock.current) return false;
    operationLock.current = 'CANCEL';
    setState((current) => ({ ...current, isCancelling: true, error: null }));
    try {
      await registrationService.cancelRegistration(signal);
      clearFlow();
      return true;
    } catch (error) {
      if (isTerminalRegistrationError(error)) clearFlow();
      else setState((current) => ({ ...current, isCancelling: false, error }));
      throw error;
    } finally {
      operationLock.current = null;
    }
  }, [clearFlow]);

  const value = useMemo(() => ({ ...state, hasFlow: Boolean(state.registrationFlowToken), startRegistration, restoreFlow, verifyOtp, resendOtp, cancelRegistration, clearFlow }), [state, startRegistration, restoreFlow, verifyOtp, resendOtp, cancelRegistration, clearFlow]);
  return <RegistrationContext.Provider value={value}>{children}</RegistrationContext.Provider>;
}
