import { authApi } from '../../../api/index.js';

export const facebookAuthService = Object.freeze({
  authenticate(accessToken, registrationFlowToken, signal) {
    // Provider credential chỉ tồn tại trong call stack này và chỉ gửi tới endpoint Facebook Auth.
    return authApi.authenticateWithFacebook({ accessToken }, registrationFlowToken, signal);
  },
});
