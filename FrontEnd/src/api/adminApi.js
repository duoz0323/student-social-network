import { ADMIN_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

export const adminApi = Object.freeze({
  getUsers: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.users, { params: compactParams(params), signal })),
  getUser: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.user(id), { signal })),
  updateUserProfile: (id, payload, avatarChange = {}, signal) => {
    const { avatarAction = 'KEEP', avatarFile = null } = avatarChange;
    if (avatarAction === 'KEEP') {
      return requestData(httpClient.put(ADMIN_ENDPOINTS.userProfile(id), payload, { signal }));
    }
    const formData = new FormData();
    formData.append('profile', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    formData.append('avatarAction', avatarAction);
    if (avatarAction === 'REPLACE' && avatarFile) formData.append('avatar', avatarFile);
    return requestData(httpClient.put(ADMIN_ENDPOINTS.userProfile(id), formData, { signal }));
  },
  blockUser: (id, reasonCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.blockUser(id), { reasonCode }, { signal })),
  unblockUser: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.unblockUser(id), undefined, { signal })),
  getPosts: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.posts, { params: compactParams(params), signal })),
  getHashtags: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.hashtags, { params: compactParams(params), signal })),
  createHashtag: (name, signal) => requestData(httpClient.post(ADMIN_ENDPOINTS.hashtags, { name }, { signal })),
  updateHashtag: (id, name, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.hashtag(id), { name }, { signal })),
  deleteHashtag: (id, signal) => requestData(httpClient.delete(ADMIN_ENDPOINTS.hashtag(id), { signal })),
  getPost: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.post(id), { signal })),
  hidePost: (id, reasonCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.hidePost(id), { reasonCode }, { signal })),
  restorePost: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.restorePost(id), undefined, { signal })),
  getModerationCases: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationCases, { params: compactParams(params), signal })),
  getModerationCase: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationCase(id), { signal })),
  resolveCaseNoViolation: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.resolveCaseNoViolation(id), {}, { signal })),
  resolveCaseAction: (id, payload, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.resolveCaseAction(id), payload, { signal })),
  getProfileReports: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.profileReports, { params: compactParams(params), signal },
  )),
  getProfileReport: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.profileReport(id), { signal })),
  rejectProfileReport: (id, resolutionNote, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.rejectProfileReport(id), { resolutionNote }, { signal },
  )),
  resolveProfileReport: (id, resolutionNote, blockUser = false, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.resolveProfileReport(id), { resolutionNote, blockUser }, { signal },
  )),
  getActions: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.actions, { params: compactParams(params), signal })),
  getAction: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.action(id), { signal })),
  getUserEngagementMonthly: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.userEngagementMonthly,
    { params: compactParams(params), signal },
  )),
  getUserEngagementSummary: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.userEngagementSummary,
    { params: compactParams(params), signal },
  )),
  getUserEngagementDashboard: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.userEngagementDashboard,
    { params: compactParams(params), signal },
  )),
});
