import assert from 'node:assert/strict';
import test from 'node:test';
import {
  isCurrentUserProfile,
  mergeCurrentUserProfile,
  shouldLoadCurrentProfile,
  toCurrentUserProfile,
} from './currentUserProfile.js';

test('toCurrentUserProfile chuẩn hóa dữ liệu hồ sơ từ API', () => {
  assert.deepEqual(toCurrentUserProfile({
    userId: 42,
    displayName: 'Nguyễn An',
    avatarUrl: 'https://cdn.example/avatar.png',
    dateOfBirth: '2003-04-18',
    bio: 'Sinh viên',
  }, 10), {
    id: 42,
    displayName: 'Nguyễn An',
    avatarUrl: 'https://cdn.example/avatar.png',
    birthDate: '2003-04-18',
    bio: 'Sinh viên',
    profileCompletedAt: null,
  });
});

test('mergeCurrentUserProfile đồng bộ avatar mới nhưng giữ thông tin cá nhân còn lại', () => {
  assert.deepEqual(mergeCurrentUserProfile({
    id: 42,
    displayName: 'Nguyễn An',
    avatarUrl: '',
    birthDate: '2003-04-18',
    bio: 'Sinh viên',
  }, {
    avatarUrl: 'https://cdn.example/avatar-moi.png',
  }, 42), {
    id: 42,
    displayName: 'Nguyễn An',
    avatarUrl: 'https://cdn.example/avatar-moi.png',
    birthDate: '2003-04-18',
    bio: 'Sinh viên',
    profileCompletedAt: null,
  });
});

test('không dùng hồ sơ cache của tài khoản trước cho phiên mới', () => {
  assert.equal(isCurrentUserProfile({ id: 42, displayName: 'Nguyễn An' }, 42), true);
  assert.equal(isCurrentUserProfile({ id: 42, displayName: 'Nguyễn An' }, 99), false);
});

test('Admin luôn tải hồ sơ riêng để sidebar hiển thị đúng tên tài khoản đăng nhập', () => {
  assert.equal(shouldLoadCurrentProfile('ADMIN', false), true);
  assert.equal(shouldLoadCurrentProfile('ADMIN', true), true);
  assert.equal(shouldLoadCurrentProfile('USER', false), false);
  assert.equal(shouldLoadCurrentProfile('USER', true), true);
});
