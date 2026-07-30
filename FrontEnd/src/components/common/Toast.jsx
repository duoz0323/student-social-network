import { useEffect } from 'react';
import { CheckCircle2, CircleAlert, Info, X } from 'lucide-react';

export default function Toast({ message, type = 'success', onClose, duration = 3000, positioned = true }) {
  useEffect(() => {
    if (duration && onClose) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
  }, [duration, onClose]);

  const presentations = {
    success: {
      icon: CheckCircle2,
      className: 'border-blue-500 text-zinc-800',
      iconClassName: 'fill-blue-600 text-white',
      role: 'status',
    },
    error: {
      icon: CircleAlert,
      className: 'border-red-500 text-zinc-800',
      iconClassName: 'text-red-600',
      role: 'alert',
    },
    info: {
      icon: Info,
      className: 'border-slate-300 text-zinc-800',
      iconClassName: 'text-slate-600',
      role: 'status',
    },
  };
  const presentation = presentations[type] || presentations.info;
  const Icon = presentation.icon;

  return (
    <div
      className={`${positioned ? 'fixed right-4 top-4 z-[100]' : ''} flex min-h-12 w-[min(22rem,calc(100vw-2rem))] items-center gap-2.5 rounded-lg border bg-white px-3.5 py-3 shadow-[0_10px_30px_rgba(15,23,42,0.16)] ${presentation.className}`}
      role={presentation.role}
      aria-live={type === 'error' ? 'assertive' : 'polite'}
    >
      <Icon className={`h-5 w-5 shrink-0 ${presentation.iconClassName}`} strokeWidth={2.4} aria-hidden="true" />
      <span className="min-w-0 flex-1 text-sm font-semibold">{message}</span>
      {onClose && (
        <button type="button" onClick={onClose} aria-label="Đóng thông báo" className="rounded-full p-1 text-zinc-400 transition hover:bg-zinc-100 hover:text-zinc-700">
          <X className="h-4 w-4" aria-hidden="true" />
        </button>
      )}
    </div>
  );
}
