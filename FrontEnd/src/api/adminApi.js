import { ADMIN_ENDPOINTS } from './apiEndpoints.js';
import { httpClient } from './httpClient.js';
import { compactParams, requestData } from './requestData.js';

export const adminApi = Object.freeze({
  getNotifications: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.notifications, { params: compactParams(params), signal },
  )),
  getNotificationUnreadCount: (signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.notificationUnreadCount, { signal },
  )),
  markNotificationRead: (id, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.notificationRead(id), undefined, { signal },
  )),
  markAllNotificationsRead: (signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.notificationReadAll, undefined, { signal },
  )),
  deleteNotification: (id, signal) => requestData(httpClient.delete(
    ADMIN_ENDPOINTS.notification(id), { signal },
  )),
  getProfile: (signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.profile, { signal })),
  updateProfile: (payload, signal) => requestData(httpClient.put(ADMIN_ENDPOINTS.profile, payload, { signal })),
  changePassword: (payload, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.profilePassword, payload, { signal },
  )),
  getAdmins: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.admins, { params: compactParams(params), signal })),
  getAdmin: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.admin(id), { signal })),
  createAdmin: (payload, signal) => requestData(httpClient.post(ADMIN_ENDPOINTS.admins, payload, { signal })),
  updateAdmin: (id, payload, signal) => requestData(httpClient.put(ADMIN_ENDPOINTS.admin(id), payload, { signal })),
  disableAdmin: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.disableAdmin(id), undefined, { signal })),
  enableAdmin: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.enableAdmin(id), undefined, { signal })),
  resetAdminPassword: (id, payload, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.resetAdminPassword(id), payload, { signal },
  )),
  assignAdminRole: (id, roleCode, signal) => requestData(httpClient.post(ADMIN_ENDPOINTS.adminRole(id, roleCode), undefined, { signal })),
  revokeAdminRole: (id, roleCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.revokeAdminRole(id, roleCode), undefined, { signal })),
  getAdminRoleCatalog: (signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.adminRoleCatalog, { signal })),
  getRolesForPermissionManagement: (signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.adminRoles, { signal })),
  createAdminRole: (name, signal) => requestData(httpClient.post(
    ADMIN_ENDPOINTS.createAdminRole, { name }, { signal },
  )),
  getPermissionCatalog: (signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.adminPermissions, { signal })),
  updateRolePermissions: (roleCode, permissionCodes, signal) => requestData(httpClient.put(
    ADMIN_ENDPOINTS.rolePermissions(roleCode), { permissionCodes }, { signal },
  )),
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
  getAcademicSchools: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.academicSchools, { params: compactParams(params), signal },
  )),
  createAcademicSchool: (payload, signal) => requestData(httpClient.post(
    ADMIN_ENDPOINTS.academicSchools, payload, { signal },
  )),
  updateAcademicSchool: (id, payload, signal) => requestData(httpClient.put(
    ADMIN_ENDPOINTS.academicSchool(id), payload, { signal },
  )),
  changeAcademicSchoolStatus: (id, status, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.academicSchoolStatus(id), { status }, { signal },
  )),
  getAcademicFaculties: (schoolId, params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.academicFaculties(schoolId), { params: compactParams(params), signal },
  )),
  createAcademicFaculty: (schoolId, name, signal) => requestData(httpClient.post(
    ADMIN_ENDPOINTS.academicFaculties(schoolId), { name }, { signal },
  )),
  updateAcademicFaculty: (id, name, signal) => requestData(httpClient.put(
    ADMIN_ENDPOINTS.academicFaculty(id), { name }, { signal },
  )),
  changeAcademicFacultyStatus: (id, status, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.academicFacultyStatus(id), { status }, { signal },
  )),
  getAcademicMajors: (facultyId, params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.academicMajors(facultyId), { params: compactParams(params), signal },
  )),
  createAcademicMajor: (facultyId, name, signal) => requestData(httpClient.post(
    ADMIN_ENDPOINTS.academicMajors(facultyId), { name }, { signal },
  )),
  updateAcademicMajor: (id, name, signal) => requestData(httpClient.put(
    ADMIN_ENDPOINTS.academicMajor(id), { name }, { signal },
  )),
  changeAcademicMajorStatus: (id, status, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.academicMajorStatus(id), { status }, { signal },
  )),
  getAcademicInterests: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.academicInterests, { params: compactParams(params), signal },
  )),
  createAcademicInterest: (name, signal) => requestData(httpClient.post(
    ADMIN_ENDPOINTS.academicInterests, { name }, { signal },
  )),
  updateAcademicInterest: (id, name, signal) => requestData(httpClient.put(
    ADMIN_ENDPOINTS.academicInterest(id), { name }, { signal },
  )),
  changeAcademicInterestStatus: (id, status, signal) => requestData(httpClient.patch(
    ADMIN_ENDPOINTS.academicInterestStatus(id), { status }, { signal },
  )),
  getPost: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.post(id), { signal })),
  hidePost: (id, reasonCode, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.hidePost(id), { reasonCode }, { signal })),
  restorePost: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.restorePost(id), undefined, { signal })),
  getModerationCases: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationCases, { params: compactParams(params), signal })),
  getModerationSuggestions: (params, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationSuggestions, { params: compactParams(params), signal })),
  getModerationSuggestion: (id, signal) => requestData(httpClient.get(ADMIN_ENDPOINTS.moderationSuggestion(id), { signal })),
  acceptModerationSuggestion: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.acceptModerationSuggestion(id), undefined, { signal })),
  rejectModerationSuggestion: (id, signal) => requestData(httpClient.patch(ADMIN_ENDPOINTS.rejectModerationSuggestion(id), undefined, { signal })),
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
  getPostAnalytics: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.postAnalytics,
    { params: compactParams(params), signal },
  )),
  getHashtagAnalytics: (params, signal) => requestData(httpClient.get(
    ADMIN_ENDPOINTS.hashtagAnalytics,
    { params: compactParams(params), signal },
  )),
});
