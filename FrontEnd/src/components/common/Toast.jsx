import { useEffect } from 'react';
import { Check, CircleAlert, Info, X } from 'lucide-react';

export default function Toast({ message, type = 'success', onClose, duration = 3000, positioned = true }) {
  useEffect(() => {
    if (duration && onClose) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  const presentations = {
    success: {
      icon: Check,
      iconClassName: 'bg-white text-zinc-950',
      role: 'status',
    },
    error: {
      icon: CircleAlert,
      iconClassName: 'bg-red-400/15 text-red-300',
      role: 'alert',
    },
    info: {
      icon: Info,
      iconClassName: 'bg-white/10 text-zinc-100',
      role: 'status',
    },
  };
  const presentation = presentations[type] || presentations.info;
  const Icon = presentation.icon;

  // Snackbar tối giản lấy cảm hứng từ Threads: trung tính, gọn và không che nội dung phía trên.
  const content = (
    <div
      className="animate-toast-in flex min-h-12 w-max max-w-[calc(100vw-2rem)] items-center gap-3 rounded-xl border border-white/10 bg-zinc-950 px-4 py-3 text-white shadow-[0_14px_40px_rgba(0,0,0,0.28)] dark:border-white/15 dark:bg-zinc-100 dark:text-zinc-950"
      role={presentation.role}
      aria-live={type === 'error' ? 'assertive' : 'polite'}
    >
      <span className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-full ${presentation.iconClassName}`}>
        <Icon className="h-3.5 w-3.5" strokeWidth={2.6} aria-hidden="true" />
      </span>
      <span className="min-w-0 flex-1 text-sm font-semibold leading-5">{message}</span>
      {onClose && (
        <button type="button" onClick={onClose} aria-label="Đóng thông báo" className="-mr-1 rounded-full p-1 text-zinc-400 transition hover:bg-white/10 hover:text-white dark:text-zinc-500 dark:hover:bg-black/10 dark:hover:text-zinc-950">
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      )}
    </div>
  );

  if (!positioned) return content;

  // Tách lớp định vị để animation transform không làm mất căn giữa theo trục ngang.
  return (
    <div className="pointer-events-none fixed bottom-[calc(1rem+env(safe-area-inset-bottom))] left-1/2 z-[100] -translate-x-1/2 sm:bottom-6">
      <div className="pointer-events-auto">{content}</div>
    </div>
  );
}
