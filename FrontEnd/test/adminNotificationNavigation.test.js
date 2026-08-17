import test from 'node:test';
import assert from 'node:assert/strict';
import { getAdminNotificationPath } from '../src/features/admin/notifications/adminNotificationNavigation.js';

test('map reference type sang route quản trị allowlist', () => {
  assert.equal(getAdminNotificationPath({ referenceType: 'MODERATION_CASE', referenceId: 12 }), '/admin/reports/12');
  assert.equal(getAdminNotificationPath({ referenceType: 'PROFILE_REPORT', referenceId: 8 }), '/admin/profile-reports/8');
  assert.equal(getAdminNotificationPath({ referenceType: 'POST', referenceId: 5 }), '/admin/posts/5');
  assert.equal(getAdminNotificationPath({ referenceType: 'ADMIN', referenceId: 2 }), '/admin/admins');
  assert.equal(getAdminNotificationPath({ type: 'MODERATION_SUGGESTION_CREATED', referenceType: 'MODERATION_SUGGESTION', referenceId: 9 }), '/admin/moderation-suggestions/9');
  assert.equal(getAdminNotificationPath({ type: 'MODERATION_SUGGESTION_ACCEPTED', referenceType: 'MODERATION_SUGGESTION', referenceId: 9 }), '/admin/collaborator/moderation-suggestions?highlight=9');
});

test('không tin reference type hoặc URL tùy ý', () => {
  assert.equal(getAdminNotificationPath({ referenceType: 'https://evil.example', referenceId: 1 }), null);
  assert.equal(getAdminNotificationPath({ referenceType: null, referenceId: null }), null);
});
