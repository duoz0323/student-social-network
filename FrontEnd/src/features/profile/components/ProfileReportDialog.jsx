import { useState } from 'react';
import { CheckCircle2 } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { socialApi } from '../../../api/index.js';
import { PROFILE_REPORT_REASONS } from '../constants/profileReportReasons.js';

/** Luồng chọn lý do và gửi báo cáo trang cá nhân, không tiết lộ reporter cho target. */
export default function ProfileReportDialog({ open, user, onClose }) {
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');

  function close() {
    if (submitting) return;
    setReason('');
    setSubmitted(false);
    setError('');
    onClose();
  }

  async function submit() {
    if (!reason || submitting) return;
    setSubmitting(true);
    setError('');
    try {
      await socialApi.reportProfile(user.id, { reason });
      setSubmitted(true);
    } catch (requestError) {
      setError(requestError.message || 'Không thể gửi báo cáo. Vui lòng thử lại.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open={open}
      title={submitted ? 'Đã gửi báo cáo' : 'Báo cáo trang cá nhân'}
      onClose={close}
      footer={submitted ? (
        <Button className="w-full" onClick={close}>Xong</Button>
      ) : (
        <>
          <Button variant="secondary" disabled={submitting} onClick={close}>Hủy</Button>
          <Button disabled={!reason || submitting} loading={submitting} onClick={submit}>Gửi báo cáo</Button>
        </>
      )}
    >
      {submitted ? (
        <div className="py-5 text-center">
          <CheckCircle2 className="mx-auto text-emerald-600" size={46} />
          <p className="mt-4 font-semibold text-[var(--app-text)]">Cảm ơn bạn đã báo cáo.</p>
          <p className="mt-1 text-sm text-[var(--app-muted)]">Quản trị viên sẽ xem xét trang cá nhân của {user.displayName}.</p>
        </div>
      ) : (
        <div>
          <p className="mb-4 text-sm text-[var(--app-muted)]">Tại sao bạn muốn báo cáo trang cá nhân này?</p>
          {error ? <p className="mb-3 rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p> : null}
          <div className="space-y-2">
            {PROFILE_REPORT_REASONS.map((item) => (
              <label key={item.value} className="flex cursor-pointer items-center gap-3 rounded-xl border border-[var(--app-border)] px-4 py-3 hover:bg-[var(--app-surface-soft)]">
                <input
                  type="radio"
                  name="profile-report-reason"
                  value={item.value}
                  checked={reason === item.value}
                  disabled={submitting}
                  onChange={(event) => setReason(event.target.value)}
                />
                <span className="text-sm font-medium text-[var(--app-text)]">{item.label}</span>
              </label>
            ))}
          </div>
          <p className="mt-4 text-center text-xs text-[var(--app-muted)]">Người bị báo cáo sẽ không biết ai đã gửi báo cáo.</p>
        </div>
      )}
    </Modal>
  );
}
