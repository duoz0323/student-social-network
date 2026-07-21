import { USER_ENDPOINTS } from '../../../api/apiEndpoints.js';
import { httpClient } from '../../../api/httpClient.js';

export const onboardingService = Object.freeze({
  async completeProfile(payload, signal) {
    // Backend chỉ nhận các trường hồ sơ thuộc contract onboarding; userId luôn lấy từ JWT.
    const response = await httpClient.put(USER_ENDPOINTS.onboarding, {
      displayName: payload.displayName.trim(),
      dateOfBirth: payload.dateOfBirth,
      bio: payload.bio.trim() || null,
    }, { signal });
    return response.data?.data ?? response.data;
  },
});
