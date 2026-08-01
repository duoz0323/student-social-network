export default function Badge({ children, tone = 'neutral', className = '', ...props }) {
  // Badge dùng pill vì đây là nhãn trạng thái ngắn, không phải control tương tác thông thường.
  const tones = {
    neutral: 'border-[var(--app-border)] bg-[var(--app-surface-soft)] text-[var(--app-muted)]',
    success: 'border-emerald-200 bg-[var(--status-active-bg)] text-[var(--status-active)]',
    warning: 'border-amber-200 bg-[var(--status-pending-bg)] text-[var(--status-pending)]',
    danger: 'border-red-200 bg-[var(--status-blocked-bg)] text-[var(--status-blocked)]',
  };

  return (
    <span
      className={`inline-flex min-h-6 items-center rounded-[var(--radius-pill)] border px-2.5 py-0.5 text-xs font-semibold leading-4 ${tones[tone] ?? tones.neutral} ${className}`}
      {...props}
    >
      {children}
    </span>
  );
}
