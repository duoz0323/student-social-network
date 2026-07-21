import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth } from './useAuth.js';
import { getLoginErrorMessage } from '../utils/loginErrorMapper.js';
import { validateLogin } from '../validation/loginValidation.js';

export function useLogin() {
  const auth = useAuth();
  const submitLock = useRef(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [retryAvailableAt, setRetryAvailableAt] = useState(null);
  const [retrySeconds, setRetrySeconds] = useState(0);

  useEffect(() => {
    if (!retryAvailableAt) return undefined;
    function update() {
      const remaining = Math.max(0, Math.ceil((retryAvailableAt - Date.now()) / 1000));
      setRetrySeconds(remaining);
      if (remaining === 0) setRetryAvailableAt(null);
    }
    update();
    const intervalId = window.setInterval(update, 1000);
    return () => window.clearInterval(intervalId);
  }, [retryAvailableAt]);

  const clearError = useCallback(() => {
    setFieldErrors({});
    setGeneralError('');
  }, []);

  const login = useCallback(async (form, signal) => {
    if (submitLock.current || retrySeconds > 0) return null;
    const validationErrors = validateLogin(form);
    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      setGeneralError('');
      return null;
    }

    submitLock.current = true;
    setIsSubmitting(true);
    clearError();
    try {
      return await auth.login({ email: form.email.trim(), password: form.password }, signal);
    } catch (error) {
      setFieldErrors(error.fieldErrors ?? {});
      setGeneralError(getLoginErrorMessage(error));
      if (error.code === 'AUTH_RATE_LIMITED' && error.retryAfterSeconds) {
        setRetryAvailableAt(Date.now() + error.retryAfterSeconds * 1000);
      }
      throw error;
    } finally {
      submitLock.current = false;
      setIsSubmitting(false);
    }
  }, [auth, clearError, retrySeconds]);

  return { login, isSubmitting, fieldErrors, generalError, clearError, retrySeconds };
}
