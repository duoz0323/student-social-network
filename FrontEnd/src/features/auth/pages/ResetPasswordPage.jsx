import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import AuthEntryLayout from '../components/AuthEntryLayout.jsx';
import NewPasswordForm from '../components/NewPasswordForm.jsx';
import { PASSWORD_POLICY, PASSWORD_RECOVERY_STEP } from '../constants/passwordRecoveryConstants.js';
import { usePasswordRecovery } from '../hooks/usePasswordRecovery.js';

const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/;

export default function ResetPasswordPage() {
  const recovery = usePasswordRecovery();
  const navigate = useNavigate();
  const [localErrors, setLocalErrors] = useState({});
  if (recovery.flow.step !== PASSWORD_RECOVERY_STEP.RESET || !recovery.flow.resetAuthorization) return <Navigate to="/forgot-password" replace />;

  async function complete(form) {
    const errors = {};
    if (form.newPassword.length < PASSWORD_POLICY.minLength || form.newPassword.length > PASSWORD_POLICY.maxLength || !STRONG_PASSWORD.test(form.newPassword)) errors.newPassword = 'Mật khẩu phải dài 8–72 ký tự và có đủ chữ thường, chữ hoa, chữ số, ký tự đặc biệt.';
    if (form.newPassword !== form.confirmPassword) errors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    if (Object.keys(errors).length) { setLocalErrors(errors); return; }
    setLocalErrors({});
    try {
      await recovery.complete(form);
      navigate('/login', { replace: true, state: { reason: 'PASSWORD_RESET_SUCCESS' } });
    } catch (error) {
      if (error?.code === 'REQUEST_TIMEOUT' || error?.code === 'NETWORK_ERROR') navigate('/login', { replace: true, state: { reason: 'PASSWORD_RESET_OUTCOME_UNKNOWN' } });
    }
  }

  function clearFieldError(fieldName) {
    setLocalErrors((current) => {
      if (!current[fieldName]) return current;
      const next = { ...current };
      delete next[fieldName];
      return next;
    });
    recovery.clearError();
  }

  return <AuthEntryLayout title="Đặt lại mật khẩu"><NewPasswordForm onSubmit={complete} onFieldChange={clearFieldError} disabled={recovery.isSubmitting} error={recovery.error} fieldErrors={{ ...recovery.fieldErrors, ...localErrors }} /></AuthEntryLayout>;
}
