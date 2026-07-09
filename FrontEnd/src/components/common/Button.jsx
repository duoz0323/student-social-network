export default function Button({ children, type = 'button', variant = 'primary', size = 'md', disabled = false, onClick, className = '' }) {
  const variants = {
    primary: 'bg-[var(--app-active)] text-[var(--app-bg)] shadow-sm hover:opacity-90 border border-transparent',
    secondary: 'bg-transparent border border-[var(--app-border-strong)] text-[var(--app-text)] shadow-sm hover:bg-[var(--app-surface-soft)]',
    ghost: 'text-[var(--app-muted)] hover:text-[var(--app-text)] hover:bg-[var(--app-surface-soft)] border border-transparent',
    danger: 'bg-red-600 text-white shadow-sm hover:bg-red-700 border border-transparent',
    dangerSoft: 'bg-red-500/10 text-red-600 dark:text-red-400 hover:bg-red-500/20 border border-transparent'
  };
  
  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-5 py-2.5 text-base',
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-all duration-200 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50 disabled:active:scale-100 ${variants[variant]} ${sizes[size]} ${className}`}
    >
      {children}
    </button>
  );
}
