export default function Button({ children, type = 'button', variant = 'primary', size = 'md', disabled = false, onClick, className = '' }) {
  const variants = {
    primary: 'bg-[#0f172a] text-white shadow-sm hover:bg-black hover:shadow-lg hover:scale-[1.02] active:scale-[0.98] border border-transparent',
    secondary: 'bg-white border border-gray-300 text-[#1e293b] shadow-sm hover:bg-gray-50 hover:shadow-md hover:scale-[1.02] active:scale-[0.98]',
    ghost: 'text-[var(--app-muted)] hover:text-[var(--app-text)] hover:bg-[var(--app-surface-soft)] border border-transparent',
    danger: 'bg-red-600 text-white shadow-sm hover:bg-red-700 hover:scale-[1.02] active:scale-[0.98] border border-transparent',
    dangerSoft: 'bg-red-50 text-red-600 border border-red-200 shadow-sm hover:bg-red-100 hover:shadow-md hover:scale-[1.02] active:scale-[0.98]'
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
      className={`inline-flex items-center justify-center font-semibold rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[var(--app-brand)] transition-all duration-200 ${variants[variant]} ${sizes[size]} ${className} ${disabled ? '!opacity-60 !cursor-not-allowed !scale-100 !shadow-none' : ''}`}
    >
      {children}
    </button>
  );
}
