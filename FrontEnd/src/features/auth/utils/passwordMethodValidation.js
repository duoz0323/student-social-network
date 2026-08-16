export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/;

/** Validation UX phía client; Backend vẫn là hàng rào quyết định cuối cùng. */
export function validatePasswordMethodForm(mode, form) {
  const changing = mode === 'CHANGE';
  const sameAsCurrent = changing
    && Boolean(form.currentPassword)
    && Boolean(form.newPassword)
    && form.currentPassword === form.newPassword;
  const confirmationMismatch = Boolean(form.confirmPassword)
    && form.newPassword !== form.confirmPassword;

  return {
    sameAsCurrent,
    confirmationMismatch,
    valid: (!changing || Boolean(form.currentPassword))
      && PASSWORD_PATTERN.test(form.newPassword)
      && !sameAsCurrent
      && form.newPassword === form.confirmPassword,
  };
}
