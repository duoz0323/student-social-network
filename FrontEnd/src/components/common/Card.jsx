export default function Card({ children, className = '' }) {
  return (
    <div className={`rounded-[var(--radius-card)] border border-[var(--app-border)] bg-[var(--app-surface)] ${className}`}>
      {children}
    </div>
  );
}
