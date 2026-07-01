import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import SocialAuthButtons from '../components/SocialAuthButtons.jsx';

function isEmail(value) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function isPhoneNumber(value) {
  return /^\+?\d{9,15}$/.test(value);
}

function validate(form) {
  const identifier = form.identifier.trim();
  if (!identifier) return 'Vui long nhap email hoac so dien thoai.';
  if (!isEmail(identifier) && !isPhoneNumber(identifier)) return 'Email hoac so dien thoai khong hop le.';
  if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/.test(form.password)) {
    return 'Mat khau can toi thieu 8 ky tu, co chu hoa, chu thuong, so va ky tu dac biet.';
  }
  if (form.password !== form.confirmPassword) return 'Mat khau xac nhan khong khop.';
  if (!form.acceptTerms) return 'Vui long dong y dieu khoan truoc khi dang ky.';
  return '';
}

export default function RegisterPage() {
  const { register, handleFutureSocialAuth } = useApp();
  const navigate = useNavigate();
  const [form, setForm] = useState({ identifier: '', password: '', confirmPassword: '', acceptTerms: false });
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const validationError = validate(form);
    if (validationError) {
      setMessage(validationError);
      return;
    }

    setSubmitting(true);
    setMessage('');
    const result = await register(form);
    setSubmitting(false);

    if (!result.ok) {
      setMessage(result.message);
      return;
    }
    navigate('/onboarding/profile');
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setMessage(result.message);
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
            disabled={submitting}
          />
        </label>

        <label className="mt-4 block text-xs font-semibold text-zinc-700">
          Mat khau
          <input
            type="password"
            value={form.password}
            onChange={(event) => setForm({ ...form, password: event.target.value })}
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            disabled={submitting}
          />
        </label>

        <label className="mt-4 block text-xs font-semibold text-zinc-700">
          Xac nhan mat khau
          <input
            type="password"
            value={form.confirmPassword}
            onChange={(event) => setForm({ ...form, confirmPassword: event.target.value })}
            className="mt-2 h-[44px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-zinc-50 px-4 text-sm outline-none focus:border-[var(--app-text)]"
            disabled={submitting}
          />
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
            disabled={submitting}
          />
          <span>Toi dong y voi dieu khoan su dung UniShare.</span>
        </label>

        {message ? <p className="mt-3 rounded-xl bg-zinc-100 px-3 py-2 text-sm font-semibold text-zinc-700">{message}</p> : null}

        <Button type="submit" disabled={submitting} className="mt-5 min-h-[48px] w-full text-sm font-black" size="lg">
          {submitting ? 'Dang xu ly...' : 'Dang ky'}
        </Button>

        <div className="my-5 flex items-center gap-3 text-xs text-[var(--app-muted)]">
          <span className="h-px flex-1 bg-[var(--app-border)]" />
          Hoac
          <span className="h-px flex-1 bg-[var(--app-border)]" />
        </div>

        <SocialAuthButtons actionLabel="Dang ky voi" onUnavailable={showFutureMessage} />

        <p className="mt-6 text-center text-xs text-[var(--app-text)]">
          Đã có tài khoản? <Link to="/login" className="font-black">Đăng nhập</Link>
        </p>
      </form>
    </main>
  );
}
