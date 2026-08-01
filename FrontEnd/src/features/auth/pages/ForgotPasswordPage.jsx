import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthEntryLayout from '../components/AuthEntryLayout.jsx';
import RecoveryIdentifierForm from '../components/RecoveryIdentifierForm.jsx';
import RecoveryEmailNotice from '../components/RecoveryEmailNotice.jsx';
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

  let content;
  if (recovery.flow.step === PASSWORD_RECOVERY_STEP.NOTICE && recovery.flow.challenge) {
    content = <RecoveryEmailNotice email={email.trim()} disabled={recovery.isSubmitting}
      onContinue={recovery.continueToOtp} onRestart={recovery.clear} />;
  } else if (recovery.flow.step === PASSWORD_RECOVERY_STEP.OTP && recovery.flow.challenge) {
    content = <RecoveryOtpForm key={recovery.flow.challenge.recoveryFlowToken} challenge={recovery.flow.challenge}
        onVerify={verify} onResend={() => recovery.resend().catch(() => {})} onRestart={recovery.clear}
        disabled={recovery.isSubmitting} error={recovery.error} fieldError={recovery.fieldErrors.code} />;
  } else {
    content = <RecoveryIdentifierForm email={email} onChange={(value) => { setEmail(value); setLocalError(''); recovery.clearError(); }}
        onSubmit={start} disabled={recovery.isSubmitting} error={recovery.error}
        fieldError={localError || recovery.fieldErrors.email} />;
  }

  return <AuthEntryLayout>{content}</AuthEntryLayout>;
}
