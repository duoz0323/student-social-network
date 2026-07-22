import { Link } from 'react-router-dom';
import { useState } from 'react';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
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

// Lấy lỗi ưu tiên nhất để hiển thị – chỉ hiện 1 lỗi tại một thời điểm
function getDisplayError(message, fieldErrors, retrySeconds) {
  // Ưu tiên: general message > retry > field error đầu tiên
  if (message) return message;
  if (retrySeconds > 0) return `Có thể thử lại sau ${retrySeconds} giây.`;
  // Duyệt theo thứ tự field quan trọng nhất
  const fieldOrder = ['email', 'password', 'confirmPassword', 'acceptTerms'];
  for (const field of fieldOrder) {
    if (fieldErrors[field]) return fieldErrors[field];
  }
  return '';
}

// Form đăng nhập / đăng ký dùng chung, phân biệt qua prop `type`
export default function AuthForm({
  type = 'login',
  onSubmit,
  submitting,
  message,
  form,
  setForm,
  showFutureMessage,
  fieldErrors = {},
  retrySeconds = 0,
  onFieldChange,
  includeRegistrationFlow = false,
  hasPendingRegistration = false,
  onContinueRegistration,
  onGoogleAuthenticated,
  onGoogleConflict,
  onFacebookAuthenticated,
  onFacebookConflict,
}) {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const isLogin = type === 'login';

  // Lỗi hiển thị duy nhất – gộp tất cả nguồn lỗi, chỉ hiện 1
  const displayError = getDisplayError(message, fieldErrors, retrySeconds);

  // Style chung cho input – thêm viền đỏ khi field có lỗi (không chiếm thêm chỗ)
  function inputClass(fieldName) {
    const hasError = Boolean(fieldErrors[fieldName]);
    return (
      'h-[42px] w-full rounded-lg border bg-white px-3 text-sm text-gray-900 ' +
      'placeholder-gray-400 outline-none transition-all duration-200 ' +
      (hasError
        ? 'border-red-400 ring-2 ring-red-100 '
        : 'border-gray-300 focus:border-violet-600 focus:ring-2 focus:ring-violet-100 ')
    );
  }

  function updateField(name, value) {
    setForm({ ...form, [name]: value });
    onFieldChange?.(name);
  }

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

        {/* ═══ Vùng hiển thị lỗi duy nhất – chiều cao cố định, không đẩy layout ═══ */}
        <div
          className="overflow-hidden transition-all duration-300 ease-out"
          style={{
            maxHeight: displayError ? '80px' : '0px',
            opacity: displayError ? 1 : 0,
            marginBottom: displayError ? '12px' : '0px',
          }}
        >
          <div
            role="alert"
            className="rounded-lg border border-red-200 bg-red-50 px-3 py-2.5 text-[12px] font-semibold leading-relaxed text-red-700 flex items-start gap-2"
          >
            {/* Icon cảnh báo */}
            <svg className="w-4 h-4 text-red-500 flex-shrink-0 mt-0.5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
            <span>{displayError}</span>
          </div>
        </div>

        {/* Trường email cho xác thực local. */}
        <div className="mb-4">
          <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
            Email
          </label>
          <input
            value={form.email}
            onChange={(e) => updateField('email', e.target.value)}
            placeholder="student@example.com"
            className={inputClass('email')}
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
                onChange={(e) => updateField('password', e.target.value)}
                placeholder="Nhập mật khẩu"
                className={inputClass('password') + ' pr-10'}
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
            <div className="mt-2.5 text-right">
              <Link
                to="/forgot-password"
                className="text-sm font-bold transition-all duration-200"
                style={{ color: '#1e293b', textUnderlineOffset: '3px' }}
                onMouseEnter={(e) => { e.currentTarget.style.color = '#7c3aed'; e.currentTarget.style.textDecoration = 'underline'; }}
                onMouseLeave={(e) => { e.currentTarget.style.color = '#1e293b'; e.currentTarget.style.textDecoration = 'none'; }}
              >
                Quên mật khẩu?
              </Link>
            </div>
          </div>
        ) : (
          /* Đăng ký: 2 ô mật khẩu nằm ngang */
          <div className="mb-2">
            <div className="flex flex-col gap-3 sm:flex-row">
              {/* Mật khẩu */}
              <div className="flex-1">
                <label className="block text-[12px] font-semibold text-gray-700 mb-1.5">
                  Mật khẩu
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={form.password}
                    onChange={(e) => updateField('password', e.target.value)}
                    placeholder="Tạo mật khẩu"
                    className={inputClass('password') + ' pr-10'}
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
                    onChange={(e) => updateField('confirmPassword', e.target.value)}
                    placeholder="Nhập lại mật khẩu"
                    className={inputClass('confirmPassword') + ' pr-10'}
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

        {/* Hint mật khẩu + checkbox điều khoản (chỉ ở trang đăng ký) */}
        {isLogin ? null : (
          <>
            <p className="mt-2 mb-3 text-[12px] text-gray-500 font-medium text-left leading-relaxed">
              Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
            </p>
            <label className="flex items-start gap-2 text-[13px] font-medium text-gray-700 mb-4 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={form.acceptTerms}
                onChange={(e) => updateField('acceptTerms', e.target.checked)}
                className={`mt-0.5 h-4 w-4 rounded flex-shrink-0 ${fieldErrors.acceptTerms ? 'border-red-400 accent-red-500' : 'border-gray-300 accent-violet-700'}`}
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

        {/* Nút submit */}
        <div className="mb-4">
          <Button 
            type="submit" 
            disabled={submitting}
            className="w-full"
          >
            {submitting ? 'Đang xử lý...' : (isLogin ? 'Đăng nhập' : 'Tạo tài khoản')}
          </Button>
        </div>

        {!isLogin && hasPendingRegistration ? (
          <button
            type="button"
            onClick={onContinueRegistration}
            disabled={submitting}
            className="mb-4 w-full text-[13px] font-bold text-violet-700 hover:underline disabled:opacity-60"
          >
            Tiếp tục xác minh đăng ký đang chờ
          </button>
        ) : null}

        {/* Divider "Hoặc" */}
        <div className="flex items-center gap-3 mb-4">
          <span className="h-px flex-1 bg-gray-200" />
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Hoặc</span>
          <span className="h-px flex-1 bg-gray-200" />
        </div>

        {/* Nút mạng xã hội */}
        <SocialAuthButtons
          actionLabel={isLogin ? 'Tiếp tục với' : 'Đăng ký với'}
          onUnavailable={showFutureMessage}
          includeRegistrationFlow={includeRegistrationFlow}
          onGoogleAuthenticated={onGoogleAuthenticated}
          onGoogleConflict={onGoogleConflict}
          onFacebookAuthenticated={onFacebookAuthenticated}
          onFacebookConflict={onFacebookConflict}
        />

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
