import { useState } from 'react';

export default function NewPasswordForm({ onSubmit, disabled, error, fieldErrors }) {
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' });
  const [visible, setVisible] = useState(false);
  const update = (name, value) => setForm((current) => ({ ...current, [name]: value }));

  return (
    <form onSubmit={(event) => { event.preventDefault(); onSubmit(form); }} className="px-7 py-8 sm:px-10">
      <h2 className="text-center text-2xl font-black text-gray-900">Đặt mật khẩu mới</h2>
      <p className="mt-2 text-center text-sm leading-6 text-gray-500">Dùng 8–72 ký tự, gồm chữ thường, chữ hoa, chữ số và ký tự đặc biệt.</p>
      <label className="mt-6 block text-sm font-bold text-gray-700">Mật khẩu mới
        <input autoFocus type={visible ? 'text' : 'password'} autoComplete="new-password" value={form.newPassword} disabled={disabled} onChange={(event) => update('newPassword', event.target.value)}
          className="mt-2 h-12 w-full rounded-xl border border-gray-300 px-4 outline-none focus:border-violet-600 focus:ring-2 focus:ring-violet-100" />
      </label>
      {fieldErrors.newPassword ? <p className="mt-1 text-xs font-semibold text-red-700">{fieldErrors.newPassword}</p> : null}
      <label className="mt-4 block text-sm font-bold text-gray-700">Xác nhận mật khẩu
        <input type={visible ? 'text' : 'password'} autoComplete="new-password" value={form.confirmPassword} disabled={disabled} onChange={(event) => update('confirmPassword', event.target.value)}
          className="mt-2 h-12 w-full rounded-xl border border-gray-300 px-4 outline-none focus:border-violet-600 focus:ring-2 focus:ring-violet-100" />
      </label>
      {fieldErrors.confirmPassword ? <p className="mt-1 text-xs font-semibold text-red-700">{fieldErrors.confirmPassword}</p> : null}
      <label className="mt-4 flex items-center gap-2 text-sm font-semibold text-gray-600"><input type="checkbox" checked={visible} onChange={(event) => setVisible(event.target.checked)} /> Hiển thị mật khẩu</label>
      {error ? <p role="alert" className="mt-4 rounded-xl bg-red-50 p-3 text-sm font-semibold text-red-700">{error}</p> : null}
      <button type="submit" disabled={disabled} className="mt-6 h-12 w-full rounded-xl bg-violet-700 font-bold text-white disabled:cursor-not-allowed disabled:opacity-60">{disabled ? 'Đang đặt lại...' : 'Đặt lại mật khẩu'}</button>
    </form>
  );
}
