import test from 'node:test';
import assert from 'node:assert/strict';
import { getAuthenticatedHome, getSafeReturnPath } from '../src/features/auth/utils/authNavigation.js';

const adminSession = (permissions) => ({
  profileCompleted: true,
  user: { role: 'ADMIN', permissions },
});

test('USER_MANAGER có Dashboard và được đưa đến Tổng quan', () => {
  assert.equal(getAuthenticatedHome(adminSession([
    'DASHBOARD_BASIC_VIEW', 'USER_VIEW', 'USER_ANALYTICS_VIEW',
  ])), '/admin');
});

test('MODERATOR có Dashboard, bài viết, hashtag và báo cáo', () => {
  assert.equal(getAuthenticatedHome(adminSession([
    'DASHBOARD_BASIC_VIEW', 'POST_VIEW', 'HASHTAG_VIEW', 'REPORT_VIEW',
  ])), '/admin');
});

test('ADS_MANAGER và COLLABORATOR có quyền Dashboard được đưa đến Tổng quan', () => {
  assert.equal(getAuthenticatedHome(adminSession(['DASHBOARD_BASIC_VIEW'])), '/admin');
});

test('COLLABORATOR mới được đưa đến Dashboard riêng và chỉ khôi phục route đã cấp quyền', () => {
  const collaborator = adminSession([
    'COLLABORATOR_DASHBOARD_VIEW', 'COLLABORATOR_POST_VIEW_OWN',
    'COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN',
  ]);
  assert.equal(getAuthenticatedHome(collaborator), '/admin/collaborator');
  assert.equal(getSafeReturnPath('/admin/collaborator/posts', collaborator), '/admin/collaborator/posts');
  assert.equal(getSafeReturnPath('/admin/users', collaborator), null);
});

test('URL Khám phá và Đề xuất của tôi được khôi phục theo permission COLLABORATOR', () => {
  const collaborator = adminSession([
    'COLLABORATOR_DASHBOARD_VIEW', 'COLLABORATOR_EXPLORE_VIEW',
    'COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN',
  ]);

  assert.equal(getSafeReturnPath('/admin/collaborator/explore', collaborator), '/admin/collaborator/explore');
  assert.equal(getSafeReturnPath('/admin/collaborator/moderation-suggestions', collaborator), '/admin/collaborator/moderation-suggestions');
});

test('Admin không có permission không được điều hướng vào khu vực quản trị', () => {
  assert.equal(getAuthenticatedHome(adminSession([])), '/403');
});

test('USER vẫn được đưa về Feed', () => {
  assert.equal(getAuthenticatedHome({ profileCompleted: true, user: { role: 'USER' } }), '/feed/for-you');
});

test('không khôi phục return path Admin mà tài khoản không có permission', () => {
  const userManager = adminSession(['DASHBOARD_BASIC_VIEW', 'USER_VIEW', 'USER_ANALYTICS_VIEW']);
  assert.equal(getSafeReturnPath('/admin', userManager), '/admin');
  assert.equal(getSafeReturnPath('/admin/users', userManager), '/admin/users');
});
