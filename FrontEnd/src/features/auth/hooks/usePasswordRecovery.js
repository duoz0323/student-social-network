import { createContext, createElement, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { passwordRecoveryService } from '../services/passwordRecoveryService.js';
import { PASSWORD_RECOVERY_STEP, TERMINAL_RECOVERY_CODES, TERMINAL_RESET_CODES } from '../constants/passwordRecoveryConstants.js';
import { getPasswordRecoveryErrorMessage } from '../utils/passwordRecoveryErrorMapper.js';

const PasswordRecoveryContext = createContext(null);
const INITIAL_STATE = Object.freeze({ step: PASSWORD_RECOVERY_STEP.IDENTIFIER, challenge: null, resetAuthorization: null });

export function PasswordRecoveryProvider({ children }) {
  const [flow, setFlow] = useState(INITIAL_STATE);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const lock = useRef(false);
  const requestController = useRef(null);

  const clear = useCallback(() => {
    requestController.current?.abort();
    requestController.current = null;
    lock.current = false;
    setFlow(INITIAL_STATE);
    setError('');
    setFieldErrors({});
    setIsSubmitting(false);
  }, []);

  useEffect(() => () => {
    // Provider bị tháo khi rời hai route recovery: hủy request thường và xóa token khỏi memory.
    requestController.current?.abort();
  }, []);

  const run = useCallback(async (operation, { terminalCodes = TERMINAL_RECOVERY_CODES, abortable = true } = {}) => {
    if (lock.current) return null;
    lock.current = true;
    setIsSubmitting(true);
    setError('');
    setFieldErrors({});
    const controller = abortable ? new AbortController() : null;
    requestController.current = controller;
    try {
      return await operation(controller?.signal);
    } catch (requestError) {
      setFieldErrors(requestError?.fieldErrors ?? {});
      setError(getPasswordRecoveryErrorMessage(requestError));
      if (terminalCodes.has(requestError?.code)) setFlow(INITIAL_STATE);
      throw requestError;
    } finally {
      requestController.current = null;
      lock.current = false;
      setIsSubmitting(false);
    }
  }, []);

  const start = useCallback((identifier) => run(async (signal) => {
    const challenge = await passwordRecoveryService.start(identifier, signal);
    setFlow({ step: PASSWORD_RECOVERY_STEP.OTP, challenge, resetAuthorization: null });
    return challenge;
  }), [run]);

  const verify = useCallback((code) => run(async (signal) => {
    const authorization = await passwordRecoveryService.verify(code, flow.challenge?.recoveryFlowToken, signal);
    setFlow({ step: PASSWORD_RECOVERY_STEP.RESET, challenge: null, resetAuthorization: authorization });
    return authorization;
  }), [flow.challenge?.recoveryFlowToken, run]);

  const resend = useCallback(() => run(async (signal) => {
    const challenge = await passwordRecoveryService.resend(flow.challenge?.recoveryFlowToken, signal);
    setFlow({ step: PASSWORD_RECOVERY_STEP.OTP, challenge, resetAuthorization: null });
    return challenge;
  }), [flow.challenge?.recoveryFlowToken, run]);

  const complete = useCallback((payload) => run(async () => {
    const response = await passwordRecoveryService.complete(payload, flow.resetAuthorization?.resetAuthorizedToken);
    if (response?.completed !== true) throw new Error('INVALID_PASSWORD_RESET_RESPONSE');
    setFlow(INITIAL_STATE);
    return response;
  }, { terminalCodes: TERMINAL_RESET_CODES, abortable: false }).catch((requestError) => {
    if (requestError?.code === 'REQUEST_TIMEOUT' || requestError?.code === 'NETWORK_ERROR') {
      // Kết quả complete không xác định: xóa token và tuyệt đối không replay request.
      setFlow(INITIAL_STATE);
    }
    throw requestError;
  }), [flow.resetAuthorization?.resetAuthorizedToken, run]);

  const clearError = useCallback(() => { setError(''); setFieldErrors({}); }, []);
  const value = useMemo(() => ({ flow, isSubmitting, error, fieldErrors, start, verify, resend, complete, clear, clearError }),
    [flow, isSubmitting, error, fieldErrors, start, verify, resend, complete, clear, clearError]);
  // Context cần nhận các handler đóng trên ref khóa submit; ref chỉ được đọc khi handler chạy, không đọc trong render.
  // eslint-disable-next-line react-hooks/refs
  return createElement(PasswordRecoveryContext.Provider, { value }, children);
}

export function usePasswordRecovery() {
  const context = useContext(PasswordRecoveryContext);
  if (!context) throw new Error('usePasswordRecovery phải được dùng trong PasswordRecoveryProvider.');
  return context;
}
