import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

test('trang hồ sơ cộng tác viên cho sửa độc lập public identity và hồ sơ Admin', async () => {
  const source = await readFile(new URL(
    '../src/features/admin/collaborator/pages/CollaboratorIdentityPage.jsx',
    import.meta.url,
  ), 'utf8');

  assert.match(source, /collaboratorApi\.updateIdentity/);
  assert.match(source, /collaboratorApi\.uploadIdentityAvatar/);
  assert.match(source, /Username được tạo một lần và không thể thay đổi/);
  assert.doesNotMatch(source, /username:\s*draft\.username/);
  assert.doesNotMatch(source, /setDraft\(\(value\) => \(\{ \.\.\.value, username/);
  assert.match(source, /adminApi\.updateProfile/);
  assert.match(source, /Lưu hồ sơ quản trị/);
  assert.match(source, /Định danh quản trị nội bộ/);
});
