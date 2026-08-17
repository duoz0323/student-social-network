import { useState } from 'react';
import { UnlockKeyhole } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';

// Xác nhận rõ hậu quả mở khóa và giữ nguyên lịch sử vi phạm trước khi gọi API hiện có.
export default function UnblockUserDialog({ user, onClose, onConfirm }) {
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function confirm() {
    if (submitting) return;
    setSubmitting(true);
    setError('');
    try {
      await onConfirm(user);
    } catch (requestError) {
      setError(requestError.message);
      setSubmitting(false);
    }
  }

  return (
    <Modal open title="Mở khóa tài khoản" size="sm" onClose={() => !submitting && onClose()} footer={(
      <div className="flex w-full gap-3">
        <Button className="flex-1" variant="secondary" disabled={submitting} onClick={onClose}>Hủy</Button>
        <Button className="flex-1" disabled={submitting} onClick={confirm}>{submitting ? 'Đang mở khóa...' : 'Mở khóa'}</Button>
      </div>
    )}>
      <div className="flex gap-3 rounded-xl bg-emerald-50 p-4 text-sm leading-6 text-emerald-900">
        <UnlockKeyhole className="mt-0.5 shrink-0" size={20} />
        <p>Người dùng có thể đăng nhập lại. Lịch sử vi phạm và các Refresh Token đã thu hồi sẽ không được khôi phục.</p>
      </div>
      {error ? <p className="mt-4 text-sm text-red-600" role="alert">{error}</p> : null}
    </Modal>
  );
}
