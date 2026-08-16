import assert from 'node:assert/strict';
import test from 'node:test';
import { getAuthenticatedHome, getSafeReturnPath } from './authNavigation.js';

test('ưu tiên trang quản trị chính cho Master Admin', () => {
  assert.equal(getAuthenticatedHome({
    role: 'ADMIN',
    user: { role: 'ADMIN', adminRoles: ['SUPER_ADMIN'] },
    profileCompleted: true,
    permissions: ['DASHBOARD_BASIC_VIEW', 'COLLABORATOR_DASHBOARD_VIEW'],
  }), '/admin');
});

test('đưa tài khoản mang role COLLABORATOR vào dashboard cộng tác viên', () => {
  assert.equal(getAuthenticatedHome({
    role: 'ADMIN',
    user: { role: 'ADMIN', adminRoles: ['COLLABORATOR'] },
    profileCompleted: true,
    permissions: ['DASHBOARD_BASIC_VIEW', 'COLLABORATOR_DASHBOARD_VIEW'],
  }), '/admin/collaborator');
});

test('cho phép Cộng tác viên khôi phục trang hồ sơ Admin sau đăng nhập', () => {
  const session = {
    user: { role: 'ADMIN', adminRoles: ['COLLABORATOR'] },
    profileCompleted: true,
    permissions: ['COLLABORATOR_DASHBOARD_VIEW'],
  };

  assert.equal(getSafeReturnPath('/admin/profile', session), '/admin/profile');
});

test('không khôi phục hai khu vực đã gỡ khỏi giao diện Cộng tác viên', () => {
  const session = {
    user: { role: 'ADMIN', adminRoles: ['COLLABORATOR'] },
    profileCompleted: true,
    permissions: [
      'COLLABORATOR_DASHBOARD_VIEW',
      'COLLABORATOR_EXPLORE_VIEW',
      'COLLABORATOR_MODERATION_SUGGESTION_VIEW_OWN',
    ],
  };

  assert.equal(getSafeReturnPath('/admin/collaborator/explore', session), null);
  assert.equal(getSafeReturnPath('/admin/collaborator/moderation-suggestions', session), null);
});
