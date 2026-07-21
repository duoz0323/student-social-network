import { AUTH_PROVIDER_TYPES } from '../constants/authProviderConstants.js';

export function normalizeAuthProviderMethods(response) {
  const linkedByType = new Map((Array.isArray(response?.methods) ? response.methods : [])
    .filter((method) => AUTH_PROVIDER_TYPES.includes(method?.type))
    .map((method) => [method.type, method]));

  return AUTH_PROVIDER_TYPES.map((type) => {
    const method = linkedByType.get(type);
    return method ? {
      type,
      linked: true,
      maskedIdentifier: method.maskedIdentifier ?? null,
      linkedAt: method.linkedAt ?? null,
      verified: method.verified === true,
      canUnlink: method.canUnlink === true,
      // Không suy diễn field này từ verified hoặc loại provider.
      localLoginAvailable: method.localLoginAvailable === true,
    } : {
      type,
      linked: false,
      maskedIdentifier: null,
      linkedAt: null,
      verified: false,
      canUnlink: false,
      localLoginAvailable: false,
    };
  });
}
