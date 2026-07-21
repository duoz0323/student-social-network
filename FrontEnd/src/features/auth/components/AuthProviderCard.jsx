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
  return (
    <article className="min-w-0 rounded-2xl border border-[var(--app-border)] bg-[var(--app-surface)] p-4 shadow-sm sm:p-5">
      <div className="flex min-w-0 flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-base font-bold text-[var(--app-text)]">{meta.label}</h2>
            <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${method.linked ? 'bg-emerald-500/10 text-emerald-700' : 'bg-zinc-500/10 text-[var(--app-muted)]'}`}>
              {method.linked ? 'Đã liên kết' : 'Chưa liên kết'}
            </span>
          </div>
          {method.maskedIdentifier ? <p className="mt-2 break-all text-sm font-medium text-[var(--app-text)]">{method.maskedIdentifier}</p> : null}
          {method.linked ? (
            <dl className="mt-3 grid gap-1 text-xs text-[var(--app-muted)] sm:grid-cols-2 sm:gap-x-6">
              <div><dt className="inline font-semibold">Đã xác minh: </dt><dd className="inline">{method.verified ? 'Có' : 'Không'}</dd></div>
              <div><dt className="inline font-semibold">Có thể gỡ: </dt><dd className="inline">{method.canUnlink ? 'Có' : 'Không'}</dd></div>
              {meta.kind === 'LOCAL' ? <div><dt className="inline font-semibold">Đăng nhập local: </dt><dd className="inline">{method.localLoginAvailable ? 'Khả dụng' : 'Không khả dụng'}</dd></div> : null}
              {linkedAt ? <div><dt className="inline font-semibold">Liên kết lúc: </dt><dd className="inline">{linkedAt}</dd></div> : null}
            </dl>
          ) : null}
        </div>
        <Button
          variant={method.linked ? 'dangerSoft' : 'secondary'}
          disabled={disabled || (method.linked && !method.canUnlink)}
          onClick={() => method.linked ? onUnlink(method) : onLink(method)}
          className="w-full shrink-0 sm:w-auto"
        >
          {method.linked ? method.canUnlink ? 'Gỡ liên kết' : 'Không thể gỡ' : 'Liên kết'}
        </Button>
      </div>
    </article>
  );
}
