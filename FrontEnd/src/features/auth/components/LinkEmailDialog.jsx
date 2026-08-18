import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

/** Thu thập email mới; Backend chỉ liên kết sau khi OTP được xác minh. */
export default function LinkEmailDialog({ open, busy, error, onClearError, onClose, onSubmit }) {
  const [email, setEmail] = useState('');

  function changeEmail(event) {
    setEmail(event.target.value);
    if (error) onClearError?.();
  }

  return (
    <Modal open={open} title="Liên kết Email" onClose={busy ? undefined : onClose} footer={(
      <><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy</Button><Button disabled={busy || !email.trim()} onClick={() => onSubmit(email)}>{busy ? 'Đang gửi...' : 'Gửi mã OTP'}</Button></>
    )}>
      <label className="block text-sm font-semibold text-[var(--app-text)]">
        Email mới
        <input
          type="email"
          value={email}
          onChange={changeEmail}
          autoComplete="email"
          placeholder="student@example.com"
          disabled={busy}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? 'link-email-error' : 'link-email-help'}
          className={`mt-2 h-11 w-full rounded-xl border bg-[var(--app-surface)] px-3 text-sm outline-none focus:ring-2 ${error ? 'border-red-400 focus:ring-red-500/30' : 'border-[var(--app-border-strong)] focus:ring-violet-500/30'}`}
        />
      </label>
      {error ? (
        <div id="link-email-error" role="alert" className="mt-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2.5 text-sm font-semibold text-red-700">
          {error}
        </div>
      ) : (
        <p id="link-email-help" className="mt-3 text-xs leading-5 text-[var(--app-muted)]">Email chỉ được đánh dấu đã liên kết sau khi OTP được Backend xác minh thành công.</p>
      )}
    </Modal>
  );
}
