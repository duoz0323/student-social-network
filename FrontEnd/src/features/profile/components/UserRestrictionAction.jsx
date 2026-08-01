import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { Shield } from 'lucide-react';
import { socialApi } from '../../../api/index.js';
import { useApp } from '../../../contexts/AppContext.jsx';

/** Hành động dùng chung cho menu hồ sơ và bài viết, chỉ cập nhật UI sau response thành công. */
export default function UserRestrictionAction({
  userId,
  displayName,
  initialRestricted = false,
  blocked = false,
  onTrigger,
  onChanged,
}) {
  const { showToast } = useApp();
  const [restricted, setRestricted] = useState(initialRestricted);
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  if (blocked) return null;

  async function confirm() {
    if (submitting) return;
    setSubmitting(true);
    try {
      const response = restricted
        ? await socialApi.unrestrictUser(userId)
        : await socialApi.restrictUser(userId);
      const next = Boolean(response.restrictedByMe);
      setRestricted(next);
      onChanged?.(next);
      setOpen(false);
      showToast(next ? `Đã hạn chế ${displayName}.` : `Đã bỏ hạn chế ${displayName}.`);
    } catch (error) {
      showToast(error.message || 'Không thể cập nhật trạng thái hạn chế.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <button
        type="button"
        onClick={() => {
          onTrigger?.();
          setOpen(true);
        }}
      >
        <span>{restricted ? 'Bỏ hạn chế' : 'Hạn chế'}</span>
        <Shield size={16} strokeWidth={2} aria-hidden="true" />
      </button>
      <Modal
        open={open}
        title={restricted ? 'Bỏ hạn chế tài khoản này?' : 'Hạn chế tài khoản này?'}
        onClose={() => !submitting && setOpen(false)}
        footer={<>
          <Button variant="secondary" disabled={submitting} onClick={() => setOpen(false)}>Hủy</Button>
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
    </>
  );
}
