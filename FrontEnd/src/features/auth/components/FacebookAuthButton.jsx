import Button from '../../../components/common/Button.jsx';
import LogoLoader from '../../../components/common/LogoLoader.jsx';
import { useFacebookAuth } from '../facebook/useFacebookAuth.js';

const buttonClass = 'flex h-[42px] w-full items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white text-[13px] font-semibold text-gray-700 shadow-sm transition-all duration-200 hover:bg-gray-50 hover:border-violet-400 hover:shadow-md hover:scale-[1.02] active:scale-[0.98]';

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
  if (facebook.isSdkLoading) return <Button type="button" variant="secondary" loading loadingLabel="Đang tải Facebook..." className={buttonClass}>Tiếp tục với Facebook</Button>;

  return (
    <div>
      <button
        type="button"
        onClick={facebook.isReady ? facebook.startFacebookSignIn : facebook.retrySdk}
        disabled={!facebook.isConfigured || facebook.isAuthenticating}
        className={buttonClass}
      >
        {facebook.isAuthenticating
          ? <LogoLoader size="sm" message="Đang xác minh..." />
          : <><FacebookIcon />{facebook.isReady ? `${actionLabel} Facebook` : facebook.isConfigured ? 'Thử tải lại Facebook' : `${actionLabel} Facebook`}</>}
      </button>
      {facebook.error ? <p className="mt-2 text-xs font-semibold text-red-700">{facebook.error}</p> : null}
    </div>
  );
}
