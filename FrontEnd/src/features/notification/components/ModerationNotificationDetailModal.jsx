import { useEffect, useState } from 'react';
import { AlertTriangle, FileWarning, ShieldAlert } from 'lucide-react';
import { socialApi } from '../../../api/socialApi.js';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { formatDateTime } from '../../../utils/formatters.js';
import { getModerationReasonLabel } from '../utils/moderationNotification.js';

export default function ModerationNotificationDetailModal({ notification, onClose }) {
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const isWarning = notification?.type === 'CONTENT_VIOLATION_WARNING'
    || notification?.type === 'CONTENT_VIOLATION_FINAL_WARNING';

  useEffect(() => {
    const controller = new AbortController();
    socialApi.getNotificationModerationDetail(notification.notificationId, controller.signal)
      .then((response) => { setDetail(response); setError(''); })
      .catch((requestError) => { if (requestError.code !== 'ERR_CANCELED') setError(requestError.message || 'Không thể tải chi tiết kiểm duyệt.'); });
    return () => controller.abort();
  }, [notification.notificationId]);

  return (
    <Modal open title={isWarning ? 'Chi tiết cảnh báo vi phạm' : 'Chi tiết bài viết bị ẩn'} onClose={onClose} footer={<Button onClick={onClose}>Đã hiểu</Button>}>
      {!detail && !error ? <div className="flex min-h-40 items-center justify-center text-sm text-[var(--app-muted)]">Đang tải chi tiết...</div> : null}
      {error ? <div className="rounded-xl bg-red-50 p-4 text-sm text-red-700">{error}</div> : null}
      {detail ? <div className="space-y-4">
        <div className={`flex gap-3 rounded-xl p-4 ${isWarning ? 'bg-amber-50 text-amber-900' : 'bg-orange-50 text-orange-900'}`}>
          {isWarning ? <AlertTriangle className="mt-0.5 shrink-0" size={21} /> : <ShieldAlert className="mt-0.5 shrink-0" size={21} />}
          <div><p className="font-bold">{isWarning ? `Cảnh báo ${detail.violationCount}/${detail.violationThreshold}` : 'Bài viết không còn hiển thị công khai'}</p><p className="mt-1 text-sm leading-6">{isWarning ? 'Vi phạm đã được quản trị viên xác nhận. Các vi phạm tiếp theo có thể khiến tài khoản bị khóa.' : 'Bài viết được ẩn sau khi quản trị viên đối chiếu với tiêu chuẩn nội dung của hệ thống.'}</p></div>
        </div>
        <div className="rounded-xl border border-[var(--app-border)] p-4"><p className="text-xs font-semibold uppercase tracking-wide text-[var(--app-muted)]">Lý do xử lý</p><p className="mt-2 font-semibold text-[var(--app-text)]">{getModerationReasonLabel(detail.reasonCode)}</p></div>
        <div className="rounded-xl border border-[var(--app-border)] p-4"><div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-[var(--app-muted)]"><FileWarning size={15} /><span>Nội dung liên quan{detail.postId ? ` · Bài viết #${detail.postId}` : ''}</span></div><p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-[var(--app-text)]">{detail.postSummary || 'Không còn snapshot nội dung để hiển thị.'}</p></div>
        <div className="text-sm text-[var(--app-muted)]"><span className="font-semibold text-[var(--app-text)]">Thời điểm xử lý:</span> {formatDateTime(detail.processedAt)}</div>
        <p className="rounded-xl bg-[var(--app-surface-soft)] p-4 text-sm leading-6 text-[var(--app-muted)]">Nếu cho rằng quyết định chưa chính xác, hãy lưu mã thông báo #{detail.notificationId} để liên hệ bộ phận hỗ trợ. Danh tính quản trị viên xử lý được bảo mật.</p>
      </div> : null}
    </Modal>
  );
}
