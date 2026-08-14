import assert from 'node:assert/strict';
import test from 'node:test';
import { getAdminPageTitle } from '../src/features/admin/utils/adminPageTitle.js';

test('trả đúng tiêu đề cho các trang danh sách Admin', () => {
  assert.equal(getAdminPageTitle('/admin'), 'Tổng quan quản trị');
  assert.equal(getAdminPageTitle('/admin/users'), 'Quản lý người dùng');
  assert.equal(getAdminPageTitle('/admin/user-analytics'), 'Thống kê người dùng');
  assert.equal(getAdminPageTitle('/admin/posts'), 'Quản lý bài viết');
  assert.equal(getAdminPageTitle('/admin/hashtags'), 'Quản lý hashtag');
  assert.equal(getAdminPageTitle('/admin/academic'), 'Dữ liệu học thuật');
  assert.equal(getAdminPageTitle('/admin/reports'), 'Quản lý báo cáo');
  assert.equal(getAdminPageTitle('/admin/actions'), 'Lịch sử quản trị');
});

test('ưu tiên tiêu đề route chi tiết thay vì route danh sách', () => {
  assert.equal(getAdminPageTitle('/admin/posts/15'), 'Chi tiết bài viết');
  assert.equal(getAdminPageTitle('/admin/reports/21'), 'Chi tiết báo cáo');
  assert.equal(getAdminPageTitle('/admin/profile-reports/34'), 'Chi tiết báo cáo hồ sơ');
});

test('dùng tiêu đề quản trị an toàn cho route Admin chưa được ánh xạ', () => {
  assert.equal(getAdminPageTitle('/admin/new-module'), 'Trang quản trị');
});
