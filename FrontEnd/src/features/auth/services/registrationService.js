import { authApi } from '../../../api/index.js';

const FLOW_STORAGE_KEY = 'unishare.auth.registration-flow';

function normalizeFlow(response, registrationFlowToken) {
  return {
    registrationFlowToken,
    maskedIdentifier: response.maskedIdentifier ?? '',
    identifierType: response.identifierType ?? null,
    expiresAt: response.pendingExpiresAt ?? null,
    otpExpiresAt: response.otpExpiresAt ?? null,
    resendAvailableAt: response.resendAvailableAt ?? null,
    remainingAttempts: response.remainingOtpAttempts ?? null,
    canResend: response.canResend ?? false,
    status: response.status ?? 'OTP_REQUIRED',
    nextStep: response.nextStep ?? 'VERIFY_OTP',
  };
}

function storeFlow(flow) {
  // Chỉ lưu token và metadata cần cho reload; tuyệt đối không lưu password hoặc OTP.
  sessionStorage.setItem(FLOW_STORAGE_KEY, JSON.stringify(flow));
  return flow;
}

export const registrationService = Object.freeze({
  async startRegistration(payload, signal) {
    const response = await authApi.startRegistration(payload, signal);
    return storeFlow(normalizeFlow(response, response.registrationFlowToken));
  },

  async getRegistrationStatus(signal) {
    const stored = this.getStoredFlow();
    if (!stored?.registrationFlowToken) return null;
    const response = await authApi.getRegistrationStatus(stored.registrationFlowToken, signal);
    return storeFlow(normalizeFlow(response, stored.registrationFlowToken));
  },

  async resendOtp(signal) {
    const stored = this.getStoredFlow();
    if (!stored?.registrationFlowToken) return null;
    const response = await authApi.resendRegistrationOtp(stored.registrationFlowToken, signal);
    return storeFlow(normalizeFlow(response, stored.registrationFlowToken));
  },

  async verifyOtp(code, device = {}, signal) {
    const stored = this.getStoredFlow();
    if (!stored?.registrationFlowToken) return null;
    return authApi.verifyRegistrationOtp({ registrationFlowToken: stored.registrationFlowToken, code, ...device }, signal);
  },

  async cancelRegistration(signal) {
    const stored = this.getStoredFlow();
    if (!stored?.registrationFlowToken) return null;
    return authApi.cancelRegistration(stored.registrationFlowToken, signal);
  },

  getStoredFlow() {
    try {
      const value = sessionStorage.getItem(FLOW_STORAGE_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      sessionStorage.removeItem(FLOW_STORAGE_KEY);
      return null;
    }
  },

  clearFlow() {
    sessionStorage.removeItem(FLOW_STORAGE_KEY);
  },
});
