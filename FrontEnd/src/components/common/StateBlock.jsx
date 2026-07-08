import Button from './Button.jsx';

export function LoadingState({ message = 'Dang tai du lieu...' }) {
  return <div className="rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] p-8 text-center text-[var(--app-muted)]">{message}</div>;
}

export function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div className="rounded-2xl border border-dashed border-[var(--app-border-strong)] bg-[var(--app-surface)] p-8 text-center">
      <h3 className="text-lg font-bold text-[var(--app-text)]">{title}</h3>
      <p className="mt-2 text-sm text-[var(--app-muted)]">{description}</p>
      {actionLabel ? (
        <Button className="mt-4" variant="secondary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
