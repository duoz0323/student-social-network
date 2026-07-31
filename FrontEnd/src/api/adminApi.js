import { ADMIN_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

export const adminApi = Object.freeze({
  getUsers: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.users, { params: compactParams(params), signal })),
  getUser: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.user(id), { signal })),
  updateUserProfile: (id, payload, signal) => requestData(httpClient.put(ADMIN_ENDPOINTS.userProfile(id), payload, { signal })),
  blockUser: (id, reasonCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.blockUser(id), { reasonCode }, { signal })),
  unblockUser: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.unblockUser(id), undefined, { signal })),
  getPosts: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.posts, { params: compactParams(params), signal })),
  getPost: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.post(id), { signal })),
  hidePost: (id, reasonCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.hidePost(id), { reasonCode }, { signal })),
  restorePost: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.restorePost(id), undefined, { signal })),
  getModerationCases: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationCases, { params: compactParams(params), signal })),
  getModerationCase: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationCase(id), { signal })),
  resolveCaseNoViolation: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.resolveCaseNoViolation(id), {}, { signal })),
  resolveCaseAction: (id, payload, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.resolveCaseAction(id), payload, { signal })),
  getActions: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.actions, { params: compactParams(params), signal })),
  getAction: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.action(id), { signal })),
});
