import { useState } from 'react';
import { EyeOff } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

export default function HideReportedPostDialog({ reasonLabel, onClose, onConfirm }) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  function close() {
    if (!submitting) onClose();
  }

  async function confirm() {
    if (submitting) return;
    setSubmitting(true);
    setError('');
    try {
      // Trang chi tiết sở hữu mã lý do; hộp thoại chỉ xác nhận hành động có ảnh hưởng đến bài viết.
      await onConfirm();
    } catch (requestError) {
      setError(requestError.message || 'Không thể ẩn bài viết.');
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open
      title="Xác nhận ẩn bài"
      size="sm"
      onClose={close}
      footer={(
        <div className="flex w-full gap-3">
          <Button className="flex-1" variant="secondary" disabled={submitting} onClick={close}>
            Hủy
          </Button>
          <Button className="flex-1 gap-2" disabled={submitting} onClick={confirm}>
            <EyeOff size={16} /> {submitting ? 'Đang ẩn...' : 'Ẩn bài'}
          </Button>
        </div>
      )}
    >
      <div className="rounded-xl bg-amber-50 p-4 text-sm leading-6 text-amber-900">
        <p>Bạn có chắc chắn muốn ẩn bài viết này không?</p>
        <p className="mt-2"><strong>Lý do:</strong> {reasonLabel}</p>
      </div>
      {error ? <p className="app-error mt-4 rounded-xl p-3 text-sm" role="alert">{error}</p> : null}
    </Modal>
  );
}
