import { forwardRef } from 'react';

const Input = forwardRef(({ className = '', ...props }, ref) => {
  // Input dùng chung token focus/invalid/disabled để Auth, User và Admin có phản hồi nhất quán.
  return (
    <input
      ref={ref}
      className={`app-field h-[var(--h-input)] w-full rounded-[var(--radius-control)] border px-3.5 text-sm outline-none transition-[border-color,box-shadow,background-color] duration-[var(--motion-fast)] hover:border-[var(--app-border-strong)] aria-[invalid=true]:border-[var(--status-blocked)] aria-[invalid=true]:bg-[var(--status-blocked-bg)] disabled:cursor-not-allowed disabled:bg-[var(--app-surface-soft)] disabled:text-[var(--app-muted)] disabled:opacity-70 read-only:bg-[var(--app-surface-soft)] ${className}`}
      {...props}
    />
  );
});

Input.displayName = 'Input';
export default Input;
