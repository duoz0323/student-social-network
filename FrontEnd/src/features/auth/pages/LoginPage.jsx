import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import SocialAuthButtons from '../components/SocialAuthButtons.jsx';
import { useLogin } from '../hooks/useLogin.js';
import { getAuthenticatedHome, getSafeReturnPath } from '../utils/authNavigation.js';

export default function LoginPage() {
  const { handleFutureSocialAuth } = useApp();
  const loginState = useLogin();
  const navigate = useNavigate();
  const location = useLocation();
  const identifierRef = useRef(null);
  const passwordRef = useRef(null);
  const [form, setForm] = useState({ identifier: '', password: '' });
  const [auxiliaryMessage, setAuxiliaryMessage] = useState(() => {
    if (location.state?.reason === 'SESSION_EXPIRED') return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
    if (location.state?.reason === 'BLOCKED') return 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.';
    return '';
  });

  useEffect(() => {
    if (loginState.fieldErrors.identifier) identifierRef.current?.focus();
    else if (loginState.fieldErrors.password) passwordRef.current?.focus();
  }, [loginState.fieldErrors]);

  async function submit(event) {
    event.preventDefault();
    setAuxiliaryMessage('');
    try {
      const session = await loginState.login(form);
      if (!session) return;
      const returnPath = getSafeReturnPath(location.state?.from, session);
      setForm((current) => ({ ...current, password: '' }));
      navigate(returnPath ?? getAuthenticatedHome({ role: session.user.role, profileCompleted: session.profileCompleted }), { replace: true });
    } catch {
      // Hook đã ánh xạ ApiError; Page không hiển thị raw error hoặc giữ lại password.
      setForm((current) => ({ ...current, password: '' }));
    }
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setAuxiliaryMessage(result.message);
  }

  function finishGoogleAuthentication(session) {
    const returnPath = getSafeReturnPath(location.state?.from, session);
    navigate(returnPath ?? getAuthenticatedHome(session), { replace: true });
  }

  const disabled = loginState.isSubmitting || loginState.retrySeconds > 0;

  return (
    <main className="auth-pattern flex min-h-screen items-center justify-center px-4 py-8">
      <form onSubmit={submit} className="stitch-card-shadow w-full max-w-[390px] rounded-[10px] border border-[var(--app-border)] bg-white px-9 py-8">
        <div className="mb-6 flex flex-col items-center text-center">
          <img src={logo} alt="UniShare" className="h-16 w-16 object-contain" />
          <h1 className="mt-2 text-base font-black text-[var(--app-text)]">UniShare</h1>
        </div>

        <label className="block text-xs font-semibold text-zinc-700">
          Email hoặc số điện thoại
          <input
            ref={identifierRef}
            autoComplete="username"
            value={form.identifier}
            onChange={(event) => { setForm({ ...form, identifier: event.target.value }); loginState.clearError(); }}
            placeholder="Nhập email hoặc số điện thoại"
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            disabled={disabled}
          />
          {loginState.fieldErrors.identifier ? <span className="mt-1 block text-xs text-red-700">{loginState.fieldErrors.identifier}</span> : null}
        </label>

        <label className="mt-4 block text-xs font-semibold text-zinc-700">
          Mật khẩu
          <input
            ref={passwordRef}
            type="password"
            autoComplete="current-password"
            value={form.password}
            onChange={(event) => { setForm({ ...form, password: event.target.value }); loginState.clearError(); }}
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            disabled={disabled}
          />
          {loginState.fieldErrors.password ? <span className="mt-1 block text-xs text-red-700">{loginState.fieldErrors.password}</span> : null}
        </label>

        <button type="button" className="mt-3 block w-full text-right text-xs font-black text-[var(--app-text)]" onClick={() => setAuxiliaryMessage('Tính năng đang được phát triển.')}>
          Quên mật khẩu?
        </button>

        {loginState.generalError ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-red-700">{loginState.generalError}</p> : null}
        {loginState.retrySeconds > 0 ? <p className="mt-2 text-center text-xs font-semibold text-zinc-600">Có thể thử lại sau {loginState.retrySeconds} giây.</p> : null}
        {auxiliaryMessage ? <p className="mt-3 rounded-xl bg-zinc-100 px-3 py-2 text-sm font-semibold text-zinc-700">{auxiliaryMessage}</p> : null}

        <Button type="submit" disabled={disabled} className="mt-5 min-h-[48px] w-full text-sm font-black" size="lg">
          {loginState.isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </Button>

        <div className="my-5 flex items-center gap-3 text-xs text-[var(--app-muted)]">
          <span className="h-px flex-1 bg-[var(--app-border)]" />
          Hoặc
          <span className="h-px flex-1 bg-[var(--app-border)]" />
        </div>

        <SocialAuthButtons
          onUnavailable={showFutureMessage}
          onGoogleAuthenticated={finishGoogleAuthentication}
          onGoogleConflict={() => navigate('/auth/social-conflict', { replace: true })}
        />
        <p className="mt-6 text-center text-xs text-[var(--app-text)]">Chưa có tài khoản? <Link to="/register" className="font-black">Đăng ký</Link></p>
      </form>
    </main>
  );
}
