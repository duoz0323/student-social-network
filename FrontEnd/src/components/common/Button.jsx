export default function Button({ children, type = 'button', variant = 'primary', size = 'md', disabled = false, onClick, className = '' }) {
  const variants = {
    primary: 'bg-[var(--app-active)] text-[var(--app-surface)] hover:opacity-80',
    secondary: 'border border-[var(--app-border)] bg-[var(--app-surface)] text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]',
    ghost: 'text-zinc-700 hover:bg-[var(--app-surface-soft)]',
    danger: 'bg-red-600 text-white hover:bg-red-700',
  };
  const sizes = {
    sm: 'px-3 min-h-[var(--h-button-sm)] text-sm',
    md: 'px-4 min-h-[var(--h-button-md)] text-sm',
    lg: 'px-5 min-h-[var(--h-button-lg)] text-base',
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center rounded-[var(--radius-button)] font-semibold transition disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${sizes[size]} ${className}`}
    >
      {children}
    </button>
  );
}
