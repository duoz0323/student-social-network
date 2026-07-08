import { Link } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';

import SocialAuthButtons from './SocialAuthButtons.jsx';

// Icon mắt hiện/ẩn mật khẩu
function EyeIcon({ open }) {
  return (
    <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      {open ? (
        <>
          <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
          <circle cx="12" cy="12" r="3" />
        </>
      ) : (
        <>
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
          <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
          <line x1="1" y1="1" x2="23" y2="23" />
        </>
      )}
    </svg>
  );
}

// Form đăng nhập / đăng ký dùng chung, phân biệt qua prop `type`
export default function AuthForm({
  type = 'login',
  onSubmit,
  submitting,
  message,
  form,
  setForm,
  showFutureMessage
}) {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const isLogin = type === 'login';

  // Style chung cho input
  const inputCls =
    'h-[42px] w-full rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-900 ' +
    'placeholder-gray-400 outline-none transition ' +
    'focus:border-violet-600 focus:ring-2 focus:ring-violet-100';

  return (
    <div className="px-7 sm:px-10 py-6">
      {/* Header form: logo + tiêu đề */}
      <div className="mb-5 flex flex-col items-center text-center">
        <img
          src={logo}
          alt="UniShare"
          className="h-16 w-16 object-contain mb-3"
        />
        <h2 className="text-[1.35rem] font-bold text-gray-900 mb-1">
          {isLogin ? 'Đăng nhập UniShare' : 'Tạo tài khoản UniShare'}
        </h2>
        <p className="text-[13px] text-gray-500">
          {isLogin
            ? 'Đăng nhập để kết nối với bạn bè và chia sẻ.'
            : 'Tạo tài khoản để kết nối với bạn bè và chia sẻ.'}
        </p>
      </div>

      <form onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>

        {/* Field: Email hoặc số điện thoại */}
        <div className="mb-4">
          <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
            Email hoặc số điện thoại
          </label>
          <input
            value={form.identifier}
            onChange={(e) => setForm({ ...form, identifier: e.target.value })}
            placeholder="Nhập email hoặc số điện thoại"
            className={inputCls}
            disabled={submitting}
            autoComplete="username"
          />
        </div>

        {/* Field mật khẩu – đăng nhập: 1 cột / đăng ký: 2 cột song song */}
        {isLogin ? (
          <div className="mb-2">
            <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
              Mật khẩu
            </label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                placeholder="Nhập mật khẩu"
                className={inputCls + ' pr-10'}
                disabled={submitting}
                autoComplete="current-password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                tabIndex={-1}
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
              >
                <EyeIcon open={showPassword} />
              </button>
            </div>
          </div>
        ) : (
          /* Đăng ký: 2 ô mật khẩu nằm ngang */
          <div className="mb-2">
            <div className="flex gap-3">
              {/* Mật khẩu */}
              <div className="flex-1">
                <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
                  Mật khẩu
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    placeholder="Tạo mật khẩu"
                    className={inputCls + ' pr-10'}
                    disabled={submitting}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                    tabIndex={-1}
                    aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    <EyeIcon open={showPassword} />
                  </button>
                </div>
              </div>

              {/* Xác nhận mật khẩu */}
              <div className="flex-1">
                <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
                  Xác nhận mật khẩu
                </label>
                <div className="relative">
                  <input
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={form.confirmPassword}
                    onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                    placeholder="Nhập lại mật khẩu"
                    className={inputCls + ' pr-10'}
                    disabled={submitting}
                    autoComplete="new-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                    tabIndex={-1}
                    aria-label={showConfirmPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    <EyeIcon open={showConfirmPassword} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Quên mật khẩu (login) hoặc hint + checkbox điều khoản (register) */}
        {isLogin ? (
          <div className="flex justify-end mb-4">
            <button
              type="button"
              className="text-[12px] font-semibold text-violet-700 hover:underline"
              onClick={() => showFutureMessage('Khôi phục mật khẩu')}
            >
              Quên mật khẩu?
            </button>
          </div>
        ) : (
          <>
            <p className="mt-2 mb-3 text-[12px] text-gray-500 font-medium text-left leading-relaxed">
              Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
            </p>
            <label className="flex items-start gap-2 text-[13px] font-medium text-gray-700 mb-4 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={form.acceptTerms}
                onChange={(e) => setForm({ ...form, acceptTerms: e.target.checked })}
                className="mt-0.5 h-4 w-4 rounded border-gray-300 accent-violet-700 flex-shrink-0"
                disabled={submitting}
              />
              <span>
                Tôi đồng ý với{' '}
                <span className="text-violet-700 font-semibold hover:underline cursor-pointer">
                  điều khoản sử dụng
                </span>
                .
              </span>
            </label>
          </>
        )}

        {/* Thông báo lỗi */}
        {message && (
          <div className="mb-4 p-2.5 bg-red-50 text-red-600 text-[12px] font-semibold rounded-lg border border-red-100">
            {message}
          </div>
        )}

        {/* Nút submit */}
        <button
          type="submit"
          disabled={submitting}
          className="w-full h-[44px] rounded-full bg-[#0f172a] hover:bg-black text-white text-sm font-semibold mb-4 shadow-sm transition-colors disabled:opacity-60 disabled:cursor-not-allowed"
        >
          {submitting ? 'Đang xử lý...' : (isLogin ? 'Đăng nhập' : 'Tạo tài khoản')}
        </button>

        {/* Divider "Hoặc" */}
        <div className="flex items-center gap-3 mb-4">
          <span className="h-px flex-1 bg-gray-200" />
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Hoặc</span>
          <span className="h-px flex-1 bg-gray-200" />
        </div>

        {/* Nút mạng xã hội */}
        <SocialAuthButtons actionLabel="Tiếp tục với" onUnavailable={showFutureMessage} />

        {/* Link chuyển trang */}
        <p className="mt-5 mb-1 text-center text-[13px] font-medium text-gray-600">
          {isLogin ? (
            <>Chưa có tài khoản? <Link to="/register" className="text-violet-700 font-bold hover:underline">Đăng ký ngay</Link></>
          ) : (
            <>Đã có tài khoản? <Link to="/login" className="text-violet-700 font-bold hover:underline">Đăng nhập ngay</Link></>
          )}
        </p>
      </form>
    </div>
  );
}
