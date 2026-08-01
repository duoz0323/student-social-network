import { Mail } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';

function formatLinkedAt(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

export default function AuthProviderCard({ method, disabled, onLink, onUnlink }) {
  const meta = AUTH_PROVIDER_META[method.type];
  const linkedAt = formatLinkedAt(method.linkedAt);

  function renderProviderIcon() {
    if (method.type === 'EMAIL') return <Mail size={20} strokeWidth={2} aria-hidden="true" />;
    if (method.type === 'FACEBOOK') return <span className="text-xl font-extrabold leading-none" aria-hidden="true">f</span>;
    return <span className="text-lg font-bold leading-none" aria-hidden="true">G</span>;
  }

  return (
    <article className="flex min-w-0 items-start gap-3.5 border-b border-[var(--app-border)] px-1 py-5 last:border-b-0 sm:items-center sm:gap-4">
      <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-[var(--app-border)] bg-[var(--app-surface-soft)] text-[var(--app-text)]">
        {renderProviderIcon()}
      </span>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <h2 className="text-[15px] font-bold text-[var(--app-text)]">{meta.label}</h2>
          <span className={`inline-flex items-center gap-1.5 text-xs font-semibold ${
            method.linked ? 'text-emerald-600 dark:text-emerald-400' : 'text-[var(--app-muted)]'
          }`}>
            <span className={`h-1.5 w-1.5 rounded-full ${method.linked ? 'bg-emerald-500' : 'bg-[var(--app-border-strong)]'}`} />
            {method.linked ? 'Đã liên kết' : 'Chưa liên kết'}
          </span>
        </div>
        {method.maskedIdentifier ? (
          <p className="mt-1 break-all text-sm text-[var(--app-muted)]">{method.maskedIdentifier}</p>
        ) : (
          <p className="mt-1 text-sm text-[var(--app-muted)]">
            {method.linked ? 'Phương thức này đã sẵn sàng để đăng nhập.' : `Liên kết ${meta.label} với tài khoản của bạn.`}
          </p>
        )}
        {method.linked && linkedAt ? (
          <p className="mt-1 text-xs text-[var(--app-muted)]">Liên kết {linkedAt}</p>
        ) : null}
        {method.linked && meta.kind === 'LOCAL' && !method.localLoginAvailable ? (
          <p className="mt-1 text-xs font-medium text-amber-600 dark:text-amber-400">
            Đăng nhập bằng email hiện chưa khả dụng.
          </p>
        ) : null}
      </div>

      <div className="shrink-0 pt-0.5 sm:pt-0">
        <Button
          variant={method.linked ? 'dangerSoft' : 'secondary'}
          size="sm"
          disabled={disabled || (method.linked && !method.canUnlink)}
          onClick={() => method.linked ? onUnlink(method) : onLink(method)}
          className="min-w-[86px] px-3"
        >
          {method.linked ? method.canUnlink ? 'Gỡ liên kết' : 'Không thể gỡ' : 'Liên kết'}
        </Button>
      </div>
    </article>
  );
}
