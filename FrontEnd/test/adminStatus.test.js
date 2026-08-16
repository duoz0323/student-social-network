import assert from 'node:assert/strict';
import test from 'node:test';
import { getAdminStatusMeta } from '../src/features/admin/constants/adminStatus.js';

test('Việt hóa và tô màu trạng thái tài khoản quản trị', () => {
  assert.deepEqual(getAdminStatusMeta('ACTIVE'), {
    label: 'Hoạt động', tone: 'success', dotClassName: 'bg-emerald-500',
  });
  assert.deepEqual(getAdminStatusMeta('BLOCKED'), {
    label: 'Đã khóa', tone: 'danger', dotClassName: 'bg-red-500',
  });
});

test('Việt hóa và tô màu ba trạng thái bài viết', () => {
  assert.equal(getAdminStatusMeta('PUBLISHED').label, 'Đã đăng');
  assert.equal(getAdminStatusMeta('HIDDEN').label, 'Đã ẩn');
  assert.equal(getAdminStatusMeta('HIDDEN').tone, 'warning');
  assert.equal(getAdminStatusMeta('DELETED').label, 'Đã xóa');
  assert.equal(getAdminStatusMeta('DELETED').tone, 'danger');
});
