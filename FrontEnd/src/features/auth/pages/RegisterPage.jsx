import { Link, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

function validate(form) {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Email khong hop le.';
  if (!/^\+?\d{9,15}$/.test(form.phoneNumber)) return 'So dien thoai khong hop le.';
  if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/.test(form.password)) return 'Mat khau can toi thieu 8 ky tu, co chu, so va ky tu dac biet.';
  if (form.password !== form.confirmPassword) return 'Mat khau xac nhan khong khop.';
  return '';
}

export default function RegisterPage() {
  const { register } = useApp();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', phoneNumber: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');

  function submit(event) {
    event.preventDefault();
    const validationError = validate(form);
    if (validationError) {
      setError(validationError);
      return;
    }
    const result = register(form);
    if (!result.ok) {
      setError(result.message);
      return;
    }
    navigate('/register/success');
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-4">
      <form onSubmit={submit} className="w-full max-w-md rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
        <div className="mb-8 flex items-center gap-3">
          <img src={logo} alt="UniShare" className="h-12 w-12 rounded-xl object-contain" />
          <div>
            <h1 className="text-2xl font-black text-zinc-950">Dang ky</h1>
            <p className="text-sm text-zinc-500">Khong dung username trong MVP.</p>
          </div>
        </div>
        {['email', 'phoneNumber', 'password', 'confirmPassword'].map((field) => (
          <label key={field} className="mt-4 block text-sm font-semibold text-zinc-700">
            {field === 'phoneNumber' ? 'So dien thoai' : field === 'confirmPassword' ? 'Xac nhan mat khau' : field === 'password' ? 'Mat khau' : 'Email'}
            <input
              type={field.includes('password') || field.includes('Password') ? 'password' : 'text'}
              value={form[field]}
              onChange={(event) => setForm({ ...form, [field]: event.target.value })}
              className="mt-2 w-full rounded-2xl border border-zinc-200 px-4 py-3 outline-none focus:border-zinc-950"
            />
          </label>
        ))}
        {error ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
        <Button type="submit" className="mt-6 w-full" size="lg">Tao tai khoan</Button>
        <p className="mt-5 text-center text-sm text-zinc-500">
          Da co tai khoan? <Link to="/login" className="font-bold text-zinc-950">Dang nhap</Link>
        </p>
      </form>
    </main>
  );
}
