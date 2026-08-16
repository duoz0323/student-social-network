import { useEffect, useMemo, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

function secondsUntil(value, now) {
  const timestamp = value ? new Date(value).getTime() : 0;
  return Math.max(0, Math.ceil((timestamp - now) / 1000));
}

export default function LinkAuthOtpDialog({ flow, busy, onClose, onVerifyOtp, onComplete, onResend }) {
  const [code, setCode] = useState('');
  const [step, setStep] = useState('OTP');
  const [passwords, setPasswords] = useState({ newPassword: '', confirmPassword: '' });
  const [now, setNow] = useState(0);
  useEffect(() => {
    if (!flow) return undefined;
    const initialTimer = window.setTimeout(() => setNow(Date.now()), 0);
    const timer = window.setInterval(() => setNow(Date.now()), 1000);
    return () => { window.clearTimeout(initialTimer); window.clearInterval(timer); };
  }, [flow]);
  const countdown = useMemo(() => ({
    otp: secondsUntil(flow?.otpExpiresAt, now),
    resend: secondsUntil(flow?.resendAvailableAt, now),
    challenge: secondsUntil(flow?.challengeExpiresAt, now),
  }), [flow, now]);
  if (!flow) return null;
  const challengeExpired = countdown.challenge === 0;
  const passwordValid = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,72}$/.test(passwords.newPassword)
    && passwords.newPassword === passwords.confirmPassword;
  return (
    <Modal open title={step === 'OTP' ? 'Xác minh OTP liên kết' : 'Thiết lập mật khẩu'} onClose={busy ? undefined : onClose} footer={(
      <><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy flow</Button><Button disabled={busy || challengeExpired || (step === 'OTP' ? !/^\d{6}$/.test(code) : !passwordValid)} onClick={async () => { if (step === 'OTP') { const verified = await onVerifyOtp(code); if (verified) setStep('PASSWORD'); } else await onComplete(passwords); }}>{busy ? 'Đang hoàn tất...' : step === 'OTP' ? 'Xác minh' : 'Liên kết email'}</Button></>
    )}>
      {step === 'OTP' ? <>
      <p className="text-sm text-[var(--app-muted)]">Mã được gửi tới <strong className="text-[var(--app-text)]">{flow.maskedIdentifier}</strong>.</p>
      <input
        value={code}
        onChange={(event) => setCode(event.target.value.replace(/\D/g, '').slice(0, 6))}
        inputMode="numeric"
        autoComplete="one-time-code"
        disabled={busy || challengeExpired}
        aria-label="Mã OTP liên kết"
        className="mt-4 h-12 w-full rounded-xl border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-4 text-center text-xl font-bold tracking-[0.35em] outline-none focus:ring-2 focus:ring-violet-500/30"
      />
      <div className="mt-4 space-y-1 text-xs text-[var(--app-muted)]">
        <p>OTP còn hiệu lực: {countdown.otp} giây.</p>
        <p>Challenge còn hiệu lực: {countdown.challenge} giây.</p>
      </div>
      <Button variant="secondary" className="mt-4 w-full" disabled={busy || challengeExpired || countdown.resend > 0} onClick={onResend}>
        {countdown.resend > 0 ? `Gửi lại sau ${countdown.resend} giây` : 'Gửi lại OTP'}
      </Button>
      </> : <div className="space-y-4">
        <p className="text-sm leading-6 text-[var(--app-muted)]">Mật khẩu UniShare chỉ được lưu dạng hash và phải có 8–72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.</p>
        <input type="password" autoComplete="new-password" value={passwords.newPassword} onChange={(event) => setPasswords((current) => ({ ...current, newPassword: event.target.value }))} placeholder="Mật khẩu mới" disabled={busy} className="h-11 w-full rounded-xl border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-3 text-sm outline-none focus:ring-2 focus:ring-violet-500/30" />
        <input type="password" autoComplete="new-password" value={passwords.confirmPassword} onChange={(event) => setPasswords((current) => ({ ...current, confirmPassword: event.target.value }))} placeholder="Xác nhận mật khẩu" disabled={busy} className="h-11 w-full rounded-xl border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-3 text-sm outline-none focus:ring-2 focus:ring-violet-500/30" />
        {passwords.confirmPassword && passwords.newPassword !== passwords.confirmPassword ? <p className="text-xs font-semibold text-red-600">Mật khẩu xác nhận không khớp.</p> : null}
      </div>}
      {challengeExpired ? <p className="mt-3 text-sm font-semibold text-red-600">Phiên liên kết đã hết hạn. Hãy đóng dialog và bắt đầu lại.</p> : null}
    </Modal>
  );
}
