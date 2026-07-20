import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import SocialAuthButtons from '../components/SocialAuthButtons.jsx';
import { useRegistration } from '../hooks/useRegistration.js';
import { getRegistrationErrorMessage } from '../utils/registrationErrorMapper.js';
import { validateRegistration } from '../validation/registrationValidation.js';
import { getAuthenticatedHome } from '../utils/authNavigation.js';

export default function RegisterPage() {
  const { handleFutureSocialAuth } = useApp();
  const registration = useRegistration();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ identifier: '', password: '', confirmPassword: '', acceptTerms: false });
  const [message, setMessage] = useState(() => location.state?.reason ? 'Bạn có thể bắt đầu hoặc tiếp tục một đăng ký đang chờ.' : '');
  const [fieldErrors, setFieldErrors] = useState({});

  async function submit(event) {
    event.preventDefault();
    const validationErrors = validateRegistration(form);
    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      return;
    }

    setFieldErrors({});
    setMessage('');
    try {
      const flow = await registration.startRegistration({
        identifier: form.identifier.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
      });
      if (flow) navigate('/register/verify', { replace: true });
    } catch (error) {
      setFieldErrors(error.fieldErrors ?? {});
      setMessage(getRegistrationErrorMessage(error));
    } finally {
      // Password và confirmPassword bị loại khỏi React state ngay sau khi request kết thúc.
      setForm((current) => ({ ...current, password: '', confirmPassword: '' }));
    }
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setMessage(result.message);
  }

  function finishGoogleAuthentication(session) {
    navigate(getAuthenticatedHome(session), { replace: true });
  }

  return (
    <main className="auth-pattern flex min-h-screen flex-col items-center justify-center px-4 py-8">
      <form
        onSubmit={submit}
        className="stitch-card-shadow w-full max-w-[390px] rounded-[10px] border border-[var(--app-border)] bg-white px-9 py-8"
      >
        <div className="mb-6 flex flex-col items-center text-center">
          <img src={logo} alt="UniShare" className="h-14 w-14 object-contain" />
          <h1 className="mt-1 text-base font-black text-[var(--app-text)]">UniShare</h1>
        </div>

        <label className="block text-xs font-semibold text-zinc-700">
          Email hoac so dien thoai
          <input
            value={form.identifier}
            onChange={(event) => setForm({ ...form, identifier: event.target.value })}
            placeholder="Nhap email hoac so dien thoai"
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            disabled={registration.isSubmitting}
          />
          {fieldErrors.identifier ? <span className="mt-1 block text-xs text-red-700">{fieldErrors.identifier}</span> : null}
        </label>

        <label className="mt-4 block text-xs font-semibold text-zinc-700">
          Mat khau
          <input
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            autoComplete="new-password"
            disabled={registration.isSubmitting}
          />
          {fieldErrors.password ? <span className="mt-1 block text-xs text-red-700">{fieldErrors.password}</span> : null}
        </label>

        <label className="mt-4 block text-xs font-semibold text-zinc-700">
          Xac nhan mat khau
          <input
            type="password"
            value={form.confirmPassword}
            onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            autoComplete="new-password"
            disabled={registration.isSubmitting}
          />
          {fieldErrors.confirmPassword ? <span className="mt-1 block text-xs text-red-700">{fieldErrors.confirmPassword}</span> : null}
        </label>

        <p className="mt-3 text-xs leading-5 text-[var(--app-muted)]">
          Mat khau toi thieu 8 ky tu, gom chu hoa, chu thuong, chu so va ky tu dac biet.
        </p>

        <label className="mt-4 flex items-start gap-2 text-xs font-semibold text-zinc-700">
          <input
            type="checkbox"
            checked={form.acceptTerms}
            onChange={(event) => setForm({ ...form, acceptTerms: event.target.checked })}
            className="mt-0.5"
            disabled={registration.isSubmitting}
          />
          <span>Toi dong y voi dieu khoan su dung UniShare.</span>
        </label>
        {fieldErrors.acceptTerms ? <p className="mt-1 text-xs text-red-700">{fieldErrors.acceptTerms}</p> : null}

        {message ? <p className="mt-3 rounded-xl bg-zinc-100 px-3 py-2 text-sm font-semibold text-zinc-700">{message}</p> : null}

        <Button type="submit" disabled={registration.isSubmitting} className="mt-5 min-h-[48px] w-full text-sm font-black" size="lg">
          {registration.isSubmitting ? 'Đang tạo đăng ký...' : 'Đăng ký'}
        </Button>

        {registration.hasFlow ? (
          <button type="button" onClick={() => navigate('/register/verify')} className="mt-3 w-full text-sm font-black text-zinc-700">
            Tiếp tục xác minh đăng ký đang chờ
          </button>
        ) : null}

        <div className="my-5 flex items-center gap-3 text-xs text-[var(--app-muted)]">
          <span className="h-px flex-1 bg-[var(--app-border)]" />
          Hoac
          <span className="h-px flex-1 bg-[var(--app-border)]" />
        </div>

        <SocialAuthButtons
          actionLabel="Đăng ký với"
          includeRegistrationFlow
          onUnavailable={showFutureMessage}
          onGoogleAuthenticated={finishGoogleAuthentication}
          onGoogleConflict={() => navigate('/auth/social-conflict', { replace: true })}
        />

        <p className="mt-6 text-center text-xs text-[var(--app-text)]">
          Đã có tài khoản? <Link to="/login" className="font-black">Đăng nhập</Link>
        </p>
      </form>
    </main>
  );
}
