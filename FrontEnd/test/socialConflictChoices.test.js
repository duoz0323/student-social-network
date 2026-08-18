import test from 'node:test';
import assert from 'node:assert/strict';
import {
  SOCIAL_CONFLICT_ACTIONS,
  SOCIAL_CONFLICT_TYPES,
  allowedSocialConflictActions,
} from '../src/features/auth/services/socialConflictPolicy.js';

test('Facebook email conflict cho phép đăng nhập account cũ hoặc tạo account riêng', () => {
  assert.deepEqual(allowedSocialConflictActions(
    SOCIAL_CONFLICT_TYPES.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
    'FACEBOOK',
  ), [
    SOCIAL_CONFLICT_ACTIONS.LOGIN_EXISTING_ACCOUNT,
    SOCIAL_CONFLICT_ACTIONS.CONTINUE_WITH_SEPARATE_ACCOUNT,
  ]);
});

test('Google không nhận action tạo account riêng chỉ dành cho Facebook', () => {
  const actions = allowedSocialConflictActions(
    SOCIAL_CONFLICT_TYPES.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER,
    'GOOGLE',
  );
  assert.deepEqual(actions, [
    SOCIAL_CONFLICT_ACTIONS.LOGIN_EXISTING_ACCOUNT,
    SOCIAL_CONFLICT_ACTIONS.START_ACCOUNT_RECOVERY,
  ]);
  assert.equal(actions.includes(SOCIAL_CONFLICT_ACTIONS.CONTINUE_WITH_SEPARATE_ACCOUNT), false);
});
