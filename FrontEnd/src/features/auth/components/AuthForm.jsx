import { Link } from 'react-router-dom';
import { useState } from 'react';
import { CheckCircle2, CircleAlert, LockKeyhole, Mail } from 'lucide-react';
import Button from '../../../components/common/Button.jsx';
import SocialAuthButtons from './SocialAuthButtons.jsx';
import PasswordVisibilityIcon from './PasswordVisibilityIcon.jsx';

// Icon mắt hiện/ẩn mật khẩu
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
  successMessage = '',
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
  // Lỗi luôn được ưu tiên; thông báo thành công chỉ xuất hiện khi form không có lỗi hiện hành.
  const noticeMessage = displayError || successMessage;
  const isSuccessNotice = !displayError && Boolean(successMessage);

  // Style chung cho input, giữ trạng thái focus và lỗi dễ nhận biết.
  function inputClass(fieldName) {
    const hasError = Boolean(fieldErrors[fieldName]);
    return (
      'h-12 w-full rounded-[10px] border px-11 text-sm font-normal text-zinc-950 sm:h-[52px] sm:px-12 sm:text-[15px] ' +
      'placeholder-zinc-400 outline-none transition-[border-color,box-shadow,background-color] duration-200 disabled:cursor-not-allowed disabled:opacity-60 ' +
      (hasError
        ? 'border-red-400 bg-red-50/40 ring-2 ring-red-100 '
        : 'border-zinc-200 focus:border-violet-600 focus:ring-4 focus:ring-zinc-200/70 ')
    );
  }

  function updateField(name, value) {
    setForm({ ...form, [name]: value });
    onFieldChange?.(name);
  }

  return (
    <div>
      <div className="mb-4 sm:mb-5">
        <h2 className="mb-1.5 text-[1.6rem] font-medium leading-tight tracking-[-0.01em] text-zinc-950 sm:text-[1.85rem]">
          {isLogin ? 'Chào mừng trở lại!' : 'Tạo tài khoản mới'}
        </h2>
        <p className="text-[13px] leading-5 text-zinc-500 sm:text-sm">
          {isLogin
            ? 'Đăng nhập để kết nối với bạn bè và tiếp tục hành trình học tập.'
            : 'Bắt đầu hành trình học hỏi, chia sẻ và kết nối cùng sinh viên.'}
        </p>
      </div>

      <form onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>

        {/* Vùng phản hồi dùng đúng ngữ nghĩa và màu sắc cho trạng thái lỗi hoặc thành công. */}
        <div
          className="overflow-hidden transition-all duration-300 ease-out"
          style={{
            maxHeight: noticeMessage ? '80px' : '0px',
            opacity: noticeMessage ? 1 : 0,
            marginBottom: noticeMessage ? '12px' : '0px',
          }}
        >
          <div
            role={isSuccessNotice ? 'status' : 'alert'}
            className={`flex items-start gap-2 rounded-[10px] border px-3.5 py-3 text-xs font-medium leading-relaxed ${isSuccessNotice ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-red-200 bg-red-50 text-red-700'}`}
          >
            {isSuccessNotice
              ? <CheckCircle2 className="mt-0.5 h-4 w-4 flex-shrink-0 text-emerald-600" aria-hidden="true" />
              : <CircleAlert className="mt-0.5 h-4 w-4 flex-shrink-0 text-red-500" aria-hidden="true" />}
            <span>{noticeMessage}</span>
          </div>
        </div>

        {/* Trường email cho xác thực local. */}
        <div className="mb-3">
          <label htmlFor={`${type}-email`} className="mb-1.5 block text-[13px] font-medium text-zinc-800 sm:text-sm">
            Email
          </label>
          <div className="relative">
            <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400" size={19} aria-hidden="true" />
            <input
              id={`${type}-email`}
              name="email"
              type="email"
              value={form.email}
              onChange={(e) => updateField('email', e.target.value)}
              placeholder="Nhập email của bạn"
              className={inputClass('email')}
              style={{ background: 'var(--auth-input-bg)', color: 'var(--app-text)' }}
              disabled={submitting}
              autoComplete="username"
              aria-invalid={Boolean(fieldErrors.email)}
            />
          </div>
        </div>

        {/* Field mật khẩu – đăng nhập: 1 cột / đăng ký: 2 cột song song */}
        {isLogin ? (
          <div className="mb-3">
            <label htmlFor="login-password" className="mb-1.5 block text-[13px] font-medium text-zinc-800 sm:text-sm">
              Mật khẩu
            </label>
            <div className="relative">
              <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400" size={19} aria-hidden="true" />
              <input
                id="login-password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                value={form.password}
                onChange={(e) => updateField('password', e.target.value)}
                placeholder="Nhập mật khẩu"
                className={inputClass('password') + ' pr-12'}
                style={{ background: 'var(--auth-input-bg)', color: 'var(--app-text)' }}
                disabled={submitting}
                autoComplete="current-password"
                aria-invalid={Boolean(fieldErrors.password)}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-4 top-1/2 -translate-y-1/2 rounded-md p-1 text-zinc-400 transition-colors hover:text-zinc-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950"
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
              >
                <PasswordVisibilityIcon visible={showPassword} />
              </button>
            </div>
            <div className="mt-2 text-right">
              <Link
                to="/forgot-password"
                className="inline-block text-[13px] font-medium text-zinc-500 transition-all duration-200 hover:text-zinc-950 hover:font-bold hover:underline hover:underline-offset-4 sm:text-sm"
              >
                Quên mật khẩu?
              </Link>
            </div>
          </div>
        ) : (
          <div className="mb-3">
            <div className="grid gap-3 sm:grid-cols-2">
              {/* Mật khẩu */}
              <div className="flex-1">
                <label htmlFor="register-password" className="mb-1.5 block text-[13px] font-medium text-zinc-800 sm:text-sm">
                  Mật khẩu
                </label>
                <div className="relative">
                  <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400" size={19} aria-hidden="true" />
                  <input
                    id="register-password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    value={form.password}
                    onChange={(e) => updateField('password', e.target.value)}
                    placeholder="Tạo mật khẩu"
                    className={inputClass('password') + ' pr-12'}
                    style={{ background: 'var(--auth-input-bg)', color: 'var(--app-text)' }}
                    disabled={submitting}
                    autoComplete="new-password"
                    aria-invalid={Boolean(fieldErrors.password)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-zinc-400 transition-colors hover:text-zinc-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950"
                    aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    <PasswordVisibilityIcon visible={showPassword} />
                  </button>
                </div>
              </div>

              {/* Xác nhận mật khẩu */}
              <div className="flex-1">
                <label htmlFor="register-confirm-password" className="mb-1.5 block text-[13px] font-medium text-zinc-800 sm:text-sm">
                  Xác nhận mật khẩu
                </label>
                <div className="relative">
                  <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400" size={19} aria-hidden="true" />
                  <input
                    id="register-confirm-password"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={form.confirmPassword}
                    onChange={(e) => updateField('confirmPassword', e.target.value)}
                    placeholder="Nhập lại mật khẩu"
                    className={inputClass('confirmPassword') + ' pr-12'}
                    style={{ background: 'var(--auth-input-bg)', color: 'var(--app-text)' }}
                    disabled={submitting}
                    autoComplete="new-password"
                    aria-invalid={Boolean(fieldErrors.confirmPassword)}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-zinc-400 transition-colors hover:text-zinc-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-950"
                    aria-label={showConfirmPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  >
                    <PasswordVisibilityIcon visible={showConfirmPassword} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Hint mật khẩu + checkbox điều khoản (chỉ ở trang đăng ký) */}
        {isLogin ? null : (
          <>
            <p className="mb-2 mt-1.5 text-[11px] font-medium leading-4 text-zinc-500 sm:text-xs">
              Tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.
            </p>
            <label className="mb-3 flex cursor-pointer select-none items-start gap-2 text-[13px] font-medium leading-5 text-zinc-700 sm:text-sm">
              <input
                type="checkbox"
                checked={form.acceptTerms}
                onChange={(e) => updateField('acceptTerms', e.target.checked)}
                className={`mt-0.5 h-4 w-4 flex-shrink-0 rounded ${fieldErrors.acceptTerms ? 'border-red-400 accent-red-500' : 'border-zinc-300 accent-zinc-950'}`}
                disabled={submitting}
              />
              <span>
                Tôi đồng ý với{' '}
                <span className="cursor-pointer font-medium text-zinc-950 hover:underline">
                  điều khoản sử dụng
                </span>
                .
              </span>
            </label>
          </>
        )}

        {/* Nút submit */}
        <div className="mb-3">
          <Button 
            type="submit" 
            disabled={submitting}
            className="!h-12 w-full !rounded-[12px] !text-sm !font-medium transition-all duration-200 hover:-translate-y-0.5 active:translate-y-0 active:scale-[0.99] sm:!h-[52px] sm:!text-[15px]"
            style={{ background: 'var(--auth-btn-bg)', color: 'var(--auth-btn-text)' }}
          >
            {submitting ? 'Đang xử lý...' : (isLogin ? 'Đăng nhập' : 'Tạo tài khoản')}
          </Button>
        </div>

        {!isLogin && hasPendingRegistration ? (
          <button
            type="button"
            onClick={onContinueRegistration}
            disabled={submitting}
            className="mb-3 w-full text-sm font-medium text-zinc-950 hover:underline disabled:opacity-60"
          >
            Tiếp tục xác minh đăng ký đang chờ
          </button>
        ) : null}

        <div className="mb-3 flex items-center gap-3">
          <span className="h-px flex-1 bg-zinc-200" />
          <span className="text-xs font-medium text-zinc-400">Hoặc tiếp tục với</span>
          <span className="h-px flex-1 bg-zinc-200" />
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
        <p className="mb-1 mt-3 text-center text-[13px] font-medium text-zinc-600 sm:text-sm">
          {isLogin ? (
            <>
              Chưa có tài khoản?{' '}
              <Link
                to="/register"
                className="font-semibold text-zinc-900 transition-all duration-200 hover:text-zinc-950 hover:font-bold hover:underline hover:decoration-2 hover:underline-offset-4"
              >
                Đăng ký ngay
              </Link>
            </>
          ) : (
            <>
              Đã có tài khoản?{' '}
              <Link
                to="/login"
                className="font-semibold text-zinc-900 transition-all duration-200 hover:text-zinc-950 hover:font-bold hover:underline hover:decoration-2 hover:underline-offset-4"
              >
                Đăng nhập ngay
              </Link>
            </>
          )}
        </p>
      </form>
    </div>
  );
}
