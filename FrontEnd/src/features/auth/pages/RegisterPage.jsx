import { useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useApp } from '../../../contexts/AppContext.jsx';
import AuthForm from '../components/AuthForm.jsx';
import AuthEntryLayout from '../components/AuthEntryLayout.jsx';
import { useRegistration } from '../hooks/useRegistration.js';
import { getAuthenticatedHome } from '../utils/authNavigation.js';
import { getRegistrationErrorMessage } from '../utils/registrationErrorMapper.js';
import { validateRegistration } from '../validation/registrationValidation.js';

export default function RegisterPage() {
  const { handleFutureSocialAuth } = useApp();
  const registration = useRegistration();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '', confirmPassword: '', acceptTerms: false });
  const [message, setMessage] = useState(() => {
    if (location.state?.reason === 'SOCIAL_CONFLICT_EXPIRED') return 'Phiên xử lý đã hết hạn. Bạn có thể tiếp tục đăng ký đang chờ hoặc bắt đầu lại social.';
    if (location.state?.reason === 'SOCIAL_CONFLICT_OUTCOME_UNKNOWN') return 'Chưa xác định được kết quả xử lý social. Đăng ký đang chờ vẫn được giữ nguyên.';
    return location.state?.reason ? 'Bạn có thể bắt đầu hoặc tiếp tục đăng ký đang chờ.' : '';
  });
  const [fieldErrors, setFieldErrors] = useState({});

  async function submit() {
    const errors = validateRegistration(form);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    setFieldErrors({});
    setMessage('');
    try {
      const flow = await registration.startRegistration({
        email: form.email.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
      });
      if (flow) navigate('/register/verify', { replace: true });
    } catch (error) {
      setFieldErrors(error.fieldErrors ?? {});
      setMessage(getRegistrationErrorMessage(error));
    } finally {
      // Không giữ password và confirmPassword trong state sau request.
      setForm((current) => ({ ...current, password: '', confirmPassword: '' }));
    }
  }

  function updateForm(nextForm) {
    setForm(nextForm);
    setFieldErrors({});
    setMessage('');
  }

  function showFutureMessage(providerName) {
    setMessage(handleFutureSocialAuth(providerName).message);
  }

  return (
    <AuthEntryLayout>
      <AuthForm
        type="register"
        form={form}
        setForm={updateForm}
        onSubmit={submit}
        submitting={registration.isSubmitting}
        message={message}
        fieldErrors={fieldErrors}
        showFutureMessage={showFutureMessage}
        includeRegistrationFlow
        hasPendingRegistration={registration.hasFlow}
        onContinueRegistration={() => navigate('/register/verify')}
        onGoogleAuthenticated={(session) => navigate(getAuthenticatedHome(session), { replace: true })}
        onGoogleConflict={() => navigate('/auth/social-conflict', { replace: true })}
        onFacebookAuthenticated={(session) => navigate(getAuthenticatedHome(session), { replace: true })}
        onFacebookConflict={() => navigate('/auth/social-conflict', { replace: true })}
      />
    </AuthEntryLayout>
  );
}
