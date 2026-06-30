import Button from './Button.jsx';

export function LoadingState({ message = 'Dang tai du lieu...' }) {
  return <div className="rounded-2xl border border-zinc-100 bg-white p-8 text-center text-zinc-500">{message}</div>;
}

export function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div className="rounded-2xl border border-dashed border-zinc-200 bg-white p-8 text-center">
      <h3 className="text-lg font-bold text-zinc-950">{title}</h3>
      <p className="mt-2 text-sm text-zinc-500">{description}</p>
      {actionLabel ? (
        <Button className="mt-4" variant="secondary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
