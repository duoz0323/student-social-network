import { authApi } from '../../../api/index.js';
import { UNLINK_PURPOSE } from '../constants/authProviderConstants.js';
import { normalizeAuthProviderMethods } from '../utils/authProviderNormalizer.js';

export const authProviderService = Object.freeze({
  async list(signal) {
    return normalizeAuthProviderMethods(await authApi.getAuthProviders(signal));
  },
  startEmailLink(email, signal) {
    return authApi.linkEmail(email, signal);
  },
  verifyEmailLink(code, linkFlowToken, signal) {
    return authApi.verifyLinkedEmail(code, linkFlowToken, signal);
  },
  resendEmailLink(linkFlowToken, signal) {
    return authApi.resendLinkedEmail(linkFlowToken, signal);
  },
  linkSocial(type, credential, signal) {
    return type === 'GOOGLE'
      ? authApi.linkGoogle({ idToken: credential }, signal)
      : authApi.linkFacebook({ accessToken: credential }, signal);
  },
  reauthenticate(method, targetMethod, credential, signal) {
    const payload = { method, purpose: UNLINK_PURPOSE, targetMethod };
    if (method === 'PASSWORD') payload.password = credential;
    else payload.providerCredential = credential;
    return authApi.reauthenticate(payload, signal);
  },
  unlink(targetMethod, token, signal) {
    return authApi.unlinkProvider(targetMethod, token, signal);
  },
});
