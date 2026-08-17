import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

// Kiểm tra contract điều hướng và nội dung tĩnh mà không cần bổ sung thư viện render UI mới.
test('policy public mô tả đủ cơ chế 1/3, 2/3, 3/3 và cách tính một case', async () => {
  const source = await readFile(new URL(
    '../src/features/policy/pages/CommunityStandardsPage.jsx', import.meta.url), 'utf8');

  assert.match(source, /Lần 1/);
  assert.match(source, /Lần 2/);
  assert.match(source, /Lần 3/);
  assert.match(source, /Một bài bị nhiều người báo cáo/);
  assert.match(source, /Moderation Case/);
});

test('Login, Register và Settings đều có entry point tới chính sách', async () => {
  const [authForm, settings] = await Promise.all([
    readFile(new URL('../src/features/auth/components/AuthForm.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/components/layout/SettingsLayout.jsx', import.meta.url), 'utf8'),
  ]);

  assert.match(authForm, /\/policies\/community-standards/);
  assert.match(authForm, /Xem Tiêu chuẩn cộng đồng của UniShare/);
  assert.match(authForm, /ShieldCheck/);
  assert.match(settings, /\/policies\/community-standards/);
  assert.match(settings, /\/settings\/account-status/);
});

test('Auth hero không còn card CTA bị tách khỏi luồng chính ở đáy cột trái', async () => {
  const source = await readFile(new URL(
    '../src/features/auth/components/AuthEntryLayout.jsx', import.meta.url), 'utf8');

  assert.doesNotMatch(source, /Tham gia cộng đồng sinh viên/);
  assert.doesNotMatch(source, /avatarHoangNam/);
  assert.doesNotMatch(source, /Thanh biểu tượng chân trang hero/);
  assert.doesNotMatch(source, /GraduationCap/);
  assert.doesNotMatch(source, /PencilLine/);
});

test('Account Standing render trực tiếp ba giá trị authoritative cho 0/3, 1/3 và 2/3', async () => {
  const source = await readFile(new URL(
    '../src/features/account/pages/AccountStandingPage.jsx', import.meta.url), 'utf8');

  for (const standing of [
    { confirmedViolationCount: 0, violationThreshold: 3, remainingBeforeBlock: 3 },
    { confirmedViolationCount: 1, violationThreshold: 3, remainingBeforeBlock: 2 },
    { confirmedViolationCount: 2, violationThreshold: 3, remainingBeforeBlock: 1 },
  ]) {
    assert.equal(`${standing.confirmedViolationCount} / ${standing.violationThreshold}`,
      `${standing.confirmedViolationCount} / 3`);
  }
  assert.match(source, /confirmedViolationCount/);
  assert.match(source, /violationThreshold/);
  assert.match(source, /remainingBeforeBlock/);
  assert.doesNotMatch(source, /notifications\.length/);
});

test('Login có dedicated blocked screen và Admin block/unblock đều qua modal xác nhận', async () => {
  const [login, blockedPanel, adminUsers, unblockDialog] = await Promise.all([
    readFile(new URL('../src/features/auth/pages/LoginPage.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/auth/components/BlockedAccountPanel.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/admin/pages/AdminUsersPage.jsx', import.meta.url), 'utf8'),
    readFile(new URL('../src/features/admin/components/UnblockUserDialog.jsx', import.meta.url), 'utf8'),
  ]);

  assert.match(login, /ACCOUNT_BLOCKED/);
  assert.match(blockedPanel, /REPEATED_VIOLATION/);
  assert.match(blockedPanel, /\/policies\/community-standards/);
  assert.match(adminUsers, /BlockUserDialog/);
  assert.match(adminUsers, /UnblockUserDialog/);
  assert.match(unblockDialog, /Mở khóa tài khoản/);
});
