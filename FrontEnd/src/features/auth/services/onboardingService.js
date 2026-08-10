import { USER_ENDPOINTS } from '../../../api/apiEndpoints.js';
import { httpClient } from '../../../api/httpClient.js';

export const onboardingService = Object.freeze({
  async getStatus(signal) {
    const response = await httpClient.get(USER_ENDPOINTS.onboarding, { signal });
    return response.data?.data ?? response.data;
  },

  async checkUsernameAvailability(username, signal) {
    const response = await httpClient.get(USER_ENDPOINTS.usernameAvailability, {
      params: { username },
      signal,
    });
    return response.data?.data ?? response.data;
  },

  async completeProfile(payload, signal) {
    // Backend chỉ nhận các trường hồ sơ thuộc contract onboarding; userId luôn lấy từ JWT.
    const response = await httpClient.put(USER_ENDPOINTS.onboarding, {
      username: payload.username.trim(),
      displayName: payload.displayName.trim(),
      dateOfBirth: payload.dateOfBirth,
      bio: payload.bio.trim() || null,
    }, { signal });
    return response.data?.data ?? response.data;
  },
});
