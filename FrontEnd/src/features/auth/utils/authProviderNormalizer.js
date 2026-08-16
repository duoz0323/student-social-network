import { AUTH_PROVIDER_TYPES } from '../constants/authProviderConstants.js';

export function normalizeAuthProviderMethods(response) {
  const linkedByType = new Map((Array.isArray(response?.methods) ? response.methods : [])
    .filter((method) => AUTH_PROVIDER_TYPES.includes(method?.type))
    .map((method) => [method.type, method]));

  return AUTH_PROVIDER_TYPES.map((type) => {
    const method = linkedByType.get(type);
    return method ? {
      type,
      linked: method.linked === true,
      maskedIdentifier: method.maskedIdentifier ?? null,
      linkedAt: method.linkedAt ?? null,
      verified: method.verified === true,
      canUnlink: method.canUnlink === true,
      canLink: method.canLink === true,
      state: method.state ?? null,
      passwordConfigured: method.passwordConfigured === true,
      canSetPassword: method.canSetPassword === true,
      canChangePassword: method.canChangePassword === true,
      // Không suy diễn field này từ verified hoặc loại provider.
      localLoginAvailable: method.localLoginAvailable === true,
    } : {
      type,
      linked: false,
      maskedIdentifier: null,
      linkedAt: null,
      verified: false,
      canUnlink: false,
      canLink: true,
      state: type === 'EMAIL' ? 'NOT_LINKED' : null,
      passwordConfigured: false,
      canSetPassword: false,
      canChangePassword: false,
      localLoginAvailable: false,
    };
  });
}
