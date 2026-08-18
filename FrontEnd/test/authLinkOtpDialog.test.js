import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const dialogSource = readFileSync(new URL('../src/features/auth/components/LinkAuthOtpDialog.jsx', import.meta.url), 'utf8');

// UI chỉ giải thích thời hạn phiên bằng ngôn ngữ người dùng, không lộ thuật ngữ kỹ thuật Challenge.
test('dialog OTP liên kết ẩn countdown Challenge và hiển thị thời hạn 15 phút', () => {
  assert.doesNotMatch(dialogSource, /Challenge còn hiệu lực/);
  assert.match(dialogSource, /Phiên liên kết có hiệu lực tối đa 15 phút/);
});
