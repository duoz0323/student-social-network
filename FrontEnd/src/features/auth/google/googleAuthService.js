import { authApi } from '../../../api/index.js';

export const googleAuthService = Object.freeze({
  authenticate(idToken, registrationFlowToken, signal) {
    if (!idToken) throw new Error('GOOGLE_CREDENTIAL_MISSING');
    // Credential chỉ đi thẳng tới Backend và không được decode hoặc lưu trong service.
    return authApi.authenticateWithGoogle({ idToken }, registrationFlowToken, signal);
  },
});
