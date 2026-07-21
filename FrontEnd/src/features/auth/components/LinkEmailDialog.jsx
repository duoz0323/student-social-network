import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

/** Thu thập email mới; Backend chỉ liên kết sau khi OTP được xác minh. */
export default function LinkEmailDialog({ open, busy, onClose, onSubmit }) {
  const [email, setEmail] = useState('');
  return (
    <Modal open={open} title="Liên kết Email" onClose={busy ? undefined : onClose} footer={(
      <><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy</Button><Button disabled={busy || !email.trim()} onClick={() => onSubmit(email)}>{busy ? 'Đang gửi...' : 'Gửi mã OTP'}</Button></>
    )}>
      <label className="block text-sm font-semibold text-[var(--app-text)]">
        Email mới
        <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" placeholder="student@example.com" disabled={busy} className="mt-2 h-11 w-full rounded-xl border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-3 text-sm outline-none focus:ring-2 focus:ring-violet-500/30" />
      </label>
      <p className="mt-3 text-xs leading-5 text-[var(--app-muted)]">Email chỉ được đánh dấu đã liên kết sau khi OTP được Backend xác minh thành công.</p>
    </Modal>
  );
}
