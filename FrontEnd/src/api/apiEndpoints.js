const AUTH_BASE = '/api/v1/auth';
const AUTH_PROVIDERS_BASE = '/api/v1/users/me/auth-providers';
const USER_ONBOARDING_BASE = '/api/v1/users/me/onboarding';

// CÃ¡c path dÆ°á»›i Ä‘Ã¢y Ä‘Æ°á»£c Ä‘á»‘i chiáº¿u trá»±c tiáº¿p vá»›i AuthController hiá»‡n hÃ nh.
export const AUTH_ENDPOINTS = Object.freeze({
  registrations: `${AUTH_BASE}/registrations`,
  verifyRegistration: `${AUTH_BASE}/registrations/verify`,
  resendRegistration: `${AUTH_BASE}/registrations/resend`,
  registrationStatus: `${AUTH_BASE}/registrations/status`,
  cancelRegistration: `${AUTH_BASE}/registrations/cancel`,
  login: `${AUTH_BASE}/login`,
  passwordRecovery: `${AUTH_BASE}/password-recovery`,
  verifyPasswordRecovery: `${AUTH_BASE}/password-recovery/verify`,
  resendPasswordRecovery: `${AUTH_BASE}/password-recovery/resend`,
  completePasswordRecovery: `${AUTH_BASE}/password-recovery/complete`,
  googleAuth: `${AUTH_BASE}/oauth/google`,
  facebookAuth: `${AUTH_BASE}/oauth/facebook`,
  resolveSocialConflict: `${AUTH_BASE}/registrations/resolve-social-conflict`,
  refreshToken: `${AUTH_BASE}/refresh-token`,
  logout: `${AUTH_BASE}/logout`,
  reauthenticate: `${AUTH_BASE}/reauthenticate`,
  authProviders: AUTH_PROVIDERS_BASE,
  linkEmail: `${AUTH_PROVIDERS_BASE}/email`,
  verifyLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/verify`,
  resendLinkedEmail: `${AUTH_PROVIDERS_BASE}/email/resend`,
  linkGoogle: `${AUTH_PROVIDERS_BASE}/google`,
  linkFacebook: `${AUTH_PROVIDERS_BASE}/facebook`,
  unlinkProvider: (provider) => `${AUTH_PROVIDERS_BASE}/${encodeURIComponent(provider)}`,
});

export const USER_ENDPOINTS = Object.freeze({
  onboarding: USER_ONBOARDING_BASE,
  profile: '/api/v1/users/me/profile',
  avatar: '/api/v1/users/me/avatar',
  follow: (userId) => `/api/v1/users/${encodeURIComponent(userId)}/follow`,
  followers: (userId) => `/api/v1/users/${encodeURIComponent(userId)}/followers`,
  following: (userId) => `/api/v1/users/${encodeURIComponent(userId)}/following`,
});

export const POST_ENDPOINTS = Object.freeze({
  root: '/api/v1/posts',
  detail: (postId) => `/api/v1/posts/${encodeURIComponent(postId)}`,
  likes: (postId) => `/api/v1/posts/${encodeURIComponent(postId)}/likes`,
  saves: (postId) => `/api/v1/posts/${encodeURIComponent(postId)}/saves`,
  reports: (postId) => `/api/v1/posts/${encodeURIComponent(postId)}/reports`,
  comments: (postId) => `/api/v1/posts/${encodeURIComponent(postId)}/comments`,
  replies: (commentId) => `/api/v1/comments/${encodeURIComponent(commentId)}/replies`,
  comment: (commentId) => `/api/v1/comments/${encodeURIComponent(commentId)}`,
  hashtagSuggestions: '/api/v1/hashtags/suggestions',
});

export const DISCOVERY_ENDPOINTS = Object.freeze({
  searchUsers: '/api/v1/search/users',
  searchPosts: '/api/v1/search/posts',
  notifications: '/api/v1/notifications',
  notificationUnreadCount: '/api/v1/notifications/unread-count',
  notificationReadAll: '/api/v1/notifications/read-all',
  notificationRead: (notificationId) => `/api/v1/notifications/${encodeURIComponent(notificationId)}/read`,
  notification: (notificationId) => `/api/v1/notifications/${encodeURIComponent(notificationId)}`,
});

export const ADMIN_ENDPOINTS = Object.freeze({
  users: '/api/v1/admin/users',
  user: (userId) => `/api/v1/admin/users/${encodeURIComponent(userId)}`,
  blockUser: (userId) => `/api/v1/admin/users/${encodeURIComponent(userId)}/block`,
  unblockUser: (userId) => `/api/v1/admin/users/${encodeURIComponent(userId)}/unblock`,
  posts: '/api/v1/admin/posts',
  post: (postId) => `/api/v1/admin/posts/${encodeURIComponent(postId)}`,
  hidePost: (postId) => `/api/v1/admin/posts/${encodeURIComponent(postId)}/hide`,
  restorePost: (postId) => `/api/v1/admin/posts/${encodeURIComponent(postId)}/restore`,
  reports: '/api/v1/admin/reports',
  report: (reportId) => `/api/v1/admin/reports/${encodeURIComponent(reportId)}`,
  rejectReport: (reportId) => `/api/v1/admin/reports/${encodeURIComponent(reportId)}/reject`,
  resolveReport: (reportId) => `/api/v1/admin/reports/${encodeURIComponent(reportId)}/resolve`,
  actions: '/api/v1/admin/actions',
  action: (actionId) => `/api/v1/admin/actions/${encodeURIComponent(actionId)}`,
});
