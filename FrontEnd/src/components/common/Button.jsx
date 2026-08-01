export default function Button({
  children,
  type = 'button',
  variant = 'primary',
  size = 'md',
  shape = 'control',
  disabled = false,
  loading = false,
  loadingLabel = 'Đang xử lý...',
  onClick,
  className = '',
  ...props
}) {
  const variants = {
    primary: 'border border-transparent bg-[var(--app-active)] text-[var(--app-active-contrast)] transition-all duration-200 hover:-translate-y-0.5 hover:shadow-[0_8px_20px_rgba(0,0,0,0.14)] hover:opacity-95 active:translate-y-0 active:scale-[0.99]',
    secondary: 'border border-[var(--app-border-strong)] bg-[var(--app-control-bg)] text-[var(--app-text)] transition-all duration-200 hover:-translate-y-0.5 hover:border-zinc-300 hover:shadow-[0_6px_18px_rgba(0,0,0,0.05)] hover:bg-[var(--app-surface-soft)] active:translate-y-0 active:scale-[0.99]',
    ghost: 'border border-transparent text-[var(--app-muted)] transition-all duration-200 hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)] active:scale-[0.98]',
    danger: 'border border-transparent bg-red-600 text-white transition-all duration-200 hover:-translate-y-0.5 hover:bg-red-700 hover:shadow-[0_6px_18px_rgba(220,38,38,0.25)] active:translate-y-0 active:scale-[0.99]',
    dangerSoft: 'border border-red-200 bg-[var(--status-blocked-bg)] text-[var(--status-blocked)] transition-all duration-200 hover:-translate-y-0.5 hover:bg-red-100 active:translate-y-0 active:scale-[0.99]',
  };

  const sizes = {
    sm: 'min-h-8 px-3 py-1.5 text-xs',
    md: 'min-h-11 px-4 py-2.5 text-sm',
    lg: 'min-h-12 px-5 py-3 text-base',
  };
  // Chỉ hành động ngắn như Follow mới chủ động chọn pill; mặc định mọi button dùng radius control.
  const shapes = {
    control: 'rounded-[var(--radius-control)]',
    pill: 'rounded-[var(--radius-pill)]',
  };
  const isDisabled = disabled || loading;

  return (
    <button
      type={type}
      disabled={isDisabled}
      onClick={onClick}
      aria-busy={loading || undefined}
      className={`inline-flex items-center justify-center gap-2 font-semibold outline-none transition-[background-color,border-color,color,opacity] duration-[var(--motion-fast)] focus-visible:ring-2 focus-visible:ring-[var(--app-brand)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--app-bg)] disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant] ?? variants.primary} ${sizes[size] ?? sizes.md} ${shapes[shape] ?? shapes.control} ${className}`}
      {...props}
    >
      {loading ? (
        <>
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" aria-hidden="true" />
          <span>{loadingLabel}</span>
        </>
      ) : children}
    </button>
  );
}
