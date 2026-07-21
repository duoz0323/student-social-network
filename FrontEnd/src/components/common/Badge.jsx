export default function Badge({ children, tone = 'neutral' }) {
  const tones = {
    neutral: 'bg-[var(--app-surface-soft)] text-[var(--app-muted)]',
    success: 'bg-[var(--status-active-bg)] text-[var(--status-active)]',
    warning: 'bg-[var(--status-pending-bg)] text-[var(--status-pending)]',
    danger: 'bg-[var(--status-blocked-bg)] text-[var(--status-blocked)]',
  };

  return <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${tones[tone]}`}>{children}</span>;
}
