import { Mail } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';

function formatLinkedAt(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
}

function GoogleIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" width="20" height="20">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

function FacebookIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" width="20" height="20" fill="#1877F2">
      <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.469h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.469h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
    </svg>
  );
}

export default function AuthProviderCard({ method, disabled, onLink, onUnlink, onSetPassword, onChangePassword }) {
  const meta = AUTH_PROVIDER_META[method.type];
  const linkedAt = formatLinkedAt(method.linkedAt);

  function renderProviderIcon() {
    if (method.type === 'EMAIL') return <Mail size={20} strokeWidth={2} aria-hidden="true" />;
    if (method.type === 'FACEBOOK') return <FacebookIcon />;
    return <GoogleIcon />;
  }

  const isEmail = method.type === 'EMAIL';
  const statusLabel = isEmail
    ? method.state === 'READY' ? 'Sẵn sàng đăng nhập' : method.state === 'VERIFIED_NO_PASSWORD' ? 'Đã xác minh' : 'Chưa liên kết'
    : method.linked ? 'Đã liên kết' : 'Chưa liên kết';
  const positive = method.linked || method.state === 'VERIFIED_NO_PASSWORD';
  const description = isEmail
    ? method.state === 'READY'
      ? 'Bạn có thể đăng nhập bằng email và mật khẩu UniShare.'
      : method.state === 'VERIFIED_NO_PASSWORD'
        ? 'Chưa thiết lập mật khẩu để đăng nhập bằng email.'
        : 'Thêm email và thiết lập mật khẩu để đăng nhập bằng email.'
    : method.linked ? 'Phương thức này đã sẵn sàng để đăng nhập.' : `Liên kết ${meta.label} với tài khoản của bạn.`;

  return (
    <article className="flex min-w-0 items-start gap-3.5 border-b border-[var(--app-border)] px-1 py-5 last:border-b-0 sm:items-center sm:gap-4">
      <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full border border-[var(--app-border)] bg-[var(--app-surface-soft)] text-[var(--app-text)]">
        {renderProviderIcon()}
      </span>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <h2 className="text-[15px] font-bold text-[var(--app-text)]">{meta.label}</h2>
          <span className={`inline-flex items-center gap-1.5 text-xs font-semibold ${
            positive ? 'text-emerald-600 dark:text-emerald-400' : 'text-[var(--app-muted)]'
          }`}>
            <span className={`h-1.5 w-1.5 rounded-full ${positive ? 'bg-emerald-500' : 'bg-[var(--app-border-strong)]'}`} />
            {statusLabel}
          </span>
        </div>
        {method.maskedIdentifier ? (
          <p className="mt-1 break-all text-sm text-[var(--app-muted)]">{method.maskedIdentifier}</p>
        ) : (
          <p className="mt-1 text-sm text-[var(--app-muted)]">
            {description}
          </p>
        )}
        {method.linked && linkedAt ? (
          <p className="mt-1 text-xs text-[var(--app-muted)]">
            {isEmail ? 'Xác minh lúc' : 'Liên kết'} {linkedAt}
          </p>
        ) : null}
      </div>

      <div className="shrink-0 pt-0.5 sm:pt-0">
        {method.canSetPassword ? <Button variant="secondary" size="sm" disabled={disabled} onClick={() => onSetPassword(method)} className="min-w-[110px] px-3">Thiết lập mật khẩu</Button> : method.canChangePassword ? <div className="flex flex-col gap-2"><Button variant="secondary" size="sm" disabled={disabled} onClick={() => onChangePassword(method)} className="min-w-[110px] px-3">Đổi mật khẩu</Button>{method.canUnlink ? <Button variant="dangerSoft" size="sm" disabled={disabled} onClick={() => onUnlink(method)} className="min-w-[110px] px-3">Gỡ liên kết</Button> : null}</div> : <Button
          variant={method.linked ? 'dangerSoft' : 'secondary'}
          size="sm"
          disabled={disabled || (method.linked && !method.canUnlink)}
          onClick={() => method.linked ? onUnlink(method) : onLink(method)}
          className="min-w-[86px] px-3"
        >
          {method.linked ? method.canUnlink ? 'Gỡ liên kết' : 'Không thể gỡ' : 'Liên kết'}
        </Button>}
      </div>
    </article>
  );
}
