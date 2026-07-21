import { useCallback, useRef, useState } from 'react';
import { useAuth } from './useAuth.js';
import { useRegistration } from './useRegistration.js';
import { SOCIAL_CONFLICT_ACTIONS, socialConflictService } from '../services/socialConflictService.js';
import { getSocialConflictErrorMessage, TERMINAL_SOCIAL_CONFLICT_CODES } from '../utils/socialConflictErrorMapper.js';

export function useSocialConflict() {
  const auth = useAuth();
  const registration = useRegistration();
  const operationLock = useRef(false);
  const [restored] = useState(() => socialConflictService.restoreConflict());
  const [conflict, setConflict] = useState(restored.conflict);
  const [restoreReason] = useState(restored.reason);
  const [isResolving, setIsResolving] = useState(false);
  const [isOutcomeUnknown, setIsOutcomeUnknown] = useState(false);
  const [error, setError] = useState('');

  const clearConflict = useCallback(() => {
    socialConflictService.clearConflict();
    setConflict(null);
  }, []);

  const resolveAction = useCallback(async (action, signal) => {
    if (!conflict || operationLock.current || isOutcomeUnknown || !socialConflictService.isActionAllowed(conflict, action)) return null;

    // Hai action của active-account conflict chỉ điều hướng/hướng dẫn, tuyệt đối không gọi resolve API production.
    if (action === SOCIAL_CONFLICT_ACTIONS.LOGIN_EXISTING_ACCOUNT) {
      clearConflict();
      return { type: 'LOGIN_EXISTING_ACCOUNT' };
    }
    if (action === SOCIAL_CONFLICT_ACTIONS.START_ACCOUNT_RECOVERY) {
      setError('Khôi phục tài khoản chưa được hỗ trợ. Vui lòng đăng nhập bằng phương thức hiện có.');
      return { type: 'RECOVERY_UNAVAILABLE' };
    }

    if (Number(conflict.expiresAt) <= Date.now()) {
      clearConflict();
      setError('Phiên xử lý đã hết hạn. Vui lòng bắt đầu lại đăng nhập social.');
      return { type: 'EXPIRED' };
    }

    operationLock.current = true;
    setIsResolving(true);
    setError('');
    try {
      const response = await socialConflictService.resolveConflict(conflict, action, signal);
      if (action === SOCIAL_CONFLICT_ACTIONS.CONTINUE_OTP) {
        if (response?.resolved !== true || response?.nextStep !== 'VERIFY_OTP') throw new Error('Backend không trả trạng thái tiếp tục OTP hợp lệ.');
        clearConflict();
        return { type: 'CONTINUE_OTP' };
      }

      if (action === SOCIAL_CONFLICT_ACTIONS.CANCEL_PENDING_AND_CONTINUE_SOCIAL) {
        try {
          const session = auth.setAuthenticatedSession(response);
          // Registration Flow chỉ bị xóa sau khi session Backend đã được thiết lập đầy đủ.
          registration.clearFlow();
          clearConflict();
          return { type: 'AUTH_SUCCESS', session };
        } catch {
          auth.clearSession('UNAUTHENTICATED');
          setError('Không thể thiết lập phiên đăng nhập an toàn. Vui lòng bắt đầu lại.');
          return { type: 'SESSION_SETUP_FAILED' };
        }
      }
      return null;
    } catch (requestError) {
      if (requestError?.code === 'REQUEST_TIMEOUT') {
        // Timeout có trạng thái Backend không xác định: khóa retry để tránh gửi action một lần lần thứ hai.
        setIsOutcomeUnknown(true);
      }
      if (TERMINAL_SOCIAL_CONFLICT_CODES.has(requestError?.code)) clearConflict();
      if (requestError?.code === 'USER_BLOCKED') auth.clearSession('BLOCKED', requestError);
      setError(getSocialConflictErrorMessage(requestError));
      return { type: requestError?.code === 'REQUEST_TIMEOUT' ? 'OUTCOME_UNKNOWN' : 'ERROR' };
    } finally {
      operationLock.current = false;
      setIsResolving(false);
    }
  }, [auth, clearConflict, conflict, isOutcomeUnknown, registration]);

  const beginAgain = useCallback(() => {
    clearConflict();
    return conflict?.pendingIdentifierType ? 'REGISTER' : 'LOGIN';
  }, [clearConflict, conflict]);

  return {
    conflict,
    restoreReason,
    isResolving,
    isOutcomeUnknown,
    error,
    resolveAction,
    beginAgain,
    clearError: () => setError(''),
  };
}
