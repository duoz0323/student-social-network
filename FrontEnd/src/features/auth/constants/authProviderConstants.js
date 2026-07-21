
export const AUTH_PROVIDER_META = Object.freeze({
  EMAIL: Object.freeze({ label: 'Email', kind: 'LOCAL' }),
  GOOGLE: Object.freeze({ label: 'Google', kind: 'SOCIAL' }),
  FACEBOOK: Object.freeze({ label: 'Facebook', kind: 'SOCIAL' }),
});

export const REAUTHENTICATION_METHODS = Object.freeze({
  PASSWORD: 'PASSWORD',
  GOOGLE: 'GOOGLE',
  FACEBOOK: 'FACEBOOK',
});

export const UNLINK_PURPOSE = 'UNLINK_AUTH_METHOD';
export const AUTH_PROVIDER_TYPES = Object.freeze(['EMAIL', 'GOOGLE', 'FACEBOOK']);
