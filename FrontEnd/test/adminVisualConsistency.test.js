import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { hasPageOwnedAdminHeader } from '../src/features/admin/utils/adminPageTitle.js';

const source = (relativePath) => readFileSync(new URL(`../src/${relativePath}`, import.meta.url), 'utf8');

test('các trang quản trị chính dùng chung một cấu trúc header', () => {
  const pages = [
    'features/admin/pages/AdminAcademicPage.jsx',
    'features/admin/pages/AdminHashtagsPage.jsx',
    'features/admin/pages/AdminManagementPage.jsx',
    'features/admin/pages/AdminRolePermissionsPage.jsx',
    'features/admin/pages/AdminActionsPage.jsx',
  ];

  for (const page of pages) {
    assert.match(source(page), /AdminPageHeader/, `${page} phải dùng AdminPageHeader`);
  }

  const header = source('features/admin/components/AdminPageHeader.jsx');
  assert.match(header, /bg-zinc-100 text-zinc-700/);
  assert.match(header, /text-2xl font-bold text-zinc-950/);
});

test('các thao tác quản trị đã chỉnh không còn ghi đè nút bằng màu xanh', () => {
  const interactiveFiles = [
    'features/admin/components/AdminEditUserProfileDialog.jsx',
    'features/admin/components/AdminUserDetailDialog.jsx',
    'features/admin/notifications/AdminNotificationBell.jsx',
    'features/admin/collaborator/pages/CollaboratorIdentityPage.jsx',
    'features/admin/collaborator/pages/CollaboratorHashtagsPage.jsx',
  ];

  for (const file of interactiveFiles) {
    assert.doesNotMatch(
      source(file),
      /(?:bg|text|border|ring)-(?:blue|indigo)-(?:500|600|700)|focus:(?:border|ring)-(?:blue|indigo)/,
      `${file} phải dùng hệ màu tương tác đen–zinc`,
    );
  }
});

test('AdminShell không lặp tiêu đề khi trang con đã có header riêng', () => {
  const pageOwnedRoutes = [
    '/admin',
    '/admin/hashtags',
    '/admin/academic',
    '/admin/moderation-suggestions',
    '/admin/actions',
    '/admin/collaborator/posts',
  ];

  assert.ok(pageOwnedRoutes.every(hasPageOwnedAdminHeader));
  assert.equal(hasPageOwnedAdminHeader('/admin/users'), false);
  assert.equal(hasPageOwnedAdminHeader('/admin/posts'), false);
  assert.equal(hasPageOwnedAdminHeader('/admin/reports'), false);
  assert.match(source('components/layout/AdminShell.jsx'), /pageOwnsHeader \?/);
});

test('chuông thông báo nằm cùng hàng với header riêng của trang', () => {
  const shell = source('components/layout/AdminShell.jsx');

  assert.match(shell, /absolute right-0 top-0 z-10/);
  assert.match(shell, /header:first-child\]:pr-14/);
  assert.doesNotMatch(shell, /pageOwnsHeader \? 'mb-4 justify-end'/);
});
