import { authApi } from '../../../api/index.js';
import {
  SOCIAL_CONFLICT_TYPES,
  allowedSocialConflictActions,
} from './socialConflictPolicy.js';

export { SOCIAL_CONFLICT_ACTIONS, SOCIAL_CONFLICT_TYPES } from './socialConflictPolicy.js';

const CONFLICT_KEY = 'unishare.auth.social-conflict';

const PROVIDERS = new Set(['GOOGLE', 'FACEBOOK']);

function normalizeState(details, context = {}) {
  const conflictType = details?.conflictType;
  const expiresIn = Number(details?.expiresIn);
  const provider = String(context.provider ?? '').toUpperCase();
  const allowedForType = allowedSocialConflictActions(conflictType, provider);
  if (!details?.flowToken || details.flowType !== 'SOCIAL_CONFLICT' || !allowedForType || !PROVIDERS.has(provider) || !Number.isFinite(expiresIn) || expiresIn <= 0) return null;

  // Chỉ giữ giao của action Backend trả và whitelist theo đúng conflict type production.
  const backendActions = new Set(Array.isArray(details.allowedActions) ? details.allowedActions : []);
  const allowedActions = allowedForType.filter((action) => backendActions.has(action));
  if (allowedActions.length === 0) return null;
  const pendingConflict = conflictType !== SOCIAL_CONFLICT_TYPES.ACTIVE_EMAIL_MATCH_UNLINKED_PROVIDER;
  return {
    provider,
    socialConflictFlowToken: details.flowToken,
    flowType: 'SOCIAL_CONFLICT',
    conflictType,
    allowedActions,
    maskedPendingIdentifier: pendingConflict ? context.maskedPendingIdentifier || null : null,
    pendingIdentifierType: pendingConflict ? context.pendingIdentifierType || null : null,
    expiresAt: Date.now() + expiresIn * 1000,
  };
}

function isValidStoredState(state) {
  if (!state || state.flowType !== 'SOCIAL_CONFLICT' || !state.socialConflictFlowToken || !PROVIDERS.has(state.provider)) return false;
  const allowedForType = allowedSocialConflictActions(state.conflictType, state.provider);
  return Boolean(allowedForType) && Array.isArray(state.allowedActions) && state.allowedActions.length > 0
    && state.allowedActions.every((action) => allowedForType.includes(action)) && Number.isFinite(Number(state.expiresAt));
}

export const socialConflictService = Object.freeze({
  setConflict(details, context) {
    const state = normalizeState(details, context);
    if (!state) return null;
    // Challenge được lưu theo session; không lưu provider credential, registration token hay raw response.
    sessionStorage.setItem(CONFLICT_KEY, JSON.stringify(state));
    return state;
  },

  restoreConflict() {
    try {
      const raw = sessionStorage.getItem(CONFLICT_KEY);
      if (!raw) return { conflict: null, reason: 'MISSING' };
      const state = JSON.parse(raw);
      if (!isValidStoredState(state)) {
        sessionStorage.removeItem(CONFLICT_KEY);
        return { conflict: null, reason: 'INVALID' };
      }
      if (Number(state.expiresAt) <= Date.now()) {
        sessionStorage.removeItem(CONFLICT_KEY);
        return { conflict: null, reason: 'EXPIRED' };
      }
      return { conflict: state, reason: null };
    } catch {
      sessionStorage.removeItem(CONFLICT_KEY);
      return { conflict: null, reason: 'INVALID' };
    }
  },

  isActionAllowed(conflict, action) {
    return Boolean(conflict)
      && allowedSocialConflictActions(conflict.conflictType, conflict.provider).includes(action)
      && conflict.allowedActions.includes(action);
  },

  async resolveConflict(conflict, action, signal) {
    if (!this.isActionAllowed(conflict, action) || !conflict.socialConflictFlowToken) throw new Error('Lựa chọn xử lý Social Conflict không hợp lệ.');
    // socialConflictFlowToken chỉ đi qua header; body chỉ chứa field thuộc production DTO.
    return authApi.resolveSocialConflict({ action }, conflict.socialConflictFlowToken, signal);
  },

  clearConflict() {
    sessionStorage.removeItem(CONFLICT_KEY);
  },
});
