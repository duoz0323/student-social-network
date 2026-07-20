import { AUTH_ENDPOINTS } from './apiEndpoints.js';
import { normalizeApiError } from './apiError.js';
import { httpClient } from './httpClient.js';

const FLOW_TOKEN_HEADER = 'X-Auth-Flow-Token';
// Public Auth request không nhận JWT cũ và cũng không kích hoạt refresh session.
const PUBLIC_REQUEST = Object.freeze({ skipAuth: true, skipAuthRefresh: true });

function withoutUndefined(payload) {
  return Object.fromEntries(Object.entries(payload ?? {}).filter(([, value]) => value !== undefined));
}

function flowTokenConfig(flowToken, extraConfig = {}) {
  return {
    ...extraConfig,
    headers: flowToken ? { ...extraConfig.headers, [FLOW_TOKEN_HEADER]: flowToken } : extraConfig.headers,
  };
}

async function requestData(request) {
  try {
    const response = await request;
    // Backend bọc payload nghiệp vụ trong ApiResponse.data.
    return response.data?.data ?? response.data;
  } catch (error) {
    throw normalizeApiError(error);
  }
}

export const authApi = Object.freeze({
  startRegistration: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.registrations, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  verifyRegistrationOtp: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyRegistration, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  resendRegistrationOtp: (registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendRegistration, { registrationFlowToken }, { ...PUBLIC_REQUEST, signal })),
  getRegistrationStatus: (registrationFlowToken, signal) => requestData(httpClient.get(AUTH_ENDPOINTS.registrationStatus, flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  cancelRegistration: (registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.cancelRegistration, { registrationFlowToken }, { ...PUBLIC_REQUEST, signal })),
  login: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.login, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  refreshToken: (refreshToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.refreshToken, { refreshToken }, { ...PUBLIC_REQUEST, signal })),
  logout: (refreshToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.logout, { refreshToken }, { ...PUBLIC_REQUEST, signal })),
  authenticateWithGoogle: (payload, registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.googleAuth, withoutUndefined(payload), flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  authenticateWithFacebook: (payload, registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.facebookAuth, withoutUndefined(payload), flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  resolveSocialConflict: (payload, challengeToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resolveSocialConflict, withoutUndefined(payload), flowTokenConfig(challengeToken, { ...PUBLIC_REQUEST, signal }))),
  reauthenticate: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.reauthenticate, withoutUndefined(payload), { signal })),
  getAuthProviders: (signal) => requestData(httpClient.get(AUTH_ENDPOINTS.authProviders, { signal })),
  linkEmail: (email, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkEmail, { email }, { signal })),
  linkPhone: (phoneNumber, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkPhone, { phoneNumber }, { signal })),
  verifyLinkedEmail: (code, flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyLinkedEmail, { code }, flowTokenConfig(flowToken, { signal }))),
  verifyLinkedPhone: (code, flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyLinkedPhone, { code }, flowTokenConfig(flowToken, { signal }))),
  resendLinkedEmail: (flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendLinkedEmail, undefined, flowTokenConfig(flowToken, { signal }))),
  resendLinkedPhone: (flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendLinkedPhone, undefined, flowTokenConfig(flowToken, { signal }))),
  linkGoogle: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkGoogle, withoutUndefined(payload), { signal })),
  linkFacebook: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkFacebook, withoutUndefined(payload), { signal })),
  unlinkProvider: (provider, reauthenticationToken, signal) => requestData(httpClient.delete(AUTH_ENDPOINTS.unlinkProvider(provider), flowTokenConfig(reauthenticationToken, { signal }))),
});
