import assert from 'node:assert/strict';
import test from 'node:test';
import {
  ADMIN_PERMISSIONS,
  NON_DELEGABLE_ADMIN_PERMISSIONS,
  getAdminRoleLabel,
  getPrimaryAdminRoleLabel,
} from '../src/features/admin/constants/adminRbac.js';
import { ADMIN_ENDPOINTS } from '../src/api/apiEndpoints.js';

test('hiển thị tên nghiệp vụ tiếng Việt cho các role quản trị', () => {
  assert.equal(getAdminRoleLabel('SUPER_ADMIN'), 'Quản trị viên');
  assert.equal(getAdminRoleLabel('USER_MANAGER'), 'Quản lý người dùng');
  assert.equal(getAdminRoleLabel('MODERATOR'), 'Xử lý báo cáo');
  assert.equal(getAdminRoleLabel('ADS_MANAGER'), 'Quản lý ADS');
  assert.equal(getAdminRoleLabel('COLLABORATOR'), 'Cộng tác viên');
});

test('role chưa biết vẫn dùng nhãn fallback từ Backend', () => {
  assert.equal(getAdminRoleLabel('CUSTOM_ROLE', 'Vai trò tùy chỉnh'), 'Vai trò tùy chỉnh');
});

test('sidebar hiển thị role đại diện của tài khoản quản trị', () => {
  assert.equal(getPrimaryAdminRoleLabel(['SUPER_ADMIN', 'USER_MANAGER']), 'Master Admin');
  assert.equal(getPrimaryAdminRoleLabel(['MODERATOR']), 'Xử lý báo cáo');
  assert.equal(getPrimaryAdminRoleLabel(['COLLABORATOR', 'ADS_MANAGER']), 'Quản lý ADS');
});

test('khai báo permission riêng cho thao tác cấp lại mật khẩu admin', () => {
  assert.equal(ADMIN_PERMISSIONS.ADMIN_PASSWORD_RESET, 'ADMIN_PASSWORD_RESET');
});

test('endpoint tạo vai trò dùng collection roles và role động giữ nhãn Backend', () => {
  assert.equal(ADMIN_ENDPOINTS.createAdminRole, '/api/v1/admin/roles');
  assert.equal(getAdminRoleLabel('EVENT_MANAGER', 'Quản lý sự kiện'), 'Quản lý sự kiện');
});

test('không cho ủy quyền tạo admin hoặc phân quyền tiếp cho role hỗ trợ', () => {
  assert.deepEqual(new Set(NON_DELEGABLE_ADMIN_PERMISSIONS), new Set([
    ADMIN_PERMISSIONS.ADMIN_CREATE,
    ADMIN_PERMISSIONS.ADMIN_ROLE_ASSIGN,
    ADMIN_PERMISSIONS.ADMIN_ROLE_REVOKE,
  ]));
});
