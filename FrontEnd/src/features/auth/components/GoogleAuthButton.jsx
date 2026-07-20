import { useEffect, useRef } from 'react';
import Button from '../../../components/common/Button.jsx';
import { useGoogleAuth } from '../google/useGoogleAuth.js';

export default function GoogleAuthButton(props) {
  const containerRef = useRef(null);
  const googleAuth = useGoogleAuth(props);
  const { renderButton } = googleAuth;

  useEffect(() => {
    const container = containerRef.current;
    renderButton(container);
    return () => {
      if (container) container.textContent = '';
    };
  }, [renderButton]);

  if (googleAuth.isSdkLoading) return <Button type="button" variant="secondary" disabled className="min-h-[44px] w-full">Đang tải Google...</Button>;
  if (!googleAuth.isGoogleReady) return (
    <div>
      <Button type="button" variant="secondary" disabled={!googleAuth.isConfigured} onClick={googleAuth.retrySdk} className="min-h-[44px] w-full">
        {googleAuth.isConfigured ? 'Thử tải lại Google' : 'Tiếp tục với Google'}
      </Button>
      {googleAuth.error ? <p className="mt-2 text-xs font-semibold text-red-700">{googleAuth.error}</p> : null}
    </div>
  );

  return (
    <div>
      <div ref={containerRef} aria-label="Tiếp tục với Google" className={googleAuth.isAuthenticating ? 'pointer-events-none opacity-60' : ''} />
      {googleAuth.isAuthenticating ? <p className="mt-2 text-center text-xs font-semibold text-zinc-600">Đang xác minh với Backend...</p> : null}
      {googleAuth.error ? <p className="mt-2 text-xs font-semibold text-red-700">{googleAuth.error}</p> : null}
    </div>
  );
}
