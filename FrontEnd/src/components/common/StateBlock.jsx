import Button from './Button.jsx';
import LogoLoader from './LogoLoader.jsx';

export function LoadingState({ message = 'Đang tải dữ liệu...' }) {
  return <div className="flex min-h-48 items-center justify-center rounded-xl border border-[var(--app-border)] bg-[var(--app-surface)] p-8"><LogoLoader message={message} size="md" /></div>;
}

export function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div className="rounded-xl border-2 border-dashed border-[var(--app-border-strong)] bg-[var(--app-surface-soft)] p-8 text-center">
      <h3 className="text-base font-semibold text-[var(--app-text)]">{title}</h3>
      <p className="mt-1.5 text-sm text-[var(--app-muted)]">{description}</p>
      {actionLabel ? (
        <Button className="mt-5" variant="secondary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
