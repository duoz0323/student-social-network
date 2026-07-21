const AUTH_BASE = '/api/v1/auth';
const AUTH_PROVIDERS_BASE = '/api/v1/users/me/auth-providers';
const USER_ONBOARDING_BASE = '/api/v1/users/me/onboarding';

// CÃ¡c path dÆ°á»›i Ä‘Ã¢y Ä‘Æ°á»£c Ä‘á»‘i chiáº¿u trá»±c tiáº¿p vá»›i AuthController hiá»‡n hÃ nh.
export const AUTH_ENDPOINTS = Object.freeze({
  registrations: `${AUTH_BASE}/registrations`,
  verifyRegistration: `${AUTH_BASE}/registrations/verify`,
  resendRegistration: `${AUTH_BASE}/registrations/resend`,
  registrationStatus: `${AUTH_BASE}/registrations/status`,
  cancelRegistration: `${AUTH_BASE}/registrations/cancel`,
  login: `${AUTH_BASE}/login`,
  passwordRecovery: `${AUTH_BASE}/password-recovery`,
  verifyPasswordRecovery: `${AUTH_BASE}/password-recovery/verify`,
  resendPasswordRecovery: `${AUTH_BASE}/password-recovery/resend`,
  completePasswordRecovery: `${AUTH_BASE}/password-recovery/complete`,
  googleAuth: `${AUTH_BASE}/oauth/google`,
  facebookAuth: `${AUTH_BASE}/oauth/facebook`,
  resolveSocialConflict: `${AUTH_BASE}/registrations/resolve-social-conflict`,
  refreshToken: `${AUTH_BASE}/refresh-token`,
  logout: `${AUTH_BASE}/logout`,
  reauthenticate: `${AUTH_BASE}/reauthenticate`,
  authProviders: AUTH_PROVIDERS_BASE,
  linkEmail: `${AUTH_PROVIDERS_BASE}/email`,
  verifyLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/verify`,
  resendLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/resend`,
  linkGoogle: `${AUTH_PROVIDERS_BASE}/google`,
  linkFacebook: `${AUTH_PROVIDERS_BASE}/facebook`,
  unlinkProvider: (provider) => `${AUTH_PROVIDERS_BASE}/${encodeURIComponent(provider)}`,
});

export const USER_ENDPOINTS = Object.freeze({
  onboarding: USER_ONBOARDING_BASE,
});
