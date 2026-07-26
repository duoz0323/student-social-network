import { forwardRef } from 'react';

const Input = forwardRef(({ className = '', ...props }, ref) => {
  return (
    <input
      ref={ref}
      className={`app-field h-[var(--h-input)] w-full rounded-[var(--radius-input)] border px-4 text-sm outline-none transition disabled:cursor-not-allowed disabled:opacity-50 ${className}`}
      {...props}
    />
  );
});

Input.displayName = 'Input';
export default Input;
