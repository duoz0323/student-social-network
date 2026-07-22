import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

// Danh sách lý do báo cáo mở rộng theo mockup model-report.jpg
const REPORT_REASONS = [
  { value: 'SPAM', label: 'Spam' },
  { value: 'HARASSMENT', label: 'Quấy rối' },
  { value: 'HARMFUL_CONTENT', label: 'Nội dung độc hại hoặc xúc phạm' },
  { value: 'VIOLENCE', label: 'Nội dung bạo lực' },
  { value: 'MISINFORMATION', label: 'Thông tin sai lệch' },
  { value: 'INAPPROPRIATE', label: 'Nội dung không phù hợp' },
  { value: 'OTHER', label: 'Lý do khác' },
];

// Icon check tròn cho bước thành công
function SuccessIcon() {
  return (
    <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full border-2 border-[var(--app-text)] text-[var(--app-text)]">
      <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
        <polyline points="20 6 9 17 4 12" />
      </svg>
    </div>
  );
}

export default function ReportPostFlow({ open, post, onClose }) {
  const { submitReport } = useApp();
  const [step, setStep] = useState('reason');
  const [reason, setReason] = useState('SPAM');
  const [description, setDescription] = useState('');
  const [message, setMessage] = useState('');

  async function submit() {
    const result = await submitReport(post.id, reason, description);
    if (!result.ok) {
      setMessage(result.message);
      return;
    }
    setStep('success');
  }

  function close() {
    setStep('reason');
    setReason('SPAM');
    setDescription('');
    setMessage('');
    onClose();
  }

  // Tìm label của lý do đã chọn
  const selectedReasonLabel = REPORT_REASONS.find((r) => r.value === reason)?.label || reason;

  return (
    <Modal
      open={open}
      title={step === 'success' ? '' : step === 'detail' ? 'Chi tiết báo cáo' : 'Báo cáo bài viết'}
      onClose={close}
      size={step === 'success' ? 'sm' : 'md'}
      footer={
        step === 'success' ? (
          <Button className="w-full !rounded-xl !h-[44px] text-[15px] font-bold" onClick={close}>Xong</Button>
        ) : step === 'reason' ? (
          <div className="flex w-full items-center gap-3">
            <Button variant="secondary" className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={close}>Hủy</Button>
            <Button className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={() => setStep('detail')}>Tiếp tục</Button>
          </div>
        ) : (
          <div className="flex w-full items-center gap-3">
            <Button variant="secondary" className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={() => setStep('reason')}>Hủy</Button>
            <Button className="flex-1 !rounded-xl !h-[44px] text-[15px] font-bold" onClick={submit}>Gửi báo cáo</Button>
          </div>
        )
      }
      footerClassName="!border-none !pt-2 !pb-6"
    >
      {/* Bước 3: Thành công — icon check tím + thông báo */}
      {step === 'success' ? (
        <div className="py-4 text-center">
          <SuccessIcon />
          <h3 className="text-lg font-black text-[var(--app-text)]">Đã gửi báo cáo</h3>
          <p className="mt-2 text-sm text-[var(--app-muted)]">Cảm ơn bạn đã báo cáo. Quản trị viên sẽ xem xét bài viết này.</p>
        </div>
      ) : step === 'reason' ? (
        /* Bước 1: Chọn lý do — radio bên phải theo mockup */
        <div>
          <p className="mb-1 text-base font-bold text-[var(--app-text)]">Tại sao bạn muốn báo cáo bài viết này?</p>
          <p className="mb-4 text-sm text-[var(--app-muted)]">Báo cáo của bạn sẽ được gửi đến quản trị viên để xem xét.</p>
          <div className="space-y-1">
            {REPORT_REASONS.map((item) => (
              <label
                key={item.value}
                className={`flex cursor-pointer items-center justify-between rounded-xl border px-4 py-3 text-sm transition ${
                  reason === item.value
                    ? 'border-[var(--app-text)] font-bold'
                    : 'border-[var(--app-border)] font-medium hover:bg-[var(--app-surface-soft)]'
                }`}
              >
                <span>{item.label}</span>
                <input
                  type="radio"
                  name="report-reason"
                  className="h-4 w-4 accent-black"
                  checked={reason === item.value}
                  onChange={() => setReason(item.value)}
                />
              </label>
            ))}
          </div>
        </div>
      ) : (
        /* Bước 2: Chi tiết — hiển thị lý do đã chọn + textarea mô tả */
        <div>
          {/* Hiển thị lý do đã chọn với nút "Thay đổi" */}
          <div className="mb-4 flex items-center justify-between rounded-xl border border-[var(--app-border)] px-4 py-3">
            <span className="text-sm">
              <span className="text-[var(--app-muted)]">Lý do: </span>
              <span className="font-semibold text-[var(--app-text)]">{selectedReasonLabel}</span>
            </span>
            <button className="text-sm font-bold text-[var(--app-text)] transition hover:underline" onClick={() => setStep('reason')}>
              Thay đổi
            </button>
          </div>

          <p className="mb-2 text-sm font-bold text-[var(--app-text)]">Mô tả thêm (Tùy chọn)</p>
          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Hãy cung cấp thêm thông tin để chúng tôi hiểu rõ vấn đề."
            maxLength={500}
            className="min-h-28 w-full resize-none rounded-xl border border-[var(--app-border)] p-3 text-sm outline-none transition focus:border-[var(--app-text)] focus:ring-2 focus:ring-black/5"
          />
          <div className="mt-1 text-right text-xs text-[var(--app-muted)]">{description.length}/500</div>

          <p className="mt-3 text-center text-xs text-[var(--app-muted)]">Người đăng bài sẽ không biết ai đã gửi báo cáo.</p>
          {message && <p className="mt-3 rounded-xl bg-amber-50 p-3 text-sm text-amber-700">{message}</p>}
        </div>
      )}
    </Modal>
  );
}
