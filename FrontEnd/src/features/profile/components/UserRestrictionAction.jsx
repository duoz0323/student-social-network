import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { socialApi } from '../../../api/index.js';
import { useApp } from '../../../contexts/AppContext.jsx';

/** Modal Restrict được component cha điều khiển để không bị unmount khi dropdown đóng. */
export default function UserRestrictionAction({
  open,
  userId,
  displayName,
  restricted = false,
  onClose,
  onChanged,
}) {
  const { showToast } = useApp();
  const [submitting, setSubmitting] = useState(false);

  async function confirm() {
    if (!userId || submitting) return;
    setSubmitting(true);
    try {
      const response = restricted
        ? await socialApi.unrestrictUser(userId)
        : await socialApi.restrictUser(userId);
      const next = Boolean(response.restrictedByMe);
      onChanged?.(next);
      onClose?.();
      showToast(next ? `Đã hạn chế ${displayName}.` : `Đã bỏ hạn chế ${displayName}.`);
    } catch (error) {
      showToast(error.message || 'Không thể cập nhật trạng thái hạn chế.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open={open}
      title={restricted ? 'Bỏ hạn chế tài khoản này?' : 'Hạn chế tài khoản này?'}
      onClose={() => !submitting && onClose?.()}
      footer={<>
        <Button variant="secondary" disabled={submitting} onClick={onClose}>Hủy</Button>
        <Button disabled={submitting} onClick={confirm}>
          {submitting ? 'Đang xử lý...' : restricted ? 'Bỏ hạn chế' : 'Hạn chế'}
        </Button>
      </>}
    >
      {restricted ? (
        <p>Bạn sẽ tiếp tục nhận các thông báo tương tác mới từ tài khoản này.</p>
      ) : (
        <div className="space-y-2 text-sm leading-relaxed">
          <p>Họ vẫn có thể xem hồ sơ, bài viết, thích và bình luận nội dung của bạn.</p>
          <p>Bạn sẽ không nhận thông báo về lượt thích, bình luận hoặc trả lời từ họ.</p>
          <p>Họ sẽ không được thông báo rằng bạn đã hạn chế họ.</p>
        </div>
      )}
    </Modal>
  );
}
