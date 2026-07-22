import { useEffect, useMemo, useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

function secondsUntil(value, now) {
  const timestamp = value ? new Date(value).getTime() : 0;
  return Math.max(0, Math.ceil((timestamp - now) / 1000));
}

export default function LinkAuthOtpDialog({ flow, busy, onClose, onVerify, onResend }) {
  const [code, setCode] = useState('');
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
  return (
    <Modal open title="Xác minh OTP liên kết" onClose={busy ? undefined : onClose} footer={(
      <><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy flow</Button><Button disabled={busy || challengeExpired || !/^\d{6}$/.test(code)} onClick={() => onVerify(code)}>{busy ? 'Đang xác minh...' : 'Xác minh'}</Button></>
    )}>
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
      {challengeExpired ? <p className="mt-3 text-sm font-semibold text-red-600">Phiên liên kết đã hết hạn. Hãy đóng dialog và bắt đầu lại.</p> : null}
    </Modal>
  );
}
