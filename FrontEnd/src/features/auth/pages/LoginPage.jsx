import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function LoginPage() {
  const { login } = useApp();
  const navigate = useNavigate();
  const [form, setForm] = useState({ identifier: 'minh.demo@example.com', password: 'DemoUser123!' });
  const [error, setError] = useState('');

  function submit(event) {
    event.preventDefault();
    const result = login(form.identifier, form.password);
    if (!result.ok) {
      setError(result.message);
      return;
    }
    navigate(result.role === 'ADMIN' ? '/admin' : '/feed/for-you');
  }

  return (
    <main className="auth-pattern flex min-h-screen items-center justify-center px-4 py-8">
      <form
        onSubmit={submit}
        className="stitch-card-shadow w-full max-w-[420px] rounded-[18px] border border-[var(--app-border)] bg-white/95 px-8 py-9 sm:px-9"
      >
        <div className="mb-8 flex flex-col items-center text-center">
          <img src={logo} alt="UniShare" className="h-24 w-24 object-contain" />
          <h1 className="mt-3 text-[34px] font-black leading-tight text-[var(--app-text)]">UniShare</h1>
        </div>
        <input
          value={form.identifier}
          onChange={(event) => setForm({ ...form, identifier: event.target.value })}
          placeholder="Email hoac so dien thoai"
          className="h-[54px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-white px-4 text-[15px] outline-none transition focus:border-[var(--app-text)] focus:ring-4 focus:ring-black/5"
        />
        <input
          type="password"
          value={form.password}
          onChange={(event) => setForm({ ...form, password: event.target.value })}
          placeholder="Mat khau"
          className="mt-4 h-[54px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] bg-white px-4 text-[15px] outline-none transition focus:border-[var(--app-text)] focus:ring-4 focus:ring-black/5"
        />
        {error ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
        <Button type="submit" className="mt-7 min-h-[52px] w-full text-[15px] font-black" size="lg">Dang nhap</Button>
        <p className="mt-7 text-center text-[15px] text-[var(--app-text)]">
          Chua co tai khoan? <Link to="/register" className="font-black text-[var(--app-text)]">Dang ky</Link>
        </p>
      </form>
    </main>
  );
}
