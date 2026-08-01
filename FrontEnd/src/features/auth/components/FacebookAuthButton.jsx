import Button from '../../../components/common/Button.jsx';
import { useFacebookAuth } from '../facebook/useFacebookAuth.js';

// Nút social với viền rõ nét & hiệu ứng chuyển động mượt mà khi hover
const buttonClass = 'flex h-12 w-full items-center justify-center gap-3 rounded-[12px] border border-zinc-300 bg-white text-sm font-medium text-zinc-800 shadow-xs transition-all duration-200 hover:-translate-y-0.5 hover:border-zinc-400 hover:bg-zinc-50/90 hover:shadow-[0_6px_20px_rgba(0,0,0,0.08)] active:translate-y-0 active:scale-[0.99] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60 sm:h-[52px]';

function FacebookIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18" fill="#1877F2">
      <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.469h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.469h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
    </svg>
  );
}

export default function FacebookAuthButton(props) {
  const facebook = useFacebookAuth(props);
  const actionLabel = props.actionLabel ?? 'Tiếp tục với';
  if (facebook.isSdkLoading) return <Button type="button" variant="secondary" disabled className={buttonClass}>Đang tải Facebook...</Button>;

  return (
    <div>
      <button
        type="button"
        onClick={facebook.isReady ? facebook.startFacebookSignIn : facebook.retrySdk}
        disabled={!facebook.isConfigured || facebook.isAuthenticating}
        className={buttonClass}
      >
        <FacebookIcon />
        {facebook.isReady ? `${actionLabel} Facebook` : facebook.isConfigured ? 'Thử tải lại Facebook' : `${actionLabel} Facebook`}
      </button>
      {facebook.isAuthenticating ? <p className="mt-2 text-center text-xs font-semibold text-zinc-600">Đang xác minh...</p> : null}
      {facebook.error ? <p className="mt-2 text-xs font-semibold text-red-700">{facebook.error}</p> : null}
    </div>
  );
}
