import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';
import SocialAuthButtons from '../components/SocialAuthButtons.jsx';

export default function LoginPage() {
  const { login, handleFutureSocialAuth } = useApp();
  const navigate = useNavigate();
  const [form, setForm] = useState({ identifier: '', password: '' });
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit(event) {
    event.preventDefault();
    if (!form.identifier.trim() || !form.password) {
      setMessage('Vui long nhap email/so dien thoai va mat khau.');
      return;
    }

    setSubmitting(true);
    setMessage('');
    const result = await login(form.identifier, form.password);
    setSubmitting(false);

    if (!result.ok) {
      setMessage(result.message);
      return;
    }

    if (!result.profileCompleted) {
      navigate('/onboarding/profile');
      return;
    }
    navigate(result.role === 'ADMIN' ? '/admin' : '/feed/for-you');
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setMessage(result.message);
  }

  return (
    <main className="auth-pattern flex min-h-screen items-center justify-center px-4 py-8">
      <form
        onSubmit={submit}
        className="stitch-card-shadow w-full max-w-[390px] rounded-[10px] border border-[var(--app-border)] bg-white px-9 py-8"
      >
        <div className="mb-6 flex flex-col items-center text-center">
          <img src={logo} alt="UniShare" className="h-16 w-16 object-contain" />
          <h1 className="mt-2 text-base font-black text-[var(--app-text)]">UniShare</h1>
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

        <button
          type="button"
          className="mt-3 block w-full text-right text-xs font-black text-[var(--app-text)]"
          onClick={() => setMessage('Tinh nang dang duoc phat trien.')}
        >
          Quen mat khau?
        </button>

        {message ? <p className="mt-3 rounded-xl bg-zinc-100 px-3 py-2 text-sm font-semibold text-zinc-700">{message}</p> : null}

        <Button type="submit" disabled={submitting} className="mt-5 min-h-[48px] w-full text-sm font-black" size="lg">
          {submitting ? 'Dang xu ly...' : 'Dang nhap'}
        </Button>

        <div className="my-5 flex items-center gap-3 text-xs text-[var(--app-muted)]">
          <span className="h-px flex-1 bg-[var(--app-border)]" />
          Hoac
          <span className="h-px flex-1 bg-[var(--app-border)]" />
        </div>

        <SocialAuthButtons onUnavailable={showFutureMessage} />

        <p className="mt-6 text-center text-xs text-[var(--app-text)]">
          Chua co tai khoan? <Link to="/register" className="font-black">Dang ky</Link>
        </p>
      </form>
    </main>
  );
}
