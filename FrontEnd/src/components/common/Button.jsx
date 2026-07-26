export default function Button({ children, type = 'button', variant = 'primary', size = 'md', disabled = false, onClick, className = '' }) {
  const variants = {
    primary: 'bg-[var(--app-active)] text-[var(--app-bg)] shadow-sm hover:opacity-90 hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] border border-transparent',
    secondary: 'bg-[var(--app-control-bg)] border border-[var(--app-border-strong)] text-[var(--app-text)] shadow-sm hover:bg-[var(--app-surface-soft)] hover:shadow-md hover:scale-[1.02] active:scale-[0.98]',
    ghost: 'text-[var(--app-muted)] hover:text-[var(--app-text)] hover:bg-[var(--app-surface-soft)] border border-transparent',
    danger: 'bg-red-600 text-white shadow-sm hover:bg-red-700 hover:scale-[1.02] active:scale-[0.98] border border-transparent',
    dangerSoft: 'bg-[var(--status-blocked-bg)] text-[var(--status-blocked)] border border-current/30 shadow-sm hover:brightness-95 hover:shadow-md hover:scale-[1.02] active:scale-[0.98]'
  };
  
  const sizes = {
    sm: 'h-8 px-3 text-xs',
    md: 'h-[44px] px-4 text-sm',
    lg: 'h-12 px-5 text-base',
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center font-semibold rounded-full focus:outline-none focus:ring-2 focus:ring-[var(--app-brand)] focus:ring-offset-2 focus:ring-offset-[var(--app-bg)] transition-all duration-200 ${variants[variant]} ${sizes[size]} ${className} ${disabled ? '!opacity-60 !cursor-not-allowed !scale-100 !shadow-none' : ''}`}
    >
      {children}
    </button>
  );
}
