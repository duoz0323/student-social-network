import assert from 'node:assert/strict';
import test from 'node:test';
import { getAdminNavigationScopes } from './adminRbac.js';

test('giữ menu quản trị đầy đủ cho Master Admin dù có toàn bộ permission cộng tác viên', () => {
  assert.deepEqual(getAdminNavigationScopes(['SUPER_ADMIN']), {
    showRegularAdmin: true,
    showCollaborator: false,
  });
});

test('chỉ hiện menu cộng tác viên khi tài khoản chỉ mang role COLLABORATOR', () => {
  assert.deepEqual(getAdminNavigationScopes(['COLLABORATOR']), {
    showRegularAdmin: false,
    showCollaborator: true,
  });
});

test('giữ chức năng của cả hai nhóm khi tài khoản mang nhiều role', () => {
  assert.deepEqual(getAdminNavigationScopes(['MODERATOR', 'COLLABORATOR']), {
    showRegularAdmin: true,
    showCollaborator: true,
  });
});
