const CONFLICT_KEY = 'unishare.auth.social-conflict';

export const socialConflictService = Object.freeze({
  save(details) {
    if (!details?.flowToken || details.flowType !== 'SOCIAL_CONFLICT') return null;
    const state = {
      flowToken: details.flowToken,
      flowType: details.flowType,
      conflictType: details.conflictType,
      allowedActions: Array.isArray(details.allowedActions) ? details.allowedActions : [],
      expiresAt: Date.now() + Number(details.expiresIn || 0) * 1000,
    };
    // Chỉ lưu challenge đã được Backend phát hành; không lưu Google credential hoặc provider identity.
    sessionStorage.setItem(CONFLICT_KEY, JSON.stringify(state));
    return state;
  },

  get() {
    try {
      const value = sessionStorage.getItem(CONFLICT_KEY);
      return value ? JSON.parse(value) : null;
    } catch {
      sessionStorage.removeItem(CONFLICT_KEY);
      return null;
    }
  },

  clear() {
    sessionStorage.removeItem(CONFLICT_KEY);
  },
});
