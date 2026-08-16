import { authApi } from '../../../api/index.js';
import { SET_PASSWORD_PURPOSE, UNLINK_PURPOSE } from '../constants/authProviderConstants.js';
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
  completeEmailLink(payload, linkFlowToken, signal) {
    return authApi.completeLinkedEmail(payload, linkFlowToken, signal);
  },
  resendEmailLink(linkFlowToken, signal) {
    return authApi.resendLinkedEmail(linkFlowToken, signal);
  },
  linkSocial(type, credential, signal) {
    return type === 'GOOGLE'
      ? authApi.linkGoogle({ idToken: credential }, signal)
      : authApi.linkFacebook({ accessToken: credential }, signal);
  },
  reauthenticate(method, targetMethod, credential, signal, purpose = UNLINK_PURPOSE) {
    const payload = { method, purpose, targetMethod };
    if (method === 'PASSWORD') payload.password = credential;
    else payload.providerCredential = credential;
    return authApi.reauthenticate(payload, signal);
  },
  unlink(targetMethod, token, signal) {
    return authApi.unlinkProvider(targetMethod, token, signal);
  },
  async setPassword(proofMethod, credential, payload, signal) {
    const challenge = await this.reauthenticate(
      proofMethod, 'EMAIL', credential, signal, SET_PASSWORD_PURPOSE,
    );
    if (!challenge?.reauthenticationToken || challenge.purpose !== SET_PASSWORD_PURPOSE) {
      throw new Error('INVALID_REAUTHENTICATION_RESPONSE');
    }
    return authApi.setPassword(payload, challenge.reauthenticationToken, signal);
  },
  changePassword(payload, signal) {
    return authApi.changePassword(payload, signal);
  },
});
