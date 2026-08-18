import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const dialogSource = readFileSync(new URL('../src/features/auth/components/LinkEmailDialog.jsx', import.meta.url), 'utf8');
const pageSource = readFileSync(new URL('../src/features/auth/pages/AuthProvidersPage.jsx', import.meta.url), 'utf8');

// Lỗi bắt đầu link Email phải nằm trong modal để không bị lớp overlay che khuất.
test('modal liên kết Email nhận và hiển thị lỗi bằng vùng alert', () => {
  assert.match(dialogSource, /error, onClearError/);
  assert.match(dialogSource, /role="alert"/);
  assert.match(dialogSource, /aria-invalid=\{Boolean\(error\)\}/);
  assert.match(pageSource, /error=\{actions\.error\}/);
});

// Khi modal đang mở, cùng một lỗi không được render lặp lại ở trang nền.
test('trang phương thức đăng nhập ẩn actions error phía sau modal Email', () => {
  assert.match(pageSource, /!passwordMode && !showEmailDialog \? actions\.error/);
  assert.match(pageSource, /onClearError=\{actions\.clearMessages\}/);
});
