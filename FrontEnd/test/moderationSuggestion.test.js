import assert from 'node:assert/strict';
import test from 'node:test';
import {
  MODERATION_SUGGESTION_REASONS,
  getSuggestionReasonLabel,
  getSuggestionActorRoles,
  getSuggestionStatusLabel,
} from '../src/features/admin/moderation/moderationSuggestion.js';

test('dùng chung nhãn lý do và trạng thái giữa Collaborator với Moderator', () => {
  assert.equal(MODERATION_SUGGESTION_REASONS.length, 6);
  assert.equal(getSuggestionReasonLabel('SCAM_SUSPECTED'), 'Nghi ngờ lừa đảo');
  assert.equal(getSuggestionStatusLabel('PENDING'), 'Chờ xử lý');
  assert.equal(getSuggestionStatusLabel('ACCEPTED'), 'Đã chấp nhận');
  assert.equal(getSuggestionStatusLabel('REJECTED'), 'Đã từ chối');
});

test('hiển thị role hiện tại và fallback Cộng tác viên cho người đề xuất', () => {
  assert.deepEqual(getSuggestionActorRoles({ roles: ['COLLABORATOR', 'MODERATOR'] }, 'COLLABORATOR'),
    ['COLLABORATOR', 'MODERATOR']);
  assert.deepEqual(getSuggestionActorRoles({ roles: [] }, 'COLLABORATOR'), ['COLLABORATOR']);
  assert.deepEqual(getSuggestionActorRoles(null, undefined), []);
});
