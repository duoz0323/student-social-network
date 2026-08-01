import { useState } from 'react';
import { Link } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import PasswordVisibilityIcon from './PasswordVisibilityIcon.jsx';

function PasswordField({ label, name, value, visible, error, disabled, autoFocus, onChange, onToggle }) {
  const inputClass = error
    ? 'border-red-400 ring-2 ring-red-100'
    : 'border-gray-300 focus:border-violet-600 focus:ring-2 focus:ring-violet-100';

  return (
    <div className="flex-1">
      <label htmlFor={name} className="mb-1.5 block text-[12px] font-semibold text-gray-700">
        {label}
      </label>
      <div className="relative">
        <input
          id={name}
          name={name}
          autoFocus={autoFocus}
          type={visible ? 'text' : 'password'}
          autoComplete="new-password"
          value={value}
          disabled={disabled}
          onChange={(event) => onChange(name, event.target.value)}
          placeholder={name === 'newPassword' ? 'Tạo mật khẩu mới' : 'Nhập lại mật khẩu'}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${name}-error` : undefined}
          className={`h-[42px] w-full rounded-lg border bg-white px-3 pr-10 text-sm text-gray-900 outline-none transition-all duration-200 placeholder:text-gray-400 ${inputClass}`}
        />
        <button
          type="button"
          onClick={onToggle}
          disabled={disabled}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 transition-colors hover:text-gray-600 disabled:cursor-not-allowed disabled:opacity-50"
          aria-label={visible ? `Ẩn ${label.toLowerCase()}` : `Hiện ${label.toLowerCase()}`}
        >
          <PasswordVisibilityIcon visible={visible} />
        </button>
      </div>
      {error ? <p id={`${name}-error`} className="mt-1.5 text-[12px] font-medium text-red-700">{error}</p> : null}
    </div>
  );
}

export default function NewPasswordForm({ onSubmit, onFieldChange, disabled, error, fieldErrors = {} }) {
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' });
  const [visibility, setVisibility] = useState({ newPassword: false, confirmPassword: false });

  function update(name, value) {
    setForm((current) => ({ ...current, [name]: value }));
    onFieldChange?.(name);
  }

  function toggleVisibility(name) {
    setVisibility((current) => ({ ...current, [name]: !current[name] }));
  }

  return (
    <div className="px-4 py-4 sm:px-8 sm:py-6">
      <div className="mb-5 flex flex-col items-center text-center">
        <img src={logo} alt="UniShare" className="mb-3 h-16 w-16 object-contain" />
        <h2 className="mb-1 text-[1.35rem] font-bold text-gray-900">Đặt mật khẩu mới</h2>
        <p className="text-[13px] text-gray-500">Chọn mật khẩu mới để tiếp tục sử dụng tài khoản.</p>
      </div>

      <form onSubmit={(event) => { event.preventDefault(); onSubmit(form); }} noValidate>
        {error ? (
          <div role="alert" className="mb-4 flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-[12px] font-semibold leading-relaxed text-red-700">
            <svg className="mt-0.5 h-4 w-4 flex-shrink-0 text-red-500" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
            <span>{error}</span>
          </div>
        ) : null}

        <div className="flex flex-col gap-3 sm:flex-row">
          <PasswordField
            label="Mật khẩu mới"
            name="newPassword"
            value={form.newPassword}
            visible={visibility.newPassword}
            error={fieldErrors.newPassword}
            disabled={disabled}
            autoFocus
            onChange={update}
            onToggle={() => toggleVisibility('newPassword')}
          />
          <PasswordField
            label="Xác nhận mật khẩu"
            name="confirmPassword"
            value={form.confirmPassword}
            visible={visibility.confirmPassword}
            error={fieldErrors.confirmPassword}
            disabled={disabled}
            onChange={update}
            onToggle={() => toggleVisibility('confirmPassword')}
          />
        </div>

        <p className="mb-4 mt-2 text-left text-[12px] font-medium leading-relaxed text-gray-500">
          8–72 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
        </p>

        <Button type="submit" disabled={disabled} className="w-full">
          {disabled ? 'Đang cập nhật...' : 'Cập nhật mật khẩu'}
        </Button>

        <p className="mb-1 mt-5 text-center text-[13px] font-medium text-gray-600">
          Muốn quay lại?{' '}
          <Link to="/login" className="font-bold text-violet-700 hover:underline">Đăng nhập</Link>
        </p>
      </form>
    </div>
  );
}
