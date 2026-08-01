import { AlertCircle, Inbox, LoaderCircle } from 'lucide-react';
import Button from './Button.jsx';

// Các trạng thái dữ liệu dùng icon đơn sắc và cùng mật độ để tái sử dụng xuyên suốt ứng dụng.
export function LoadingState({ message = 'Đang tải dữ liệu...' }) {
  return (
    <div className="flex min-h-48 items-center justify-center bg-[var(--app-surface)] p-8 text-[var(--app-muted)]" role="status" aria-live="polite" aria-busy="true">
      <div className="flex items-center gap-2.5 text-sm font-medium">
        <LoaderCircle size={19} className="animate-spin text-[var(--app-brand)]" aria-hidden="true" />
        <span>{message}</span>
      </div>
    </div>
  );
}

export function EmptyState({ title, description, actionLabel, onAction }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center bg-[var(--app-surface)] p-8 text-center">
      <Inbox size={28} strokeWidth={1.7} className="mb-3 text-[var(--app-muted)]" aria-hidden="true" />
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

export function ErrorState({ title = 'Không thể tải dữ liệu', description, actionLabel = 'Thử lại', onAction }) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center bg-[var(--app-surface)] p-8 text-center" role="alert">
      <AlertCircle size={28} strokeWidth={1.7} className="mb-3 text-[var(--status-blocked)]" aria-hidden="true" />
      <h3 className="text-base font-semibold text-[var(--app-text)]">{title}</h3>
      {description ? <p className="mt-1.5 max-w-md text-sm text-[var(--app-muted)]">{description}</p> : null}
      {onAction ? (
        <Button className="mt-5" variant="secondary" onClick={onAction}>
          {actionLabel}
        </Button>
      ) : null}
    </div>
  );
}
