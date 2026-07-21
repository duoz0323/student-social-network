import { useEffect, useRef } from 'react';
import Button from '../../../components/common/Button.jsx';
import { useGoogleAuth } from '../google/useGoogleAuth.js';

// Icon Google nhiều màu chuẩn brand
function GoogleIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4" />
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
    </svg>
  );
}

// Style đồng bộ với FacebookAuthButton – có hiệu ứng hover rõ ràng
const buttonClass = 'flex h-[42px] w-full items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white text-[13px] font-semibold text-gray-700 shadow-sm transition-all duration-200 hover:bg-gray-50 hover:border-violet-400 hover:shadow-md hover:scale-[1.02] active:scale-[0.98]';

export default function GoogleAuthButton(props) {
  const googleAuth = useGoogleAuth(props);
  const actionLabel = props.actionLabel ?? 'Tiếp tục với';
  // Container ẩn cho nút Google native – vẫn cần để SDK khởi tạo đúng
  const hiddenContainerRef = useRef(null);
  const { renderButton } = googleAuth;

  useEffect(() => {
    renderButton(hiddenContainerRef.current);
  }, [renderButton]);

  // Khi user click custom button, trigger click vào nút Google native ẩn bên dưới
  function handleClick() {
    const nativeButton = hiddenContainerRef.current?.querySelector('[role="button"], button, div[tabindex]');
    if (nativeButton) {
      nativeButton.click();
    }
  }

  // Trạng thái đang tải SDK
  if (googleAuth.isSdkLoading) return <Button type="button" variant="secondary" disabled className={buttonClass}><GoogleIcon /> Đang tải Google...</Button>;

  // SDK không tải được, hiện nút retry
  if (!googleAuth.isGoogleReady) return (
    <div>
      <button
        type="button"
        disabled={!googleAuth.isConfigured}
        onClick={googleAuth.retrySdk}
        className={buttonClass}
      >
        <GoogleIcon />
        {googleAuth.isConfigured ? 'Thử tải lại Google' : `${actionLabel} Google`}
      </button>
      {googleAuth.error ? <p className="mt-2 text-xs font-semibold text-red-700">{googleAuth.error}</p> : null}
    </div>
  );

  // SDK đã sẵn sàng – hiện custom button đồng bộ kiểu Facebook, nút native ẩn phía sau
  return (
    <div className="relative">
      {/* Container ẩn chứa nút Google native – cần cho SDK initialize/callback */}
      <div
        ref={hiddenContainerRef}
        className="absolute inset-0 overflow-hidden opacity-0 pointer-events-none"
        aria-hidden="true"
      />
      {/* Custom button hiển thị – style đồng nhất với Facebook */}
      <button
        type="button"
        onClick={handleClick}
        disabled={googleAuth.isAuthenticating}
        className={buttonClass}
      >
        <GoogleIcon />
        {`${actionLabel} Google`}
      </button>
      {googleAuth.isAuthenticating ? <p className="mt-2 text-center text-xs font-semibold text-zinc-600">Đang xác minh...</p> : null}
      {googleAuth.error ? <p className="mt-2 text-xs font-semibold text-red-700">{googleAuth.error}</p> : null}
    </div>
  );
}
