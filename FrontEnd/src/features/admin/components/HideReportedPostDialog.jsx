import { useState } from 'react';
import { EyeOff } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { ADMIN_POST_HIDE_REASONS, isAdminPostHideReason } from '../constants/adminPostHideReasons.js';

export default function HideReportedPostDialog({ onClose, onConfirm }) {
  const [reasonCode, setReasonCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  function close() {
    if (!submitting) onClose();
  }

  async function confirm() {
    if (!isAdminPostHideReason(reasonCode) || submitting) return;
    setSubmitting(true);
    setError('');
    try {
      // Chỉ gửi mã enum đã được Backend công bố, không gửi nhãn hoặc kết luận tự do.
      await onConfirm(reasonCode);
    } catch (requestError) {
      setError(requestError.message || 'Không thể ẩn bài viết.');
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open
      title="Chọn lý do vi phạm"
      size="md"
      onClose={close}
      footer={(
        <div className="flex w-full gap-3">
          <Button className="flex-1" variant="secondary" disabled={submitting} onClick={close}>
            Hủy
          </Button>
          <Button className="flex-1 gap-2" disabled={!reasonCode || submitting} onClick={confirm}>
            <EyeOff size={16} /> {submitting ? 'Đang ẩn...' : 'Ẩn bài'}
          </Button>
        </div>
      )}
    >
      <div className="mb-4 rounded-xl bg-amber-50 p-3 text-sm leading-5 text-amber-900">
        Chọn lý do phù hợp để ẩn bài viết
      </div>
      <fieldset disabled={submitting}>
        <legend className="mb-2 text-sm font-semibold text-[var(--app-text)]">
          Lý do vi phạm <span className="text-red-600">*</span>
        </legend>
        <div className="grid gap-2 sm:grid-cols-2">
          {ADMIN_POST_HIDE_REASONS.map((reason) => (
            <label
              key={reason.value}
              className={`flex cursor-pointer items-center justify-between rounded-xl border px-3 py-2.5 text-sm transition ${
                reasonCode === reason.value
                  ? 'border-[var(--app-text)] bg-[var(--app-surface-soft)] font-semibold'
                  : 'border-[var(--app-border)] hover:bg-[var(--app-surface-soft)]'
              }`}
            >
              <span>{reason.label}</span>
              <input
                type="radio"
                name="moderation-case-hide-reason"
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
