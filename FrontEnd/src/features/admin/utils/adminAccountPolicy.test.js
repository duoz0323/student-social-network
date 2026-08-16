import assert from 'node:assert/strict';
import test from 'node:test';
import { canManageMasterProtectedAccount, isMasterAdmin } from './adminAccountPolicy.js';

// Khóa đồng nhất ba nhóm thao tác quản trị chéo đối với tài khoản gốc.
test('nhận diện Master Admin và khóa thao tác quản trị chéo', () => {
  const masterAdmin = { roles: ['SUPER_ADMIN', 'MODERATOR'] };

  assert.equal(isMasterAdmin(masterAdmin), true);
  assert.equal(canManageMasterProtectedAccount(masterAdmin), false);
});

test('vẫn cho phép quản lý tài khoản admin hỗ trợ', () => {
  const supportAdmin = { roles: ['MODERATOR'] };

  assert.equal(isMasterAdmin(supportAdmin), false);
  assert.equal(canManageMasterProtectedAccount(supportAdmin), true);
});
