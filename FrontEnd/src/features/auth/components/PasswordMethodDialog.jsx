import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { validatePasswordMethodForm } from '../utils/passwordMethodValidation.js';
import PasswordVisibilityIcon from './PasswordVisibilityIcon.jsx';

function PasswordInput({ label, value, visible, autoComplete, disabled, invalid = false, onChange, onToggle }) {
  return <label className="block text-sm font-semibold text-[var(--app-text)]">{label}<span className="relative mt-2 block"><input type={visible ? 'text' : 'password'} value={value} autoComplete={autoComplete} disabled={disabled} aria-invalid={invalid} onChange={(event) => onChange(event.target.value)} className={`h-11 w-full rounded-xl border bg-[var(--app-surface)] px-3 pr-11 text-sm outline-none focus:ring-2 ${invalid ? 'border-red-500 focus:ring-red-500/25' : 'border-[var(--app-border-strong)] focus:ring-violet-500/30'}`} /><button type="button" onClick={onToggle} className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--app-muted)]" aria-label={visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}><PasswordVisibilityIcon visible={visible} /></button></span></label>;
}

/** Dialog chỉ giữ password trong local component state và xóa khi unmount. */
export default function PasswordMethodDialog({ mode, open, busy, error = '', onClearError, onClose, onSubmit }) {
  const changing = mode === 'CHANGE';
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [visible, setVisible] = useState({ currentPassword: false, newPassword: false, confirmPassword: false });
  const validation = validatePasswordMethodForm(mode, form);
  const setField = (name, value) => {
    onClearError?.();
    setForm((current) => ({ ...current, [name]: value }));
  };
  const toggle = (name) => setVisible((current) => ({ ...current, [name]: !current[name] }));
  return <Modal open={open} title={changing ? 'Đổi mật khẩu' : 'Thiết lập mật khẩu'} onClose={busy ? undefined : onClose} footer={<><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy</Button><Button disabled={busy || !validation.valid} onClick={() => onSubmit(form)}>{busy ? 'Đang xử lý...' : changing ? 'Đổi mật khẩu' : 'Thiết lập mật khẩu'}</Button></>}>
    <div className="space-y-4">
      {changing ? <PasswordInput label="Mật khẩu hiện tại" value={form.currentPassword} visible={visible.currentPassword} autoComplete="current-password" disabled={busy} onChange={(value) => setField('currentPassword', value)} onToggle={() => toggle('currentPassword')} /> : null}
      <PasswordInput label="Mật khẩu mới" value={form.newPassword} visible={visible.newPassword} autoComplete="new-password" disabled={busy} invalid={validation.sameAsCurrent} onChange={(value) => setField('newPassword', value)} onToggle={() => toggle('newPassword')} />
      {validation.sameAsCurrent ? <p className="text-xs font-semibold text-red-600">Mật khẩu mới phải khác mật khẩu hiện tại.</p> : null}
      <PasswordInput label="Xác nhận mật khẩu mới" value={form.confirmPassword} visible={visible.confirmPassword} autoComplete="new-password" disabled={busy} invalid={validation.confirmationMismatch} onChange={(value) => setField('confirmPassword', value)} onToggle={() => toggle('confirmPassword')} />
      <p className="text-xs leading-5 text-[var(--app-muted)]">8–72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.</p>
      {validation.confirmationMismatch ? <p className="text-xs font-semibold text-red-600">Mật khẩu xác nhận không khớp.</p> : null}
      {error ? <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm font-semibold text-red-700">{error}</div> : null}
    </div>
  </Modal>;
}
