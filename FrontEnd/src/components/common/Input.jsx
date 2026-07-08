import { forwardRef } from 'react';

const Input = forwardRef(({ className = '', ...props }, ref) => {
  return (
    <input
      ref={ref}
      className={`h-[var(--h-input)] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm placeholder:text-zinc-400 outline-none transition focus:border-[var(--app-text)] focus:ring-1 focus:ring-[var(--app-text)] disabled:cursor-not-allowed disabled:opacity-50 ${className}`}
      {...props}
    />
  );
});

Input.displayName = 'Input';
export default Input;
