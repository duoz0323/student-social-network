import { useState } from 'react';
import { ShieldAlert } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { ADMIN_USER_BLOCK_REASONS, isAdminUserBlockReason } from '../constants/adminUserBlockReasons.js';

export default function BlockUserDialog({ user, onClose, onConfirm }) {
  const [reasonCode, setReasonCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const userName = user?.displayName?.trim() || user?.email || 'người dùng này';

  function close() {
    if (!submitting) onClose();
  }

  async function confirm() {
    if (!isAdminUserBlockReason(reasonCode) || submitting) return;
    setSubmitting(true);
    setError('');
    try {
      // Chỉ gửi mã enum đã được Backend công bố, không gửi label hoặc lý do tự do.
      await onConfirm(reasonCode);
    } catch (requestError) {
      setError(requestError.message);
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open
      title="Khóa tài khoản người dùng"
      size="sm"
      onClose={close}
      footer={(
        <div className="flex w-full gap-3">
          <Button className="flex-1" variant="secondary" disabled={submitting} onClick={close}>
            Hủy
          </Button>
          <Button
            className="flex-1"
            variant="danger"
            disabled={!reasonCode || submitting}
            onClick={confirm}
          >
            {submitting ? 'Đang khóa...' : 'Xác nhận khóa'}
          </Button>
        </div>
      )}
    >
      <div className="mb-4 flex gap-3 rounded-xl bg-red-50 p-3 text-red-800">
        <ShieldAlert className="mt-0.5 h-5 w-5 shrink-0" aria-hidden="true" />
        <p className="text-sm leading-5">
          Chọn lý do khóa tài khoản <strong>{userName}</strong>. Người dùng sẽ không thể tiếp tục đăng nhập.
        </p>
      </div>

      <fieldset disabled={submitting}>
        <legend className="mb-2 text-sm font-semibold text-[var(--app-text)]">
          Lý do khóa tài khoản <span className="text-red-600">*</span>
        </legend>
        <div className="space-y-2">
          {ADMIN_USER_BLOCK_REASONS.map((reason) => (
            <label
              key={reason.value}
              className={`flex cursor-pointer items-center justify-between rounded-xl border px-4 py-3 text-sm transition ${
                reasonCode === reason.value
                  ? 'border-[var(--app-text)] bg-[var(--app-surface-soft)] font-semibold'
                  : 'border-[var(--app-border)] hover:bg-[var(--app-surface-soft)]'
              }`}
            >
              <span>{reason.label}</span>
              <input
                type="radio"
                name="admin-user-block-reason"
                value={reason.value}
                checked={reasonCode === reason.value}
                onChange={(event) => setReasonCode(event.target.value)}
                className="h-4 w-4 accent-red-600"
              />
            </label>
          ))}
        </div>
      </fieldset>

      {error ? <p className="app-error mt-4 rounded-xl p-3 text-sm" role="alert">{error}</p> : null}
    </Modal>
  );
}
