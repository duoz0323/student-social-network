import test from 'node:test';
import assert from 'node:assert/strict';
import { hasCollaboratorBadge } from '../src/components/common/publicIdentityBadge.js';

test('chỉ render badge khi Backend trả COLLABORATOR', () => {
  assert.equal(hasCollaboratorBadge(['COLLABORATOR']), true);
  assert.equal(hasCollaboratorBadge([]), false);
  assert.equal(hasCollaboratorBadge(undefined), false);
});
