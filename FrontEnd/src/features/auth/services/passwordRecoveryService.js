import { authApi } from '../../../api/index.js';

function isValidTimestamp(value) {
  return typeof value === 'string' && Number.isFinite(Date.parse(value));
}

function normalizeChallenge(response) {
  const valid = response?.accepted === true
    && response.flowType === 'PASSWORD_RECOVERY'
    && typeof response.recoveryFlowToken === 'string'
    && response.recoveryFlowToken.length > 0
    && isValidTimestamp(response.otpExpiresAt)
    && isValidTimestamp(response.resendAvailableAt)
    && isValidTimestamp(response.challengeExpiresAt);
  if (!valid) throw new Error('INVALID_PASSWORD_RECOVERY_RESPONSE');
  return response;
}

export const passwordRecoveryService = Object.freeze({
  async start(email, signal) {
    return normalizeChallenge(await authApi.startPasswordRecovery({ email: email.trim() }, signal));
  },
  async verify(code, recoveryFlowToken, signal) {
    const response = await authApi.verifyPasswordRecovery(code, recoveryFlowToken, signal);
    if (!response?.resetAuthorizedToken || !isValidTimestamp(response.resetTokenExpiresAt)) {
      throw new Error('INVALID_PASSWORD_RESET_AUTHORIZATION_RESPONSE');
    }
    return response;
  },
  async resend(recoveryFlowToken, signal) {
    return normalizeChallenge(await authApi.resendPasswordRecovery(recoveryFlowToken, signal));
  },
  complete(payload, resetAuthorizedToken) {
    return authApi.completePasswordRecovery(payload, resetAuthorizedToken);
  },
});
