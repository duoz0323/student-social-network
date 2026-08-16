import assert from 'node:assert/strict';
import test from 'node:test';
import { isRequestCanceled, normalizeApiError } from '../src/api/apiError.js';

test('lỗi AbortController sau khi chuẩn hóa vẫn được nhận diện là request đã hủy', () => {
  const canceledError = new Error('canceled');
  canceledError.name = 'CanceledError';
  canceledError.code = 'ERR_CANCELED';

  const normalizedError = normalizeApiError(canceledError);

  assert.equal(normalizedError.name, 'ApiError');
  assert.equal(normalizedError.code, 'ERR_CANCELED');
  assert.equal(isRequestCanceled(normalizedError), true);
});

test('lỗi API thông thường không bị xem nhầm là request đã hủy', () => {
  assert.equal(isRequestCanceled(new Error('server failed')), false);
});
