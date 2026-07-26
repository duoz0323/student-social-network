import { DISCOVERY_ENDPOINTS, USER_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

export const socialApi = Object.freeze({
  getMyProfile: (signal) => requestData(httpClient.get(USER_ENDPOINTS.profile, { signal })),
  getProfile: (userId, signal) => requestData(httpClient.get(USER_ENDPOINTS.publicProfile(userId), { signal })),
  updateProfile: (payload, signal) => requestData(httpClient.put(USER_ENDPOINTS.profile, payload, { signal })),
  uploadAvatar: (file, signal) => {
    const formData = new FormData();
    formData.append('file', file);
    return requestData(httpClient.post(USER_ENDPOINTS.avatar, formData, { signal }));
  },
  deleteAvatar: (signal) => requestData(httpClient.delete(USER_ENDPOINTS.avatar, { signal })),
  follow: (userId, signal) => requestData(httpClient.post(USER_ENDPOINTS.follow(userId), undefined, { signal })),
  unfollow: (userId, signal) => requestData(httpClient.delete(USER_ENDPOINTS.follow(userId), { signal })),
  getFollowers: (userId, signal) => requestData(httpClient.get(USER_ENDPOINTS.followers(userId), { signal })),
  getFollowing: (userId, signal) => requestData(httpClient.get(USER_ENDPOINTS.following(userId), { signal })),
  getUserPosts: (userId, params, signal) => requestData(
    httpClient.get(USER_ENDPOINTS.posts(userId), { params: compactParams(params), signal }),
  ),
  searchUsers: (params, signal) => requestData(httpClient.get(DISCOVERY_ENDPOINTS.searchUsers, { params: compactParams(params), signal })),
  searchPosts: (params, signal) => requestData(httpClient.get(DISCOVERY_ENDPOINTS.searchPosts, { params: compactParams(params), signal })),
  getNotifications: (params, signal) => requestData(httpClient.get(DISCOVERY_ENDPOINTS.notifications, { params: compactParams(params), signal })),
  getUnreadCount: (signal) => requestData(httpClient.get(DISCOVERY_ENDPOINTS.notificationUnreadCount, { signal })),
  markNotificationRead: (id, signal) => requestData(httpClient.patch(DISCOVERY_ENDPOINTS.notificationRead(id), undefined, { signal })),
  markAllNotificationsRead: (signal) => requestData(httpClient.patch(DISCOVERY_ENDPOINTS.notificationReadAll, undefined, { signal })),
  deleteNotification: (id, signal) => requestData(httpClient.delete(DISCOVERY_ENDPOINTS.notification(id), { signal })),
});
