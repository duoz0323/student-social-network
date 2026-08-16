import { AUTH_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { requestData, withoutUndefined } from './requestData.js';

const FLOW_TOKEN_HEADER = 'X-Auth-Flow-Token';
// Public Auth request không nhận JWT cũ và không kích hoạt refresh session.
const PUBLIC_REQUEST = Object.freeze({ skipAuth: true, skipAuthRefresh: true });

function flowTokenConfig(flowToken, extraConfig = {}) {
  return {
    ...extraConfig,
    headers: flowToken ? { ...extraConfig.headers, [FLOW_TOKEN_HEADER]: flowToken } : extraConfig.headers,
  };
}

export const authApi = Object.freeze({
  startRegistration: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.registrations, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  verifyRegistrationOtp: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyRegistration, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  resendRegistrationOtp: (registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendRegistration, { registrationFlowToken }, { ...PUBLIC_REQUEST, signal })),
  getRegistrationStatus: (registrationFlowToken, signal) => requestData(httpClient.get(AUTH_ENDPOINTS.registrationStatus, flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  cancelRegistration: (registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.cancelRegistration, { registrationFlowToken }, { ...PUBLIC_REQUEST, signal })),
  login: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.login, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  startPasswordRecovery: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.passwordRecovery, withoutUndefined(payload), { ...PUBLIC_REQUEST, signal })),
  verifyPasswordRecovery: (code, flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyPasswordRecovery, { code }, flowTokenConfig(flowToken, { ...PUBLIC_REQUEST, signal }))),
  resendPasswordRecovery: (flowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendPasswordRecovery, undefined, flowTokenConfig(flowToken, { ...PUBLIC_REQUEST, signal }))),
  completePasswordRecovery: (payload, resetToken) => requestData(httpClient.post(AUTH_ENDPOINTS.completePasswordRecovery, withoutUndefined(payload), flowTokenConfig(resetToken, PUBLIC_REQUEST))),
  refreshToken: (refreshToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.refreshToken, { refreshToken }, { ...PUBLIC_REQUEST, signal })),
  logout: (refreshToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.logout, { refreshToken }, { ...PUBLIC_REQUEST, signal })),
  authenticateWithGoogle: (payload, registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.googleAuth, withoutUndefined(payload), flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  authenticateWithFacebook: (payload, registrationFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.facebookAuth, withoutUndefined(payload), flowTokenConfig(registrationFlowToken, { ...PUBLIC_REQUEST, signal }))),
  resolveSocialConflict: (payload, socialConflictFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resolveSocialConflict, withoutUndefined(payload), flowTokenConfig(socialConflictFlowToken, { ...PUBLIC_REQUEST, signal }))),
  reauthenticate: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.reauthenticate, withoutUndefined(payload), { signal })),
  getAuthProviders: (signal) => requestData(httpClient.get(AUTH_ENDPOINTS.authProviders, { signal })),
  linkEmail: (email, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkEmail, { email }, { signal })),
  verifyLinkedEmail: (code, linkFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.verifyLinkedEmail, { code }, flowTokenConfig(linkFlowToken, { signal }))),
  completeLinkedEmail: (payload, linkFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.completeLinkedEmail, withoutUndefined(payload), flowTokenConfig(linkFlowToken, { signal }))),
  resendLinkedEmail: (linkFlowToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.resendLinkedEmail, undefined, flowTokenConfig(linkFlowToken, { signal }))),
  linkGoogle: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkGoogle, withoutUndefined(payload), { signal })),
  linkFacebook: (payload, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.linkFacebook, withoutUndefined(payload), { signal })),
  setPassword: (payload, reauthenticationToken, signal) => requestData(httpClient.post(AUTH_ENDPOINTS.password, withoutUndefined(payload), flowTokenConfig(reauthenticationToken, { signal }))),
  changePassword: (payload, signal) => requestData(httpClient.patch(AUTH_ENDPOINTS.password, withoutUndefined(payload), { signal })),
  unlinkProvider: (provider, reauthenticationToken, signal) => requestData(httpClient.delete(AUTH_ENDPOINTS.unlinkProvider(provider), flowTokenConfig(reauthenticationToken, { signal }))),
});
