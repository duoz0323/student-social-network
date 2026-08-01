import { useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useApp } from '../../../contexts/AppContext.jsx';
import AuthForm from '../components/AuthForm.jsx';
import AuthEntryLayout from '../components/AuthEntryLayout.jsx';
import { useLogin } from '../hooks/useLogin.js';
import { getAuthenticatedHome, getSafeReturnPath } from '../utils/authNavigation.js';

export default function LoginPage() {
  const { handleFutureSocialAuth } = useApp();
  const loginState = useLogin();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [successMessage, setSuccessMessage] = useState(() => (
    location.state?.reason === 'PASSWORD_RESET_SUCCESS'
      ? 'Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.'
      : ''
  ));
  const [message, setMessage] = useState(() => {
    if (location.state?.reason === 'SESSION_EXPIRED') return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
    if (location.state?.reason === 'BLOCKED') return 'Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.';
    if (location.state?.reason === 'USE_EXISTING_AUTH_METHOD') return 'Vui lòng đăng nhập bằng phương thức đã liên kết với tài khoản hiện có.';
    if (location.state?.reason === 'SOCIAL_CONFLICT_EXPIRED') return 'Phiên xử lý đã hết hạn. Vui lòng bắt đầu lại đăng nhập social.';
    if (location.state?.reason === 'SOCIAL_CONFLICT_UNAVAILABLE') return 'Phiên xử lý không còn khả dụng. Vui lòng bắt đầu lại đăng nhập social.';
    if (location.state?.reason === 'SOCIAL_CONFLICT_OUTCOME_UNKNOWN') return 'Chưa xác định được kết quả trước đó. Vui lòng đăng nhập lại để kiểm tra an toàn.';
    if (location.state?.reason === 'PASSWORD_RESET_OUTCOME_UNKNOWN') return 'Chưa xác định được kết quả đặt lại mật khẩu. Hãy thử đăng nhập bằng mật khẩu mới; nếu không được, vui lòng bắt đầu khôi phục lại.';
    return '';
  });

  async function submit() {
    setMessage('');
    setSuccessMessage('');
    try {
      const session = await loginState.login(form);
      if (!session) return;
      const returnPath = getSafeReturnPath(location.state?.from, session);
      setForm((current) => ({ ...current, password: '' }));
      navigate(returnPath ?? getAuthenticatedHome(session), { replace: true });
    } catch {
      // Mật khẩu không được giữ lại sau một lần đăng nhập thất bại.
      setForm((current) => ({ ...current, password: '' }));
    }
  }

  function showFutureMessage(providerName) {
    setSuccessMessage('');
    setMessage(handleFutureSocialAuth(providerName).message);
  }

  function finishSocialAuthentication(session) {
    const returnPath = getSafeReturnPath(location.state?.from, session);
    navigate(returnPath ?? getAuthenticatedHome(session), { replace: true });
  }

  return (
    <AuthEntryLayout>
      <AuthForm
        type="login"
        form={form}
        setForm={setForm}
        onSubmit={submit}
        submitting={loginState.isSubmitting || loginState.retrySeconds > 0}
        message={loginState.generalError || message}
        successMessage={successMessage}
        fieldErrors={loginState.fieldErrors}
        retrySeconds={loginState.retrySeconds}
        onFieldChange={loginState.clearError}
        showFutureMessage={showFutureMessage}
        onGoogleAuthenticated={finishSocialAuthentication}
        onGoogleConflict={() => navigate('/auth/social-conflict', { replace: true })}
        onFacebookAuthenticated={finishSocialAuthentication}
        onFacebookConflict={() => navigate('/auth/social-conflict', { replace: true })}
      />
    </AuthEntryLayout>
  );
}
