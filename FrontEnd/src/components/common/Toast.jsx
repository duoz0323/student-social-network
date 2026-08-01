import { useEffect } from 'react';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';

export default function Toast({ message, type = 'success', onClose, duration = 3000 }) {
  useEffect(() => {
    if (duration && onClose) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  const types = {
    success: 'border-[var(--status-active)] bg-[var(--status-active-bg)] text-[var(--status-active)]',
    error: 'border-[var(--status-blocked)] bg-[var(--status-blocked-bg)] text-[var(--status-blocked)]',
    info: 'border-[var(--app-border-strong)] bg-[var(--app-surface-soft)] text-[var(--app-text)]',
  };
  const icons = {
    success: CheckCircle2,
    error: AlertCircle,
    info: Info,
  };
  const StatusIcon = icons[type] ?? Info;

  // Toast là overlay nên được phép dùng shadow nhẹ và tự co về chiều rộng mobile.
  return (
    <div className={`fixed bottom-4 left-4 right-4 z-50 flex items-center gap-3 rounded-[var(--radius-card)] border px-4 py-3 shadow-[var(--shadow-overlay)] sm:left-auto sm:max-w-md ${types[type] ?? types.info}`} role={type === 'error' ? 'alert' : 'status'} aria-live={type === 'error' ? 'assertive' : 'polite'}>
      <StatusIcon size={18} strokeWidth={2} className="shrink-0" aria-hidden="true" />
      <span className="min-w-0 flex-1 text-sm font-medium">{message}</span>
      {onClose && (
        <button onClick={onClose} className="flex h-7 w-7 shrink-0 items-center justify-center rounded-[var(--radius-control)] opacity-70 outline-none transition-[background-color,opacity] duration-[var(--motion-fast)] hover:bg-black/5 hover:opacity-100 focus-visible:ring-2 focus-visible:ring-[var(--app-brand)]" aria-label="Đóng thông báo">
          <X size={16} strokeWidth={2} aria-hidden="true" />
        </button>
      )}
    </div>
  );
}
