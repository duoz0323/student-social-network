import { useNavigate } from 'react-router-dom';
import Button from '../../../components/common/Button.jsx';

export default function RecoveryIdentifierForm({ email, onChange, onSubmit, disabled, error, fieldError }) {
  const navigate = useNavigate();
  return (
    <form onSubmit={(event) => { event.preventDefault(); onSubmit(); }} className="px-7 py-8 sm:px-10">
      <h1 className="text-2xl font-bold text-gray-900">Quên mật khẩu</h1>
      <p className="mt-2 text-sm text-gray-500">Nhập email đã xác minh để nhận mã OTP.</p>
      
      <label className="mt-7 block text-sm font-medium text-gray-700">
        Email
        <input 
          autoFocus 
          type="email" 
          autoComplete="email" 
          value={email} 
          disabled={disabled}
          onChange={(event) => onChange(event.target.value)} 
          placeholder="student@example.com"
          className="mt-2 h-12 w-full rounded-xl border border-gray-300 px-4 text-gray-900 outline-none transition-all duration-200 focus:border-gray-900 focus:ring-1 focus:ring-gray-900"
          aria-invalid={Boolean(fieldError)} 
        />
      </label>
      
      {fieldError ? <p className="mt-2 text-sm text-red-600">{fieldError}</p> : null}
      {error ? <p className="mt-2 text-sm text-red-600">{error}</p> : null}
      
      <div className="mt-8">
        <Button 
          type="submit" 
          disabled={disabled || !email}
          loading={disabled}
          loadingLabel="Đang gửi mã..."
          className="w-full"
        >
          Gửi mã xác minh
        </Button>
      </div>
      
      <div className="mt-3">
        <Button type="button" onClick={() => navigate('/login')} variant="secondary" className="w-full">
          Quay lại đăng nhập
        </Button>
      </div>
    </form>
  );
}
