const AUTH_BASE = '/api/v1/auth';
const AUTH_PROVIDERS_BASE = '/api/v1/users/me/auth-providers';

// Các path dưới đây được đối chiếu trực tiếp với AuthController hiện hành.
export const AUTH_ENDPOINTS = Object.freeze({
  registrations: `${AUTH_BASE}/registrations`,
  verifyRegistration: `${AUTH_BASE}/registrations/verify`,
  resendRegistration: `${AUTH_BASE}/registrations/resend`,
  registrationStatus: `${AUTH_BASE}/registrations/status`,
  cancelRegistration: `${AUTH_BASE}/registrations/cancel`,
  login: `${AUTH_BASE}/login`,
  googleAuth: `${AUTH_BASE}/oauth/google`,
  facebookAuth: `${AUTH_BASE}/oauth/facebook`,
  resolveSocialConflict: `${AUTH_BASE}/registrations/resolve-social-conflict`,
  refreshToken: `${AUTH_BASE}/refresh-token`,
  logout: `${AUTH_BASE}/logout`,
  reauthenticate: `${AUTH_BASE}/reauthenticate`,
  authProviders: AUTH_PROVIDERS_BASE,
  linkEmail: `${AUTH_PROVIDERS_BASE}/email`,
  linkPhone: `${AUTH_PROVIDERS_BASE}/phone`,
  verifyLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/verify`,
  verifyLinkedPhone: `${AUTH_PROVIDERS_BASE}/phone/verify`,
  resendLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/resend`,
  resendLinkedPhone: `${AUTH_PROVIDERS_BASE}/phone/resend`,
  linkGoogle: `${AUTH_PROVIDERS_BASE}/google`,
  linkFacebook: `${AUTH_PROVIDERS_BASE}/facebook`,
  unlinkProvider: (provider) => `${AUTH_PROVIDERS_BASE}/${encodeURIComponent(provider)}`,
});
