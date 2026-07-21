import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout.jsx';
import RecoveryIdentifierForm from '../components/RecoveryIdentifierForm.jsx';
import RecoveryOtpForm from '../components/RecoveryOtpForm.jsx';
import { PASSWORD_RECOVERY_STEP } from '../constants/passwordRecoveryConstants.js';
import { usePasswordRecovery } from '../hooks/usePasswordRecovery.js';

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function ForgotPasswordPage() {
  const recovery = usePasswordRecovery();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [localError, setLocalError] = useState('');

  async function start() {
    const normalized = email.trim();
    if (!EMAIL_PATTERN.test(normalized)) {
      setLocalError('Email không hợp lệ.');
      return;
    }
    setLocalError('');
    try { await recovery.start(normalized); } catch { /* Hook ánh xạ lỗi an toàn. */ }
  }

  async function verify(code) {
    try { await recovery.verify(code); navigate('/reset-password'); } catch { /* Giữ bước OTP khi còn hiệu lực. */ }
  }

  return <AuthLayout>{recovery.flow.step === PASSWORD_RECOVERY_STEP.OTP && recovery.flow.challenge
    ? <RecoveryOtpForm key={recovery.flow.challenge.recoveryFlowToken} challenge={recovery.flow.challenge}
        onVerify={verify} onResend={() => recovery.resend().catch(() => {})} onRestart={recovery.clear}
        disabled={recovery.isSubmitting} error={recovery.error} fieldError={recovery.fieldErrors.code} />
    : <RecoveryIdentifierForm email={email} onChange={(value) => { setEmail(value); setLocalError(''); recovery.clearError(); }}
        onSubmit={start} disabled={recovery.isSubmitting} error={recovery.error}
        fieldError={localError || recovery.fieldErrors.email} />}</AuthLayout>;
}
