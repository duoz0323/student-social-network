export default function Button({ children, type = 'button', variant = 'primary', size = 'md', disabled = false, onClick, className = '' }) {
  const variants = {
    primary: 'bg-[var(--app-active)] text-white hover:bg-zinc-800',
    secondary: 'border border-[var(--app-border)] bg-white text-[var(--app-text)] hover:bg-[var(--app-surface-soft)]',
    ghost: 'text-zinc-700 hover:bg-[var(--app-surface-soft)]',
    danger: 'bg-red-600 text-white hover:bg-red-700',
  };
  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-5 py-3 text-base',
  };

  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center rounded-full font-semibold transition disabled:cursor-not-allowed disabled:opacity-50 ${variants[variant]} ${sizes[size]} ${className}`}
    >
      {children}
    </button>
  );
}
